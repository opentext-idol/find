define([
    'backbone',
    'underscore',
    'find/app/util/model-any-changed-attribute-listener',
    'find/app/util/topic-map-view',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'text!find/templates/app/page/search/results/entity-topic-map-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'iCheck',
    'slider/bootstrap-slider'
], function(Backbone, _, addChangeListener, TopicMapView, i18n, configuration, template, loadingTemplate) {

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

    var CLUSTER_MODE = 'docsWithPhrase';

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'slideStop .speed-slider': function(event) {
                var maxResults = event.value;

                this.model.set('maxResults', maxResults);
            }
        },

        initialize: function(options) {
            this.entityCollection = options.entityCollection;
            this.queryModel = options.queryModel;

            this.topicMap = new TopicMapView({
                clickHandler: options.clickHandler
            });

            var viewState;

            if (this.entityCollection.currentRequest) {
                viewState = ViewState.LOADING;
            } else {
                viewState = this.entityCollection.isEmpty() ? ViewState.EMPTY : ViewState.MAP;
            }

            this.viewModel = new Backbone.Model({
                state: viewState
            });

            this.model = new Backbone.Model({
                maxCount: 10
            });

            this.listenTo(this.model, 'change:maxResults', this.fetchRelatedConcepts);

            this.listenTo(this.entityCollection, 'sync', function() {
                this.viewModel.set('state', this.entityCollection.isEmpty() ? ViewState.EMPTY : ViewState.MAP);
                this.updateTopicMapData();
                this.update();
            });

            this.listenTo(this.entityCollection, 'request', function() {
                this.viewModel.set('state', ViewState.LOADING);
            });

            this.listenTo(this.entityCollection, 'error', function(collection, xhr) {
                // Status of zero means the request has been aborted
                this.viewModel.set('state', xhr.status === 0 ? ViewState.LOADING : ViewState.ERROR);
            });

            this.listenTo(this.viewModel, 'change', this.updateViewState);

            this.updateTopicMapData();
        },

        update: function() {
            // If the view is not visible, update will be called again if the user switches to this tab
            if (this.$el.is(':visible')) {
                this.topicMap.draw();
            }
        },

        updateTopicMapData: function() {
            var data = _.chain(this.entityCollection.groupBy('cluster'))
                // Order the concepts in each cluster
                .map(function (cluster) {
                    return _.sortBy(cluster, function (model) {
                        return -model.get(CLUSTER_MODE);
                    });
                })
                // For each related concept give the name and size
                .map(function(cluster) {
                    return cluster.map(function (model) {
                        return {name: model.get('text'), size: model.get(CLUSTER_MODE)};
                    })
                })
                // Give each cluster a name (first concept in list), total size and add all concepts to the children attribute to create the topic map double level
                .map(function(cluster) {
                    var size = _.chain(cluster)
                        .pluck('size')
                        .reduce(function(a,b) {return a + b;})
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
            this.$('.entity-topic-map-error').toggleClass('hide', state !== ViewState.ERROR);
            this.$('.entity-topic-map-empty').toggleClass('hide', state !== ViewState.EMPTY);
            this.$('.entity-topic-map-loading').toggleClass('hide', state !== ViewState.LOADING);
        },

        fetchRelatedConcepts: function() {
            if (this.queryModel.get('queryText') && this.queryModel.get('indexes').length !== 0) {
                var data = {
                    databases: this.queryModel.get('indexes'),
                    queryText: this.queryModel.get('queryText'),
                    fieldText: this.queryModel.get('fieldText'),
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    minScore: this.queryModel.get('minScore'),
                    stateTokens: this.queryModel.get('stateMatchIds'),
                    maxResults: this.model.get('maxResults')
                };

                this.entityCollection.fetch({data: data});
            }
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: loadingHtml,
                cid: this.cid
            }));

            this.$('.speed-slider')
                .slider({
                    id: this.cid + '-speed-slider',
                    min: 50,
                    max: configuration().topicMapMaxResults,
                    value: 300
                });

            this.topicMap.setElement(this.$('.entity-topic-map')).render();
            this.update();
            this.updateViewState();
        }
    });

});


