define([
    'backbone',
    'jquery',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'find/app/util/popover',
    'find/app/util/search-data-util',
    'find/app/util/view-state-selector',
    'text!find/templates/app/page/search/related-concepts/related-concepts-view.html',
    'text!find/templates/app/page/search/related-concepts/related-concept-cluster.html',
    'text!find/templates/app/page/search/popover-message.html',
    'text!find/templates/app/page/search/results-popover.html',
    'text!find/templates/app/page/loading-spinner.html'
], function (Backbone, $, _, i18n, DocumentsCollection, popover, searchDataUtil, viewStateSelector, viewTemplate, clusterTemplate,
             popoverMessageTemplate, popoverTemplate, loadingSpinnerTemplate) {

    var html = _.template(viewTemplate)({
        i18n: i18n,
        loadingSpinnerHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false})
    });

    var clusterTemplateFunction = _.template(clusterTemplate);
    var popoverTemplateFunction = _.template(popoverTemplate);
    var popoverMessageTemplateFunction = _.template(popoverMessageTemplate);

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
        var entityCluster = $target.data('entityCluster');
        var clusterEntities = _.isUndefined(entityCluster) ? [$target.data('entityText')] : this.entityCollection.getClusterEntities(entityCluster);
        var relatedConcepts = _.union(this.queryTextModel.get('relatedConcepts'), clusterEntities);

        var queryText = searchDataUtil.makeQueryText(this.queryTextModel.get('inputText'), relatedConcepts);

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
            error: _.bind(function () {
                $content.html(popoverMessageTemplateFunction({message: i18n['search.relatedConcepts.topResults.error']}));
            }, this),
            success: _.bind(function () {
                if (topResultsCollection.isEmpty()) {
                    $content.html(popoverMessageTemplateFunction({message: i18n['search.relatedConcepts.topResults.none']}));
                } else {
                    $content.html('<ul class="list-unstyled"></ul>');
                    _.each(topResultsCollection.models, function (model) {
                        var listItem = $(popoverTemplateFunction({
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
        className: 'p-l-sm suggestions-content',
        viewStateSelector: _.noop,

        events: {
            'click [data-entity-text]': function (e) {
                var $target = $(e.currentTarget);
                var text = $target.attr('data-entity-text');
                this.clickHandler([text]);
            },
            'click [data-entity-cluster]': function (e) {
                var $target = $(e.currentTarget);
                var queryCluster = Number($target.attr('data-entity-cluster'));
                this.clickHandler(this.entityCollection.getClusterEntities(queryCluster));
            }
        },

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.queryTextModel = options.queryTextModel;
            this.entityCollection = options.entityCollection;
            this.indexesCollection = options.indexesCollection;
            this.clickHandler = options.clickHandler;

            var initialViewState;

            if (this.indexesCollection.isEmpty()) {
                initialViewState = ViewState.NOT_LOADING;
            } else {
                initialViewState = this.entityCollection.isEmpty() ? ViewState.PROCESSING : ViewState.LIST;
            }

            this.model = new Backbone.Model({viewState: initialViewState});
            this.listenTo(this.model, 'change:viewState', updateForViewState);

            // Each instance of this view gets its own bound, de-bounced popover handler
            var handlePopover = _.debounce(_.bind(popoverHandler, this), 500);

            this.listenTo(this.entityCollection, 'reset', function () {
                if (this.indexesCollection.isEmpty()) {
                    this.model.set('viewState', ViewState.NOT_LOADING);
                } else {
                    if (this.entityCollection.isEmpty()) {
                        this.model.set('viewState', ViewState.NONE);
                    } else {
                        this.model.set('viewState', ViewState.LIST);

                        var html = this.entityCollection.chain()
                            .groupBy(function (model) {
                                return model.get('cluster');
                            })
                            .map(function (models, cluster) {
                                return clusterTemplateFunction({
                                    entities: _.map(models, function (model) {
                                        return model.get('text');
                                    }),
                                    cluster: cluster
                                });
                            })
                            .value()
                            .join('');

                        this.$list.html(html);

                        popover(this.$list.find('.entity-text'), 'hover', handlePopover);
                    }
                }
            });

            this.listenTo(this.entityCollection, 'request', function () {
                this.model.set('viewState', this.indexesCollection.isEmpty() ? ViewState.NOT_LOADING : ViewState.PROCESSING);
            });

            this.listenTo(this.entityCollection, 'error', function () {
                this.model.set('viewState', ViewState.ERROR);
            });
        },

        render: function () {
            this.$el.html(html);

            this.$list = this.$('.related-concepts-list');
            this.$error = this.$('.related-concepts-error');
            this.$none = this.$('.related-concepts-none');
            this.$notLoading = this.$('.related-concepts-not-loading');

            this.$processing = this.$('.related-concepts-processing');

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
