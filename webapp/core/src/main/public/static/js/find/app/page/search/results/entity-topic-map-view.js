/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'find/app/util/topic-map-view',
    'find/app/model/entity-collection',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/entity-topic-map-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'iCheck'
], function(_, $, Backbone, TopicMapView, EntityCollection, i18n, configuration, generateErrorHtml,
            template, loadingTemplate) {
    'use strict';

    const loadingHtml = _.template(loadingTemplate)({i18n: i18n, large: true});

    /**
     * @readonly
     * @enum {String}
     */
    const ViewState = {
        LOADING: 'LOADING',
        ERROR: 'ERROR',
        EMPTY: 'EMPTY',
        MAP: 'MAP'
    };

    const Type = {
        QUERY: 'QUERY',
        COMPARISON: 'COMPARISON'
    };

    const CLUSTER_MODE = 'docsWithPhrase';

    function sum(a, b) {
        return a + b;
    }

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'change .speed-slider': function(e) {
                const $target = $(e.target);
                const value = $target.val();
                $target.attr('data-original-title', value);
                $target.tooltip('show');
                $target.blur();
                this.model.set('maxResults', value);
            },
            'input .speed-slider': function(e) {
                const $target = $(e.target);
                const value = $target.val();
                this.$('.tooltip-inner').text(value);
            }
        },

        initialize: function(options) {
            this.queryState = options.queryState;

            this.entityCollection = new EntityCollection([], {
                getSelectedRelatedConcepts: function() {
                    // Comparison topic view does not have queryState
                    return this.queryState
                        ? _.flatten(this.queryState.conceptGroups.pluck('concepts'))
                        : [];
                }.bind(this)
            });

            this.debouncedFetchRelatedConcepts = _.debounce(this.fetchRelatedConcepts.bind(this), 500);

            this.queryModel = options.queryModel;
            this.type = options.type;
            this.showSlider = _.isUndefined(options.showSlider) || options.showSlider;
            this.fixedHeight = _.isUndefined(options.fixedHeight) || options.fixedHeight;

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

            this.listenTo(this.model, 'change:maxResults', this.debouncedFetchRelatedConcepts);
            this.listenTo(this.queryModel, 'change', this.fetchRelatedConcepts);

            this.listenTo(this.entityCollection, 'sync', function() {
                this.viewModel.set('state', this.entityCollection.isEmpty()
                    ? ViewState.EMPTY
                    : ViewState.MAP);
                this.updateTopicMapData();
                this.update();
            });

            this.listenTo(this.entityCollection, 'request', function() {
                this.viewModel.set('state', ViewState.LOADING);
            });

            this.listenTo(this.entityCollection, 'error', function(collection, xhr) {
                this.generateErrorMessage(xhr);
                // Status of zero means the request has been aborted
                this.viewModel.set('state', xhr.status === 0
                    ? ViewState.LOADING
                    : ViewState.ERROR);
            });

            this.listenTo(this.viewModel, 'change', this.updateViewState);
            this.fetchRelatedConcepts();
        },

        render: function() {
            if(this.showSlider) {
                this.$('.speed-slider').tooltip('destroy');
            }

            this.$el.html(this.template({
                cid: this.cid,
                errorTemplate: this.errorTemplate,
                i18n: i18n,
                loadingHtml: loadingHtml,
                showSlider: this.showSlider,
                fixedHeight: this.fixedHeight,
                min: 50,
                max: configuration().topicMapMaxResults,
                step: 1
            }));

            this.$error = this.$('.entity-topic-map-error');
            this.$empty = this.$('.entity-topic-map-empty');
            this.$loading = this.$('.entity-topic-map-loading');

            if(this.showSlider) {
                const maxResults = this.model.get('maxResults');
                this.$('.speed-slider')
                    .attr('value', maxResults)
                    .tooltip({
                        title: maxResults,
                        placement: 'top'
                    });
            }

            this.topicMap.setElement(this.$('.entity-topic-map')).render();
            this.update();
            this.updateViewState();
        },

        remove: function() {
            if(this.showSlider) {
                this.$('.speed-slider').tooltip('destroy');
            }

            Backbone.View.prototype.remove.call(this);
        },

        update: function() {
            this.topicMap.draw();
        },

        updateTopicMapData: function() {
            const data = _.chain(this.entityCollection.groupBy('cluster'))
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
                // Give each cluster a name (first concept in list), total size and add all
                // concepts to the children attribute to create the topic map double level.
                .map(function(cluster) {
                    return {
                        name: cluster[0].name,
                        size: _.chain(cluster)
                            .pluck('size')
                            .reduce(sum)
                            .value(),
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
            const state = this.viewModel.get('state');
            this.topicMap.$el.toggleClass('hide', state !== ViewState.MAP);

            if(this.$error) {
                if(state === ViewState.ERROR) {
                    this.$error.html(this.errorTemplate);
                }
                this.$error.toggleClass('hide', state !== ViewState.ERROR);
            }

            if(this.$empty) {
                this.$empty.toggleClass('hide', state !== ViewState.EMPTY);
            }

            if(this.$loading) {
                this.$loading.toggleClass('hide', state !== ViewState.LOADING);
            }
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

        fetchRelatedConcepts: function() {
            let data;

            if(this.type === Type.COMPARISON) {
                data = {
                    queryText: '*',
                    stateDontMatchTokens: this.queryModel.get('stateDontMatchIds')
                };
            } else if(this.queryModel.get('queryText') && this.queryModel.get('indexes').length > 0) {
                data = {
                    queryText: this.queryModel.get('queryText'),
                    fieldText: this.queryModel.get('fieldText'),
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    minScore: this.queryModel.get('minScore')
                };
            }

            return data
                ? this.entityCollection.fetch({
                    data: _.extend(data, {
                        databases: this.queryModel.get('indexes'),
                        maxResults: this.model.get('maxResults'),
                        stateMatchTokens: this.queryModel.get('stateMatchIds')
                    })
                })
                : null;
        },

        exportData: function() {
            const paths = this.topicMap.exportPaths();
            return paths
                ? {paths: _.flatten(paths.slice(1).reverse())}
                : null;
        },
    });
});
