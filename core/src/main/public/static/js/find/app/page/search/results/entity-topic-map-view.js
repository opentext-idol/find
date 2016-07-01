define([
    'backbone',
    'underscore',
    'find/app/util/model-any-changed-attribute-listener',
    'find/app/util/topic-map-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/entity-topic-map-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'iCheck',
    'slider/bootstrap-slider'
], function(Backbone, _, addChangeListener, TopicMapView, i18n, template, loadingTemplate) {

    'use strict';

    var loadingHtml = _.template(loadingTemplate)({i18n: i18n, large: true});

    var clusteringModes = [
        {value: 'occurrences', text: i18n['search.topicMap.occurrences']},
        {value: 'docsWithPhrase', text: i18n['search.topicMap.documents']}
    ];

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

    // Return the given value if it is in the range [min, max], else return the number in the range which is closest to the value.
    function ensureRange(min, max, value) {
        return Math.max(min, Math.min(max, value));
    }

    // The maximum topic count is the number of entities which match the current relevance
    function resolveMaxCount(entityCollection) {
        return entityCollection
            .length;
    }

    // Update the clustering model after the entity collection or cluster mode has changed
    function updateClusteringModelForEntities(entityCollection, clusteringModel) {
        if (!entityCollection.isEmpty()) {
            var maxCount = resolveMaxCount(entityCollection);

            clusteringModel.set({
                maxCount: maxCount,
                count: ensureRange(1, maxCount, clusteringModel.get('count'))
            });
        }
    }

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'slideStop .speed-slider': function(event) {
                var maxResults = event.value;

                this.model.set('maxResults', maxResults);
            },
            'change .count-slider': function(event) {
                this.model.set('count', event.value.newValue);
            },
            'ifChecked .clustering-mode-i-check': function(event) {
                this.model.set('mode', event.target.value);
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
                mode: clusteringModes[0].value,
                count: 10,
                maxCount: 10
            });

            this.listenTo(this.model, 'change:maxResults', this.fetchRelatedConcepts);

            updateClusteringModelForEntities(this.entityCollection, this.model);

            this.listenTo(this.entityCollection, 'sync', function() {
                this.viewModel.set('state', this.entityCollection.isEmpty() ? ViewState.EMPTY : ViewState.MAP);
                updateClusteringModelForEntities(this.entityCollection, this.model);
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

            this.listenTo(this.model, 'change:mode', function() {
                updateClusteringModelForEntities(this.entityCollection, this.model);
            });

            addChangeListener(this, this.model, ['count', 'mode'], function() {
                this.updateTopicMapData();
                this.update();
            });

            addChangeListener(this, this.model, ['count', 'maxCount'], this.updateCountSlider);

            this.updateTopicMapData();
        },

        update: function() {
            // If the view is not visible, update will be called again if the user switches to this tab
            if (this.$el.is(':visible')) {
                this.topicMap.draw();
            }
        },

        updateTopicMapData: function() {
            var mode = this.model.get('mode');

            var data = _.chain(this.entityCollection.groupBy('cluster'))
                // Order the concepts in each cluster
                .map(function (cluster) {
                    return _.sortBy(cluster, function (model) {
                        return -model.get(mode);
                    });
                })
                // For each related concept give the name and size
                .map(function(cluster) {
                    return cluster.map(function (model) {
                        return {name: model.get('text'), size: model.get(mode)};
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

        updateCountSlider: function() {
            if (this.$countSlider) {
                this.$countSlider
                    .slider('setAttribute', 'max', this.model.get('maxCount'))
                    .slider('setValue', this.model.get('count'));
            }
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
                clusteringModes: clusteringModes,
                selectedMode: this.model.get('mode'),
                cid: this.cid
            }));

            this.$el.find('.i-check')
                .iCheck({radioClass: 'iradio-hp'});

            this.$countSlider = this.$('.count-slider')
                .slider({
                    id: this.cid + '-count-slider',
                    min: 1,
                    max: this.model.get('maxCount'),
                    value: this.model.get('count'),
                    tooltip_position: 'bottom'
                });

            this.$('.speed-slider')
                .slider({
                    id: this.cid + '-speed-slider',
                    min: 50,
                    max: 200,
                    value: 50
                });

            this.topicMap.setElement(this.$('.entity-topic-map')).render();
            this.update();
            this.updateViewState();
        }
    });

});


