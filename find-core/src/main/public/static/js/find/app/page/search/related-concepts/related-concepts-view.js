define([
    'backbone',
    'jquery',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'find/app/util/popover',
    'find/app/util/view-state-selector',
    'text!find/templates/app/page/search/related-concepts/related-concepts-view.html',
    'text!find/templates/app/page/search/related-concepts/related-concept-list-item.html',
    'text!find/templates/app/page/search/popover-message.html',
    'text!find/templates/app/page/search/results-popover.html',
    'text!find/templates/app/page/loading-spinner.html'
], function(Backbone, $, _, i18n, DocumentsCollection, popover, viewStateSelector, relatedConceptsView, relatedConceptListItemTemplate,
            popoverMessageTemplate, popoverTemplate, loadingSpinnerTemplate) {

    function popoverHandler($content, $target) {

        var entityCluster = $target.data().entityCluster;
        var relatedConcepts = entityCluster > -1 ? this.getClusteredConcepts(entityCluster) : [$target.data().entityText];

        var queryText = searchDataUtil.makeQueryText(this.queryTextModel.get('inputText'), _.union(relatedConcepts, this.queryTextModel.get('relatedConcepts')));

        var topResultsCollection = new DocumentsCollection([], {
            indexesCollection: this.indexesCollection
        });

        topResultsCollection.fetch({
            reset: true,
            data: {
                field_text: this.queryModel.get('fieldText'),
                min_date: this.queryModel.getIsoDate('minDate'),
                max_date: this.queryModel.getIsoDate('maxDate'),
                text: queryText,
                max_results: 3,
                summary: 'context',
                indexes: this.queryModel.get('indexes'),
                highlight: false
            },
            error: _.bind(function() {
                $content.html(this.popoverMessageTemplate({message: i18n['search.relatedConcepts.topResults.error']}));
            }, this),
            success: _.bind(function() {
                if (topResultsCollection.isEmpty()) {
                    $content.html(this.popoverMessageTemplate({message: i18n['search.relatedConcepts.topResults.none']}));
                } else {
                    $content.html('<ul class="list-unstyled"></ul>');
                    _.each(topResultsCollection.models, function(model) {
                        var listItem = $(this.popoverTemplate({
                            title: model.get('title'),
                            summary: model.get('summary').trim().substring(0, 100) + '...'
                        }));
                        $content.find('ul').append(listItem);
                    }, this);
                }
            }, this)
        });
    }

    return Backbone.View.extend({
        className: 'suggestions-content',

        template: _.template(relatedConceptsView),
        listItemTemplate: _.template(relatedConceptListItemTemplate),
        popoverTemplate: _.template(popoverTemplate),
        popoverMessageTemplate: _.template(popoverMessageTemplate),
        loadingSpinnerTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),

        events: {
            'click .entity-text' : function(e) {
                var $target = $(e.target);
                var queryText = $target.attr('data-title');

                if (this.queryTextModel.get('inputText') === ''){
                    this.queryTextModel.set('inputText', queryText);
                } else {
                    var concepts = this.queryTextModel.get('relatedConcepts');

                    var newConcepts = _.union(concepts, [queryText]);
                    this.queryTextModel.set('relatedConcepts', newConcepts);
                }
            },
            'click .highlight-related-concepts' : function(e) {
                this.highlightToggle.set('highlightConcepts', !this.highlightToggle.get('highlightConcepts'));
                $(e.target).toggleClass('active', this.highlightToggle.get('highlightConcepts'));
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.queryTextModel = options.queryTextModel;
            this.entityCollection = options.entityCollection;
            this.indexesCollection = options.indexesCollection;
            this.highlightToggle = options.highlightToggle;

            // Each instance of this view gets its own bound, de-bounced popover handler
            var handlePopover = _.debounce(_.bind(popoverHandler, this), 500);

            this.listenTo(this.entityCollection, 'reset', function() {
                if (this.indexesCollection.isEmpty()) {
                    this.selectViewState(['notLoading']);
                } else {
                    this.$list.empty();

                    var $highlight = this.$(".highlight-related-concepts");
                    if (this.entityCollection.isEmpty()) {
                        this.selectViewState(['none']);
                        $highlight.hide();
                    } else {
                        this.selectViewState(['list']);
                        $highlight.show();

                        var entities = this.entityCollection.chain()
                            .filter(function(model) {
                                return model.get('cluster') >= 0
                                    && model.get('text') !== this.queryTextModel.get('inputText'); // TODO: we may want to remove this if we reintroduce the hierarchical view
                            }, this)
                            .first(8)
                            .value();

                        _.each(entities, function (entity) {
                            this.$list.append(this.listItemTemplate({concept: entity.get('text')}));
                        }, this);

                        popover(this.$list.find('.entity-text'), 'hover', handlePopover);
                    }
                }
            });

            /*suggested links*/
            this.listenTo(this.entityCollection, 'request', function() {
                this.$(".highlight-related-concepts").hide();

                if (this.indexesCollection.isEmpty()) {
                    this.selectViewState(['notLoading']);
                } else {
                    this.selectViewState(['processing']);
                }
            });

            this.listenTo(this.entityCollection, 'error', function() {
                this.selectViewState(['error']);

                this.$error.text(i18n['search.error.relatedConcepts']);

                this.$(".highlight-related-concepts").hide();
            });
        },

        render: function() {
            this.$el.html(this.template({i18n:i18n}));

            this.$list = this.$('.related-concepts-list');
            this.$error = this.$('.related-concepts-error');

            this.$none = this.$('.related-concepts-none');

            this.$notLoading = this.$('.not-loading');

            this.$processing = this.$('.processing');
            this.$processing.append(this.loadingSpinnerTemplate);

            this.selectViewState = viewStateSelector({
                list: this.$list,
                processing: this.$processing,
                error: this.$error,
                none: this.$none,
                notLoading: this.$notLoading
            });
            if (this.indexesCollection.isEmpty()) {
                this.selectViewState(['notLoading']);
            } else {
                this.selectViewState(['processing'])
            }
        }
    });

});
