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
    function resolveMaxCount(entityCollection, clusteringMode, relevance) {
        return entityCollection
            .filter(function(model) {
                return model.get(clusteringMode) >= relevance;
            }, this)
            .length;
    }

    // Update the clustering model after the entity collection or cluster mode has changed
    function updateClusteringModelForEntities(entityCollection, clusteringModel) {
        if (!entityCollection.isEmpty()) {
            var clusteringMode = clusteringModel.get('mode');

            var minRelevance = entityCollection.min(clusteringMode).get(clusteringMode);
            var maxRelevance = entityCollection.max(clusteringMode).get(clusteringMode);
            var relevance = ensureRange(minRelevance, maxRelevance, clusteringModel.get('relevance'));
            var maxCount = resolveMaxCount(entityCollection, clusteringMode, relevance);

            clusteringModel.set({
                minRelevance: minRelevance,
                maxRelevance: maxRelevance,
                maxCount: maxCount,
                relevance: relevance,
                count: ensureRange(1, maxCount, clusteringModel.get('count'))
            });
        }
    }

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'change .relevance-slider': function(event) {
                var relevance = event.value.newValue;
                var attributes = {relevance: relevance};

                if (this.entityCollection.length) {
                    attributes.maxCount = resolveMaxCount(this.entityCollection, this.clusteringModel.get('mode'), relevance);
                    attributes.count = ensureRange(1, attributes.maxCount, this.clusteringModel.get('count'));
                }

                this.clusteringModel.set(attributes);
            },
            'change .count-slider': function(event) {
                this.clusteringModel.set('count', event.value.newValue);
            },
            'ifChecked .clustering-mode-i-check': function(event) {
                this.clusteringModel.set('mode', event.target.value);
            }
        },

        initialize: function(options) {
            this.entityCollection = options.entityCollection;

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

            this.clusteringModel = new Backbone.Model({
                mode: clusteringModes[0].value,
                relevance: 1,
                count: 10,
                minRelevance: 1,
                maxRelevance: 10,
                maxCount: 10
            });

            updateClusteringModelForEntities(this.entityCollection, this.clusteringModel);

            this.listenTo(this.entityCollection, 'sync', function() {
                this.viewModel.set('state', this.entityCollection.isEmpty() ? ViewState.EMPTY : ViewState.MAP);
                updateClusteringModelForEntities(this.entityCollection, this.clusteringModel);
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

            this.listenTo(this.clusteringModel, 'change:mode', function() {
                updateClusteringModelForEntities(this.entityCollection, this.clusteringModel);
            });

            addChangeListener(this, this.clusteringModel, ['count', 'relevance', 'mode'], function() {
                this.updateTopicMapData();
                this.update();
            });

            addChangeListener(this, this.clusteringModel, ['count', 'maxCount'], this.updateCountSlider);
            addChangeListener(this, this.clusteringModel, ['relevance', 'minRelevance', 'maxRelevance'], this.updateRelevanceSlider);

            this.updateTopicMapData();
        },

        update: function() {
            // If the view is not visible, update will be called again if the user switches to this tab
            if (this.$el.is(':visible')) {
                this.topicMap.draw();
            }
        },

        updateTopicMapData: function() {
            var relevance = this.clusteringModel.get('relevance');
            var mode = this.clusteringModel.get('mode');

            var data = this.entityCollection.chain()
                .filter(function(entity) {
                    return entity.get(mode) >= relevance;
                }, this)
                .sortBy(function(entity) {
                    return - entity.get(mode);
                })
                .first(this.clusteringModel.get('count'))
                .map(function(entity) {
                    return {name: entity.get('text'), size: entity.get(mode)};
                }, this)
                .value();

            this.topicMap.setData(data);
        },

        updateRelevanceSlider: function() {
            if (this.$relevanceSlider) {
                this.$relevanceSlider
                    .slider('setAttribute', 'min', this.clusteringModel.get('minRelevance'))
                    .slider('setAttribute', 'max', this.clusteringModel.get('maxRelevance'))
                    .slider('setValue', this.clusteringModel.get('relevance'));
            }
        },

        updateCountSlider: function() {
            if (this.$countSlider) {
                this.$countSlider
                    .slider('setAttribute', 'max', this.clusteringModel.get('maxCount'))
                    .slider('setValue', this.clusteringModel.get('count'));
            }
        },

        updateViewState: function() {
            var state = this.viewModel.get('state');
            this.topicMap.$el.toggleClass('hide', state !== ViewState.MAP);
            this.$('.entity-topic-map-error').toggleClass('hide', state !== ViewState.ERROR);
            this.$('.entity-topic-map-empty').toggleClass('hide', state !== ViewState.EMPTY);
            this.$('.entity-topic-map-loading').toggleClass('hide', state !== ViewState.LOADING);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: loadingHtml,
                clusteringModes: clusteringModes,
                selectedMode: this.clusteringModel.get('mode'),
                cid: this.cid
            }));

            this.$el.find('.i-check')
                .iCheck({radioClass: 'iradio-hp'});

            this.$countSlider = this.$('.count-slider')
                .slider({
                    id: this.cid + '-count-slider',
                    min: 1,
                    max: this.clusteringModel.get('maxCount'),
                    value: this.clusteringModel.get('count')
                });

            this.$relevanceSlider = this.$('.relevance-slider')
                .slider({
                    id: this.cid + '-relevance-slider',
                    min: this.clusteringModel.get('minRelevance'),
                    max: this.clusteringModel.get('maxRelevance'),
                    value: this.clusteringModel.get('relevance')
                });

            this.topicMap.setElement(this.$('.entity-topic-map')).render();
            this.update();
            this.updateViewState();
        }
    });

});


