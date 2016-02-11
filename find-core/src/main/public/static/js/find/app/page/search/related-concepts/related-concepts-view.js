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

    var html = _.template(relatedConceptsView)({i18n: i18n});
    var loadingSpinnerHtml = _.template(loadingSpinnerTemplate)({i18n: i18n, large: false});

    /**
     * @readonly
     * @enum {String}
     */
    var ViewState = {
        LIST: 'LIST',
        PROCESSING: 'PROCESSING',
        ERROR: 'ERROR',
        NONE: 'NONE',
        NOT_LOADING: 'NOT_LOADING'
    };

    function updateForViewState() {
        this.selectViewState([this.model.get('viewState')]);
    }

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

        listItemTemplate: _.template(relatedConceptListItemTemplate),
        popoverTemplate: _.template(popoverTemplate),
        popoverMessageTemplate: _.template(popoverMessageTemplate),

        viewStateSelector: _.noop,

        events: {
            'click .entity-text' : function(e) {
                var $target = $(e.target);
                var queryText = $target.attr('data-title');

                if (this.queryTextModel.get('inputText') === '') {
                    this.queryTextModel.set('inputText', queryText);
                } else {
                    var concepts = this.queryTextModel.get('relatedConcepts');
                    var newConcepts = _.union(concepts, [queryText]);
                    this.queryTextModel.set('relatedConcepts', newConcepts);
                }
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.queryTextModel = options.queryTextModel;
            this.entityCollection = options.entityCollection;
            this.indexesCollection = options.indexesCollection;

            this.model = new Backbone.Model({
                viewState: this.indexesCollection.isEmpty() ? ViewState.NOT_LOADING : ViewState.PROCESSING
            });

            this.listenTo(this.model, 'change:viewState', updateForViewState);

            // Each instance of this view gets its own bound, de-bounced popover handler
            var handlePopover = _.debounce(_.bind(popoverHandler, this), 500);

            this.listenTo(this.entityCollection, 'reset', function() {
                if (this.indexesCollection.isEmpty()) {
                    this.model.set('viewState', ViewState.NOT_LOADING);
                } else {
                    this.$list.empty();

                    if (this.entityCollection.isEmpty()) {
                        this.model.set('viewState', ViewState.NONE);
                    } else {
                        this.model.set('viewState', ViewState.LIST);

                        var entities = _.first(this.entityCollection.models, 8);

                        _.each(entities, function (entity) {
                            this.$list.append(this.listItemTemplate({concept: entity.get('text')}));
                        }, this);

                        popover(this.$list.find('.entity-text'), 'hover', handlePopover);
                    }
                }
            });

            /*suggested links*/
            this.listenTo(this.entityCollection, 'request', function() {
                if (this.indexesCollection.isEmpty()) {
                    this.model.set('viewState', ViewState.NOT_LOADING);
                } else {
                    this.model.set('viewState', ViewState.PROCESSING);
                }
            });

            this.listenTo(this.entityCollection, 'error', function() {
                this.model.set('viewState', ViewState.ERROR);
            });
        },

        render: function() {
            this.$el.html(html);

            this.$list = this.$('.related-concepts-list');
            this.$error = this.$('.related-concepts-error');
            this.$none = this.$('.related-concepts-none');
            this.$notLoading = this.$('.related-concepts-not-loading');

            this.$processing = this.$('.related-concepts-processing')
                .append(loadingSpinnerHtml);

            var viewStateElements = {};
            viewStateElements[ViewState.ERROR] = this.$error;
            viewStateElements[ViewState.PROCESSING] = this.$processing;
            viewStateElements[ViewState.NONE] = this.$none;
            viewStateElements[ViewState.NOT_LOADING] = this.$notLoading;
            viewStateElements[ViewState.LIST] = this.$list;

            this.selectViewState = viewStateSelector(viewStateElements);
            updateForViewState.call(this);
        }
    });

});
