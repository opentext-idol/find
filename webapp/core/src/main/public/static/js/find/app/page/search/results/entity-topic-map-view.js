/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'underscore',
    'find/app/util/topic-map-view',
    'find/app/model/entity-collection',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/entity-topic-map-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'iCheck',
    'slider/bootstrap-slider'
], function(Backbone, _, TopicMapView, EntityCollection, i18n, configuration, generateErrorHtml, template,
            loadingTemplate) {
    'use strict';

    var loadingHtml = _.template(loadingTemplate)({i18n: i18n, large: true});

    /**
     * @readonly
     * @enum {String}
     */
    var ViewState = {
        LOADING: 'LOADING',
        ERROR: 'ERROR',
        EMPTY: 'EMPTY',
        MAP: 'MAP'
    };

    var Type = {
        QUERY: 'QUERY',
        COMPARISON: 'COMPARISON'
    };

    var CLUSTER_MODE = 'docsWithPhrase';

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'slideStop .speed-slider': function(event) {
                var maxResults = event.value;

                this.model.set('maxResults', maxResults);
            },
            'click .entity-topic-map-pptx': function(evt){
                evt.preventDefault()

                var data = this.exportPPTData();

                if (data) {
                    // We need to append the temporary form to the document.body or Firefox and IE11 won't download the file.
                    // Previously used GET; but IE11 has a limited GET url length and loses data.
                    var $form = $('<form class="hide" enctype="multipart/form-data" method="post" target="_blank" action="api/bi/export/ppt/topicmap"><textarea name="data"></textarea><input type="submit"></form>');
                    $form[0].data.value = JSON.stringify(data)
                    $form.appendTo(document.body).submit().remove()
                }
            }
        },

        exportPPTData: function(){
            var paths = this.topicMap.exportPaths();

            return paths ? {
                paths: _.flatten(paths.slice(1).reverse())
            } : null
        },

        initialize: function(options) {
            this.queryState = options.queryState;

            this.entityCollection = new EntityCollection([], {
                getSelectedRelatedConcepts: function() {
                    // Comparison topic view does not have queryState
                    return this.queryState ? _.flatten(this.queryState.conceptGroups.pluck('concepts')) : [];
                }.bind(this)
            });

            this.queryModel = options.queryModel;
            this.type = options.type;
            this.showSlider = !_.isUndefined(options.showSlider) ? options.showSlider : true;

            this.topicMap = new TopicMapView({
                clickHandler: options.clickHandler
            });

            this.errorTemplate = generateErrorHtml({messageToUser: i18n['search.topicMap.error']});

            this.viewModel = new Backbone.Model({
                state: ViewState.EMPTY
            });

            this.model = new Backbone.Model({
                maxCount: 10,
                maxResults: options.maxResults || 300
            });

            this.listenTo(this.model, 'change:maxResults', this.fetchRelatedConcepts);
            this.listenTo(this.queryModel, 'change', this.fetchRelatedConcepts);

            this.listenTo(this.entityCollection, 'sync', function() {
                this.viewModel.set('state', this.entityCollection.isEmpty() ? ViewState.EMPTY : ViewState.MAP);
                this.updateTopicMapData();
                this.update();
            });

            this.listenTo(this.entityCollection, 'request', function() {
                this.viewModel.set('state', ViewState.LOADING);
            });

            this.listenTo(this.entityCollection, 'error', function(collection, xhr) {
                this.generateErrorMessage(xhr);
                // Status of zero means the request has been aborted
                this.viewModel.set('state', xhr.status === 0 ? ViewState.LOADING : ViewState.ERROR);
            });

            this.listenTo(this.viewModel, 'change', this.updateViewState);

            this.fetchRelatedConcepts();
        },

        update: function() {
            // If the view is not visible, update will be called again if the user switches to this tab
            if(this.$el.is(':visible')) {
                this.topicMap.draw();
            }
        },

        updateTopicMapData: function() {
            var data = _.chain(this.entityCollection.groupBy('cluster'))
            // Order the concepts in each cluster
                .map(function(cluster) {
                    return _.sortBy(cluster, function(model) {
                        return -model.get(CLUSTER_MODE);
                    });
                })
                // For each related concept give the name and size
                .map(function(cluster) {
                    return cluster.map(function(model) {
                        return {name: model.get('text'), size: model.get(CLUSTER_MODE)};
                    })
                })
                // Give each cluster a name (first concept in list), total size and add all concepts to the children attribute to create the topic map double level
                .map(function(cluster) {
                    var size = _.chain(cluster)
                        .pluck('size')
                        .reduce(function(a, b) {
                            return a + b;
                        })
                        .value();

                    return {
                        name: cluster[0].name,
                        size: size,
                        children: cluster
                    };
                })
                .sortBy(function(clusterNode) {
                    return -clusterNode.size;
                })
                .value();

            this.topicMap.setData(data);
        },

        updateViewState: function() {
            var state = this.viewModel.get('state');
            this.topicMap.$el.toggleClass('hide', state !== ViewState.MAP);
            this.handleTopicMapError();
            this.$('.entity-topic-map-empty').toggleClass('hide', state !== ViewState.EMPTY);
            this.$('.entity-topic-map-loading').toggleClass('hide', state !== ViewState.LOADING);
        },

        generateErrorMessage: function(xhr) {
            if(xhr.responseJSON) {
                this.errorTemplate = generateErrorHtml({
                    messageToUser: i18n['search.topicMap.error'],
                    errorDetails: xhr.responseJSON.message,
                    errorDetailsFallback: xhr.responseJSON.uuid,
                    errorUUID: xhr.responseJSON.uuid,
                    errorLookup: xhr.responseJSON.backendErrorCode
                });
            } else {
                this.errorTemplate = generateErrorHtml({
                    messageToUser: i18n['search.topicMap.error']
                });
            }
        },

        handleTopicMapError: function() {
            var state = this.viewModel.get('state');
            if(state === ViewState.ERROR) {
                this.$('.entity-topic-map-error').empty().append(this.errorTemplate);
            }
            this.$('.entity-topic-map-error').toggleClass('hide', state !== ViewState.ERROR);
        },

        fetchRelatedConcepts: function() {
            var data;

            if(this.type === Type.COMPARISON) {
                data = {
                    queryText: '*',
                    maxResults: this.model.get('maxResults'),
                    databases: this.queryModel.get('indexes'),
                    stateMatchTokens: this.queryModel.get('stateMatchIds'),
                    stateDontMatchTokens: this.queryModel.get('stateDontMatchIds')
                };

            } else if(this.queryModel.get('queryText') && this.queryModel.get('indexes').length !== 0) {
                data = {
                    databases: this.queryModel.get('indexes'),
                    queryText: this.queryModel.get('queryText'),
                    fieldText: this.queryModel.get('fieldText'),
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    minScore: this.queryModel.get('minScore'),
                    stateMatchTokens: this.queryModel.get('stateMatchIds'),
                    maxResults: this.model.get('maxResults')
                };
            }

            if(data) {
                return this.entityCollection.fetch({data: data});
            }
        },

        render: function() {
            this.$el.html(this.template({
                cid: this.cid,
                errorTemplate: this.errorTemplate,
                i18n: i18n,
                loadingHtml: loadingHtml,
                showSlider: this.showSlider
            }));

            if (this.showSlider) {
                this.$('.speed-slider')
                    .slider({
                        id: this.cid + '-speed-slider',
                        min: 50,
                        max: configuration().topicMapMaxResults,
                        value: this.model.get('maxResults')
                    });
            }

            this.topicMap.setElement(this.$('.entity-topic-map')).render();
            this.update();
            this.updateViewState();
        }
    });
});
