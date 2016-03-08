define([
    'backbone',
    'underscore',
    'find/app/vent',
    'find/app/util/model-any-changed-attribute-listener',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/topic-map-view.html',
    'topicmap/js/topicmap',
    'iCheck',
    'slider/bootstrap-slider'
], function(Backbone, _, vent, addChangeListener, i18n, template) {

    'use strict';

    var clusteringModes = [
        {value: 'occurrences', text: i18n['search.topicMap.occurrences']},
        {value: 'docsWithPhrase', text: i18n['search.topicMap.documents']}
    ];

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
            this.clickHandler = options.clickHandler;

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
                updateClusteringModelForEntities(this.entityCollection, this.clusteringModel);
                this.update();
            });

            this.listenTo(this.clusteringModel, 'change:mode', function() {
                updateClusteringModelForEntities(this.entityCollection, this.clusteringModel);
            });

            this.listenTo(vent, 'vent:resize', this.update);

            addChangeListener(this, this.clusteringModel, ['count', 'relevance', 'mode'], this.update);
            addChangeListener(this, this.clusteringModel, ['count', 'maxCount'], this.updateCountSlider);
            addChangeListener(this, this.clusteringModel, ['relevance', 'minRelevance', 'maxRelevance'], this.updateRelevanceSlider);
        },

        update: function() {
            // If the view is not visible, update will be called again if the user switches to this tab
            if (this.$el.is(':visible') && this.$topicMap) {
                var mode = this.clusteringModel.get('mode');

                this.$topicMap.topicmap('renderData', {
                    name: 'topic map',
                    size: 1.0,
                    sentiment: null,
                    children: this.entityCollection.chain()
                        .filter(function(entity) {
                            return entity.get(mode) >= this.clusteringModel.get('relevance');
                        }, this)
                        .first(this.clusteringModel.get('count'))
                        .map(function(entity) {
                            return {
                                name: entity.get('text'),
                                size: entity.get(mode),
                                sentiment: null,
                                children: null
                            };
                        }, this)
                        .value()
                }, false);
            }
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

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                clusteringModes: clusteringModes,
                selectedMode: this.clusteringModel.get('mode'),
                cid: this.cid
            }));

            this.$topicMap = this.$('.topic-map')
                .topicmap({
                    hideLegend: false,
                    skipAnimation: false,
                    i18n: {
                        'autn.vis.topicmap.noResultsAvailable': i18n['search.topicMap.noResults']
                    },
                    onLeafClick: _.bind(function(node) {
                        this.clickHandler(node.name);
                    }, this)
                });

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

            this.update();
        }
    });

});


