define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/document-model',
    'find/app/model/promotions-collection',
    'find/app/model/similar-documents-collection',
    'find/app/page/search/sort-view',
    'find/app/page/search/results/results-number-view',
    'find/app/util/popover',
    'find/app/util/view-server-client',
    'find/app/util/document-mime-types',
    'find/app/page/search/results/add-links-to-summary',
    'text!find/templates/app/page/search/results-popover.html',
    'text!find/templates/app/page/search/popover-message.html',
    'text!find/templates/app/page/search/results/results-view.html',
    'text!find/templates/app/page/search/results/results-container.html',
    'text!find/templates/app/page/colorbox-controls.html',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/view/media-player.html',
    'text!find/templates/app/page/view/view-document.html',
    'moment',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'colorbox'
], function(Backbone, $, _, DocumentModel, PromotionsCollection, SimilarDocumentsCollection, SortView, ResultsNumberView, popover,
            viewClient, documentMimeTypes, addLinksToSummary, popoverTemplate, popoverMessageTemplate, template, resultsTemplate,
            colorboxControlsTemplate, loadingSpinnerTemplate, mediaPlayerTemplate, viewDocumentTemplate,
            moment, i18n, i18n_indexes) {

    var Mode = {
        STATE_TOKEN: 'STATE_TOKEN',
        QUERY: 'QUERY'
    };
    
    function infiniteScroll() {
        var totalResults = this.documentsCollection.totalResults;

        if (!this.endOfResults) {
            if (this.maxResults < totalResults && this.maxResults + SCROLL_INCREMENT > totalResults) {
                this.start = this.maxResults;
                this.maxResults = totalResults;
                this.endOfResults = true;
            } else {
                this.start += SCROLL_INCREMENT;
                this.maxResults += SCROLL_INCREMENT;
                if (this.maxResults === totalResults) {
                    this.endOfResults = true;
                }
            }
            this.loadData(true);
        }
    }

    var getContentTypeClass = function(model) {
        var contentType = model.get('contentType') || '';

        var matchedType = _.find(documentMimeTypes, function(mimeType) {
            return Boolean(_.find(mimeType.typeRegex, function(regex) {
                return regex().test(contentType);
            }));
        });

        return matchedType.className;
    };

    var $window = $(window);
    var SIZE = '90%';

    var SCROLL_INCREMENT = 30;

    var onResize = function() {
        $.colorbox.resize({width: SIZE, height: SIZE});
    };

    return Backbone.View.extend({
        //to be overridden
        generateErrorMessage: _.noop,

        template: _.template(template),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),
        resultsTemplate: _.template(resultsTemplate),
        popoverMessageTemplate: _.template(popoverMessageTemplate),
        messageTemplate: _.template('<div class="result-message span10"><%-message%> </div>'),
        errorTemplate: _.template('<li class="error-message span10"><span><%-feature%>: </span><%-error%></li>'),
        mediaPlayerTemplate: _.template(mediaPlayerTemplate),
        popoverTemplate: _.template(popoverTemplate),
        viewDocumentTemplate: _.template(viewDocumentTemplate),

        events: {
            'click .entity-text': function(e) {
                e.stopPropagation();

                var $target = $(e.target);
                var queryText = $target.attr('data-title');

                this.queryTextModel.set({
                    inputText: queryText,
                    relatedConcepts: []
                });
            },
            'click .preview-mode [data-reference]': function(e) {
                var $target = $(e.currentTarget);

                if ($target.hasClass('selected-document')) {
                    //disable preview mode
                    this.trigger('close-preview');

                    //resetting selected-document class
                    this.$('.main-results-container').removeClass('selected-document');
                } else {
                    //enable/choose another preview view
                    this.trigger('preview', this.documentsCollection.findWhere({reference: $target.data('reference')}));

                    //resetting selected-document class and adding it to the target
                    this.$('.main-results-container').removeClass('selected-document');
                    $target.addClass('selected-document');
                }
            }
        },

        initialize: function(options) {
            _.bindAll(this, 'handlePopover');

            this.mode = options.mode;
            this.isComparisonView = options.isComparisonView;

            this.loadData = this.stateTokenMode() ? this.stateTokenLoadData : this.normalLoadData;
            this.refreshResults = this.stateTokenMode() ? this.stateTokenRefreshResults : this.normalRefreshResults;
            this.checkScroll = _.bind(this.stateTokenMode() ? this.stateTokenCheckScroll : this.normalCheckScroll, this);
            this.clearLoadingSpinner = this.stateTokenMode ? this.stateTokenClearLoadingSpinner : this.normalClearLoadingSpinner;

            this.queryModel = options.queryModel;
            this.queryTextModel = options.queryTextModel;
            this.entityCollection = options.entityCollection;
            this.indexesCollection = options.indexesCollection;
            this.documentsCollection = options.documentsCollection;

            if(!this.stateTokenMode()) {
                this.promotionsCollection = new PromotionsCollection();
            }

            this.sortView = new SortView({
                queryModel: this.queryModel
            });

            this.resultsNumberView = new ResultsNumberView({
                documentsCollection: this.documentsCollection
            });

            this.infiniteScroll = _.debounce(infiniteScroll, 500, true);
        },

        normalRefreshResults: function() {
            if (this.queryModel.get('queryText')) {
                if (!_.isEmpty(this.queryModel.get('indexes'))) {
                    this.endOfResults = false;
                    this.start = 1;
                    this.maxResults = SCROLL_INCREMENT;
                    this.loadData(false);
                    this.$('.main-results-content .promotions').empty();

                    this.$loadingSpinner.removeClass('hide');
                    this.toggleError(false);
                    this.$('.main-results-content .error .error-list').empty();
                    this.$('.main-results-content .results').empty();
                } else {
                    this.$loadingSpinner.addClass('hide');
                    this.$('.main-results-content .results').html(this.messageTemplate({message: i18n_indexes['search.error.noIndexes']}));
                }
            }
        },

        stateTokenRefreshResults: function() {
            this.endOfResults = false;
            this.start = 1;
            this.maxResults = SCROLL_INCREMENT;
            this.loadData(false);

            this.$loadingSpinner.removeClass('hide');
            this.toggleError(false);
            this.$('.main-results-content .error .error-list').empty();
            this.$('.main-results-content .results').empty();
        },

        normalClearLoadingSpinner: function() {
            if (this.resultsFinished && this.promotionsFinished) {
                this.$loadingSpinner.addClass('hide');
            }
        },

        stateTokenClearLoadingSpinner: function() {
            if (this.resultsFinished) {
                this.$loadingSpinner.addClass('hide');
            }
        },

        render: function() {
            this.$el.html(this.template({i18n:i18n}));

            if(!this.isComparisonView) {
                this.$('.main-results-content').addClass('preview-mode');
            }

            this.$loadingSpinner = $(this.loadingTemplate);

            this.$el.find('.results').after(this.$loadingSpinner);

            this.sortView.setElement(this.$('.sort-container')).render();
            this.resultsNumberView.setElement(this.$('.results-number-container')).render();

            if(!this.stateTokenMode()) {
                /*promotions content content*/
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
            }

            /*main results content*/
            this.listenTo(this.documentsCollection, 'add', function(model) {
                this.formatResult(model, false);
            });

            this.listenTo(this.documentsCollection, 'sync', function() {
                this.resultsFinished = true;
                this.clearLoadingSpinner();

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

            this.listenTo(this.queryModel, 'change', this.refreshResults);

            this.listenTo(this.entityCollection, 'reset', function() {
                if (!this.entityCollection.isEmpty()) {
                    this.documentsCollection.each(function(document) {
                        var summary = addLinksToSummary(this.entityCollection, document.get('summary'));

                        this.$('[data-reference="' + document.get('reference') + '"] .result-summary').html(summary);
                    }, this);

                    if(!this.stateTokenMode()) {
                        this.promotionsCollection.each(function(document) {
                            var summary = addLinksToSummary(this.entityCollection, document.get('summary'));

                            this.$('[data-reference="' + document.get('reference') + '"] .result-summary').html(summary);
                        }, this);
                    }
                }
            });

            // Do not bind here since the same function must be passed to the off method
            $('.main-content').scroll(this.checkScroll);

            /*colorbox fancy button override*/
            $('#colorbox').append(_.template(colorboxControlsTemplate));
            $('.nextBtn').on('click', this.handleNextResult);
            $('.prevBtn').on('click', this.handlePrevResult);

            if (this.documentsCollection.isEmpty()) {
                this.refreshResults();
            }
        },

        handlePrevResult: function() {
            $.colorbox.prev();
        },

        handleNextResult: function() {
            $.colorbox.next();
        },

        colorboxArguments: function(options) {
            var args = {
                current: '{current} of {total}',
                height: '70%',
                iframe: false,
                rel: 'results',
                width: '70%',
                onClosed: function() {
                    $window.off('resize', onResize);
                },
                onComplete: _.bind(function() {
                    $('#cboxPrevious, #cboxNext').remove(); //removing default colorbox nav buttons

                    var $viewServerPage = $('.view-server-page');

                    $viewServerPage.on('load', function() {
                        $('.view-server-loading-indicator').addClass('hidden');
                        $('.view-server-page').removeClass('hidden');
                    });

                    // Adding the source attribute after the colorbox has loaded prevents the iframe from loading
                    // a very quick response (such as an error) before the listener is attached
                    $viewServerPage.attr("src", options.href);

                    $window.resize(onResize);
                }, this)
            };

            if (options.model.isMedia()) {
                args.html = this.mediaPlayerTemplate({
                    i18n: i18n,
                    model: options.model
                });
            } else {
                args.html = this.viewDocumentTemplate({
                    src: options.href,
                    i18n: i18n,
                    model: options.model
                });
            }

            return args;
        },

        formatResult: function(model, isPromotion) {
            var reference = model.get('reference');
            var summary = addLinksToSummary(this.entityCollection, model.get('summary'));

            var href;

            if (model.get('promotionType') === 'STATIC_CONTENT_PROMOTION') {
                href = viewClient.getStaticContentPromotionHref(reference);
            } else {
                href = viewClient.getHref(reference, model);
            }

            var $newResult = $(this.resultsTemplate({
                i18n: i18n,
                title: model.get('title'),
                reference: reference,
                href: href,
                summary: summary,
                promotion: isPromotion,
                date: model.has('date') ? model.get('date').fromNow() : null,
                contentType: getContentTypeClass(model),
                thumbnail: model.get('thumbnail')
            }));

            if (isPromotion) {
                this.$('.main-results-content .promotions').append($newResult);
            } else {
                this.$('.main-results-content .results').append($newResult);
            }

            var colorboxArgs = this.colorboxArguments({model: model, href: href});
            var $previewTrigger = $newResult.find('.preview-documents-trigger');
            var $resultHeader = $newResult.find('.result-header');

            $previewTrigger.colorbox(colorboxArgs);

            // web documents should open the original document in a new tab
            if (model.isWebType()) {
                $resultHeader.attr({
                    href: reference,
                    target: "_blank"
                });
            } else {
                $resultHeader.click(function(e) {
                    e.preventDefault();

                    $previewTrigger.colorbox(_.extend({open: true}, colorboxArgs));
                });
            }

            var $similarDocumentsTrigger = $newResult.find('.similar-documents-trigger');
            popover($similarDocumentsTrigger, 'focus', this.handlePopover);

            //prevent preview mode opening when clicking similar documents
            $similarDocumentsTrigger.on('click', function(e) {
                e.stopPropagation();
            });
        },

        handlePopover: function($content, $target) {
            var collection = new SimilarDocumentsCollection([], {
                indexes: this.queryModel.get('indexes'),
                reference: $target.closest('[data-reference]').attr('data-reference')
            });

            collection.fetch({
                error: _.bind(function() {
                    $content.html(this.popoverMessageTemplate({message: i18n['search.similarDocuments.error']}));
                }, this),
                success: _.bind(function() {
                    if (collection.isEmpty()) {
                        $content.html(this.popoverMessageTemplate({message: i18n['search.similarDocuments.none']}));
                    } else {
                        $content.html('<ul class="list-unstyled"></ul>');
                        _.each(collection.models, function(model) {
                            var listItem = $(this.popoverTemplate({
                                title: model.get('title'),
                                summary: model.get('summary').trim().substring(0, 100) + '...'
                            }));
                            var reference = model.get('reference');
                            var href;
                            if (model.get('promotionType') === 'STATIC_CONTENT_PROMOTION') {
                                href = viewClient.getStaticContentPromotionHref(reference);
                            } else {
                                href = viewClient.getHref(reference, model);
                            }
                            $(listItem).find('a').colorbox(this.colorboxArguments({model: model, href: href}));
                            $content.find('ul').append(listItem);
                        }, this);
                    }
                }, this)
            });
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

        normalLoadData: function (infiniteScroll) {
            this.$loadingSpinner.removeClass('hide');
            this.resultsFinished = false;

            this.documentsCollection.fetch({
                data: {
                    auto_correct: infiniteScroll ? false : this.queryModel.get('autoCorrect'),
                    text: this.queryModel.get('queryText'),
                    start: this.start,
                    max_results: this.maxResults,
                    summary: 'context',
                    indexes: this.queryModel.get('indexes'),
                    field_text: this.queryModel.get('fieldText'),
                    min_date: this.queryModel.getIsoDate('minDate'),
                    max_date: this.queryModel.getIsoDate('maxDate'),
                    sort: this.queryModel.get('sort')
                },
                reset: false,
                remove: !infiniteScroll
            }, this);

            if (!infiniteScroll) {
                this.promotionsFinished = false;
                this.promotionsCollection.fetch({
                    data: {
                        auto_correct: this.queryModel.get('autoCorrect'),
                        text: this.queryModel.get('queryText'),
                        start: this.start,
                        max_results: this.maxResults,
                        summary: 'context',
                        indexes: this.queryModel.get('indexes'),
                        field_text: this.queryModel.get('fieldText'),
                        min_date: this.queryModel.getIsoDate('minDate'),
                        max_date: this.queryModel.getIsoDate('maxDate'),
                        sort: this.queryModel.get('sort')
                    },
                    reset: false
                }, this);
            }
        },

        stateTokenLoadData: function(infiniteScroll) {
            this.$loadingSpinner.removeClass('hide');
            this.resultsFinished = false;

            this.documentsCollection.fetch({
                data: {
                    start: this.start,
                    max_results: this.maxResults,
                    summary: 'context',
                    sort: this.queryModel.get('sort')
                },
                reset: false,
                remove: !infiniteScroll
            }, this);
        },

        normalCheckScroll: function() {
            var triggerPoint = 500;

            if (this.documentsCollection.size() > 0 && this.queryModel.get('queryText') && this.resultsFinished && this.el.scrollHeight + this.$el.offset().top - $(window).height() < triggerPoint) {
                this.infiniteScroll();
            }
        },

        stateTokenCheckScroll: function() {
            if (this.documentsCollection.size() > 0) {
                this.infiniteScroll();
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
        },

        stateTokenMode: function() {
            return this.mode === Mode.STATE_TOKEN;
        }
    }, {
        Mode: Mode
    });

});
