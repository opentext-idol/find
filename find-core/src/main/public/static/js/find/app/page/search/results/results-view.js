define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/vent',
    'find/app/model/document-model',
    'find/app/model/promotions-collection',
    'find/app/page/search/sort-view',
    'find/app/page/search/results/results-number-view',
    'find/app/util/view-server-client',
    'find/app/util/document-mime-types',
    'find/app/page/search/results/add-links-to-summary',
    'text!find/templates/app/page/search/results/results-view.html',
    'text!find/templates/app/page/search/results/results-container.html',
    'text!find/templates/app/page/loading-spinner.html',
    'moment',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes'
], function(Backbone, $, _, vent, DocumentModel, PromotionsCollection, SortView, ResultsNumberView,
            viewClient, documentMimeTypes, addLinksToSummary, template, resultsTemplate,
            loadingSpinnerTemplate, moment, i18n, i18n_indexes) {

    function checkScroll() {
        var triggerPoint = 500;
        var resultsPresent = this.documentsCollection.size() > 0 && this.fetchStrategy.validateQuery(this.queryModel);

        if (resultsPresent && this.resultsFinished && this.el.scrollHeight > 0 && this.el.scrollHeight + this.$el.offset().top - $(window).height() < triggerPoint) {
            this.infiniteScroll();
        }
    }

    function infiniteScroll() {
        if (!this.endOfResults) {
            this.start = this.maxResults + 1;
            this.maxResults += SCROLL_INCREMENT;

            this.loadData(true);
        }
    }

    function getContentTypeClass(model) {
        var contentType = model.get('contentType') || '';

        var matchedType = _.find(documentMimeTypes, function(mimeType) {
            return Boolean(_.find(mimeType.typeRegex, function(regex) {
                return regex().test(contentType);
            }));
        });

        return matchedType.className;
    }

    var SCROLL_INCREMENT = 30;

    return Backbone.View.extend({
        //to be overridden
        generateErrorMessage: null,

        template: _.template(template),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),
        resultsTemplate: _.template(resultsTemplate),
        messageTemplate: _.template('<div class="result-message span10"><%-message%> </div>'),
        errorTemplate: _.template('<li class="error-message span10"><span><%-feature%>: </span><%-error%></li>'),

        events: {
            'click .highlighted-entity-text': function(e) {
                e.stopPropagation();

                var $target = $(e.target);
                var queryText = $target.attr('data-entity-text');
                this.entityClickHandler(queryText);
            },
            'click .preview-mode [data-cid]': function(e) {
                var $target = $(e.currentTarget);

                if ($target.hasClass('selected-document')) {
                    //disable preview mode
                    this.trigger('close-preview');

                    //resetting selected-document class
                    this.$('.main-results-container').removeClass('selected-document');
                } else {
                    //enable/choose another preview view
                    this.trigger('preview', this.documentsCollection.get($target.data('cid')));

                    //resetting selected-document class and adding it to the target
                    this.$('.main-results-container').removeClass('selected-document');
                    $target.addClass('selected-document');
                }
            },
            'click .similar-documents-trigger': function(event) {
                event.stopPropagation();
                var documentModel = this.documentsCollection.get($(event.target).closest('[data-cid]').data('cid'));
                vent.navigateToSuggestRoute(documentModel);
            }
        },

        initialize: function(options) {
            this.fetchStrategy = options.fetchStrategy;
            this.enablePreview = options.enablePreview || false;

            this.queryModel = options.queryModel;
            this.documentsCollection = options.documentsCollection;

            this.indexesCollection = options.indexesCollection;

            if (this.indexesCollection) {
                this.selectedIndexesCollection = options.queryState.selectedIndexes;
            }

            this.entityCollection = options.entityCollection;

            if (this.entityCollection) {
                // Only required if we are highlighting entities
                this.queryTextModel = options.queryState.queryTextModel;
                this.highlightModel = options.highlightModel;
                this.listenTo(this.highlightModel, 'change:highlightEntities', this.updateEntityHighlighting);
                this.entityClickHandler = options.entityClickHandler;
            }

            this.promotionsCollection = new PromotionsCollection();

            this.sortView = new SortView({
                queryModel: this.queryModel
            });

            this.resultsNumberView = new ResultsNumberView({
                documentsCollection: this.documentsCollection
            });

            this.listenTo(this.queryModel, 'change', this.refreshResults);

            this.checkScroll = checkScroll.bind(this);
            this.infiniteScroll = _.debounce(infiniteScroll, 500, true);
        },

        refreshResults: function() {
            if (this.fetchStrategy.validateQuery(this.queryModel)) {
                if (this.fetchStrategy.waitForIndexes(this.queryModel)) {
                    this.$loadingSpinner.addClass('hide');
                    this.$('.main-results-content .results').html(this.messageTemplate({message: i18n_indexes['search.error.noIndexes']}));
                } else {
                    this.endOfResults = false;
                    this.start = 1;
                    this.maxResults = SCROLL_INCREMENT;
                    this.loadData(false);
                    this.$('.main-results-content .promotions').empty();

                    this.$loadingSpinner.removeClass('hide');
                    this.toggleError(false);
                    this.$('.main-results-content .error .error-list').empty();
                    this.$('.main-results-content .results').empty();
                }
            }
        },

        clearLoadingSpinner: function() {
            if (this.resultsFinished && this.promotionsFinished) {
                this.$loadingSpinner.addClass('hide');
            }
        },

        render: function() {
            this.$el.html(this.template({i18n: i18n}));

            if (this.enablePreview) {
                this.$('.main-results-content').addClass('preview-mode');
            }

            this.$loadingSpinner = $(this.loadingTemplate);

            this.$el.find('.results').after(this.$loadingSpinner);

            this.sortView.setElement(this.$('.sort-container')).render();
            this.resultsNumberView.setElement(this.$('.results-number-container')).render();

            this.listenTo(this.promotionsCollection, 'add', function(model) {
                this.formatResult(model, true);
            });

            this.listenTo(this.promotionsCollection, 'sync', function() {
                this.promotionsFinished = true;
                this.clearLoadingSpinner();
            });

            this.listenTo(this.promotionsCollection, 'error', function(collection, xhr) {
                this.promotionsFinished = true;
                this.clearLoadingSpinner();

                this.$('.main-results-content .promotions').append(this.handleError(i18n['app.feature.promotions'], xhr));
            });

            this.listenTo(this.documentsCollection, 'add', function(model) {
                this.formatResult(model, false);
            });

            this.listenTo(this.documentsCollection, 'sync reset', function() {
                this.resultsFinished = true;
                this.clearLoadingSpinner();

                this.endOfResults = this.maxResults >= this.documentsCollection.totalResults;

                if (this.endOfResults) {
                    this.$('.main-results-content .results').append(this.messageTemplate({message: i18n["search.noMoreResults"]}));
                } else if (this.documentsCollection.isEmpty()) {
                    this.$('.main-results-content .results').append(this.messageTemplate({message: i18n["search.noResults"]}));
                }
            });

            this.listenTo(this.documentsCollection, 'error', function(collection, xhr) {
                this.resultsFinished = true;
                this.clearLoadingSpinner();

                this.$('.main-results-content .results').append(this.handleError(i18n['app.feature.search'], xhr));
            });

            if (this.entityCollection) {
                this.listenTo(this.entityCollection, 'reset', function() {
                    if (!this.entityCollection.isEmpty()) {
                        this.documentsCollection.each(function(document) {
                            var summary = addLinksToSummary(this.entityCollection, document.get('summary'));

                            this.$('[data-cid="' + document.cid + '"] .result-summary').html(summary);
                        }, this);

                        this.promotionsCollection.each(function(document) {
                            var summary = addLinksToSummary(this.entityCollection, document.get('summary'));
                            this.$('[data-cid="' + document.cid + '"] .result-summary').html(summary);
                        }, this);
                    }
                });
            }

            // Do not bind here since the same function must be passed to the off method
            $('.main-content').scroll(this.checkScroll);

            if (this.documentsCollection.isEmpty()) {
                this.refreshResults();
            }

            if (this.entityCollection) {
                this.updateEntityHighlighting();
            }
        },

        updateEntityHighlighting: function() {
            this.$el.toggleClass('highlight-entities', this.highlightModel.get('highlightEntities'));
        },

        formatResult: function(model, isPromotion) {
            var data = {
                contentType: getContentTypeClass(model),
                date: model.has('date') ? model.get('date').fromNow() : null,
                highlightedSummary: addLinksToSummary(this.entityCollection, model.get('summary')),
                isPromotion: isPromotion,
                staticPromotion: model.get('promotionCategory') === 'STATIC_CONTENT_PROMOTION'
            };

            var $newResult = $(this.resultsTemplate({
                i18n: i18n,
                cid: model.cid,
                title: model.get('title'),
                reference: model.get('reference'),
                thumbnail: model.get('thumbnail'),
                contentType: data.contentType,
                date: data.date,
                isPromotion: data.isPromotion,
                summary: data.highlightedSummary,
                staticPromotion: data.staticPromotion
            }));

            if (isPromotion) {
                this.$('.main-results-content .promotions').append($newResult);
            } else {
                this.$('.main-results-content .results').append($newResult);
            }
        },

        handleError: function(feature, xhr) {
            this.toggleError(true);
            var message = this.generateErrorMessage(xhr);

            var messageTemplate = this.errorTemplate({feature: feature, error: message});
            this.$('.main-results-content .error .error-list').append(messageTemplate);
        },

        toggleError: function(on) {
            this.$('.main-results-content .promotions').toggleClass('hide', on);
            this.$('.main-results-content .results').toggleClass('hide', on);
            this.$('.main-results-content .error').toggleClass('hide', !on);
        },

        loadData: function(infiniteScroll) {
            this.$loadingSpinner.removeClass('hide');
            this.resultsFinished = false;

            var requestData = _.extend({
                start: this.start,
                max_results: this.maxResults,
                sort: this.queryModel.get('sort')
            }, this.fetchStrategy.requestParams(this.queryModel, infiniteScroll));

            this.documentsCollection.fetch({
                data: requestData,
                reset: false,
                remove: !infiniteScroll,
                error: function(collection) {
                    // if returns an error remove previous models from documentsCollection
                    if (collection) {
                        collection.reset({documents: []}, {parse: true});
                    }
                },
                success: function() {
                    if (this.indexesCollection && this.documentsCollection.warnings && this.documentsCollection.warnings.invalidDatabases) {
                        // Invalid databases have been deleted from IDOL; mark them as such in the indexes collection
                        this.documentsCollection.warnings.invalidDatabases.forEach(function(name) {
                            var indexModel = this.indexesCollection.findWhere({name: name});

                            if (indexModel) {
                                indexModel.set('deleted', true);
                            }

                            this.selectedIndexesCollection.remove({name: name});
                        }.bind(this));
                    }
                }.bind(this)
            });

            if (!infiniteScroll) {
                this.promotionsFinished = false;

                this.promotionsCollection.fetch({
                    data: requestData,
                    reset: false
                }, this);
            }
        },

        removeHighlighting: function() {
            this.$('.main-results-container').removeClass('selected-document');
        },

        remove: function() {
            $('.main-content').off('scroll', this.checkScroll);
            this.sortView.remove();
            this.resultsNumberView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

});
