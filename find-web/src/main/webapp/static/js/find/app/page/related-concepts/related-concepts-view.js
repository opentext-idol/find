define([
    '../../../../../bower_components/backbone/backbone',
    'jquery',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'find/app/util/popover',
    'find/app/util/view-state-selector',
    'text!find/templates/app/page/related-concepts/related-concepts-view.html',
    'text!find/templates/app/page/related-concepts/related-concept-list-item.html',
    'text!find/templates/app/page/popover-message.html',
    'text!find/templates/app/page/results-popover.html',
    'text!find/templates/app/page/loading-spinner.html'
], function(Backbone, $, _, i18n, DocumentsCollection, popover, viewStateSelector, relatedConceptsView, relatedConceptListItemTemplate,
            popoverMessageTemplate, popoverTemplate, loadingSpinnerTemplate) {

    function popoverHandler($content, $target) {
        var queryText = $target.text();

        var topResultsCollection = new DocumentsCollection([], {
            indexesCollection: this.indexesCollection
        });

        topResultsCollection.fetch({
            reset: true,
            data: {
                text: queryText,
                max_results: 3,
                summary: 'context',
                index: this.queryModel.get('indexes')
            },
            error: _.bind(function() {
                $content.html(this.popoverMessageTemplate({message: i18n['search.relatedConcepts.topResults.error']}));
            }, this),
            success: _.bind(function() {
                if (topResultsCollection.isEmpty()) {
                    $content.html(this.popoverMessageTemplate({message: i18n['search.relatedConcepts.topResults.none']}));
                } else {
                    $content.html(this.popoverTemplate({collection: topResultsCollection}));
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
            'click .query-text' : function(e) {
                var $target = $(e.target);
                var queryText = $target.attr('data-title');
                this.queryModel.set('queryText', queryText);
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.entityCollection = options.entityCollection;
            this.indexesCollection = options.indexesCollection;

            // Each instance of this view gets its own bound, de-bounced popover handler
            var handlePopover = _.debounce(_.bind(popoverHandler, this), 500);

            this.listenTo(this.entityCollection, 'reset', function() {
                this.$list.empty();

                if (this.entityCollection.isEmpty()) {
                    this.selectViewState(['none']);
                } else {
                    this.selectViewState(['list']);

                    var clusters = this.entityCollection.groupBy('cluster');

                    _.each(clusters, function(entities) {
                        this.$list.append(this.listItemTemplate({entities: entities}));
                    }, this);

                    popover(this.$list.find('.query-text'), handlePopover);
                }
            });

            /*suggested links*/
            this.listenTo(this.entityCollection, 'request', function() {
                this.selectViewState(['processing']);
            });

            this.listenTo(this.entityCollection, 'error', function() {
                this.selectViewState(['error']);

                this.$error.text(i18n['search.error.relatedConcepts']);
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
        }
    });

});
