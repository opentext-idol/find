/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'js-whatever/js/model-any-changed-attribute-listener',
    'find/app/vent',
    'find/app/model/document-model',
    'find/app/model/promotions-collection',
    'find/app/page/search/sort-view',
    'find/app/page/search/results/results-number-view',
    'find/app/util/view-server-client',
    'find/app/util/events',
    'find/app/page/search/results/add-links-to-summary',
    'find/app/configuration',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/search-result-container.html',
    'text!find/templates/app/page/search/results/results-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'moment',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes'
], function(_, $, Backbone, addChangeListener, vent, DocumentModel, PromotionsCollection, SortView, ResultsNumberView,
            viewClient, events, addLinksToSummary, configuration, generateErrorHtml, resultTemplate, html,
            loadingSpinnerTemplate, moment, i18n, i18n_indexes) {
    'use strict';

    const SCROLL_INCREMENT = 30;
    const INFINITE_SCROLL_POSITION_PIXELS = 500;

    function infiniteScroll() {
        const resultsPresent = this.documentsCollection.size() > 0 && this.fetchStrategy.validateQuery(this.queryModel);

        if (resultsPresent && this.loadingTracker.resultsFinished && !this.endOfResults) {
            this.start = this.maxResults + 1;
            this.maxResults += SCROLL_INCREMENT;

            this.loadData(true);

            events().page(this.maxResults / SCROLL_INCREMENT);
        }
    }

    return Backbone.View.extend({
        // Overridden for HoD and IDOL implementations
        getQuestionsViewConstructor: _.constant(null),

        loadingHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),
        messageTemplate: _.template('<div class="result-message span10"><%-message%></div>'),
        resultTemplate: _.template(resultTemplate),

        events: {
            'click .preview-mode [data-cid]:not(.answered-question)': function(e) {
                if (String(window.getSelection()).length >= 2) {
                    // If the user is partway selecting text for selection-entity-search, we suppress the click,
                    //   otherwise the preview pane will toggle every time you try and select something.
                    return;
                }

                const $target = $(e.target);

                if (!$target.is('a')) {
                    const $result = $(e.currentTarget).closest('.main-results-container');

                    if ($result.hasClass('selected-document')) {
                        // disable preview mode
                        this.previewModeModel.set({document: null});
                    } else {
                        // enable/choose another preview view
                        const cid = $result.data('cid');
                        const isPromotion = $result.closest('.main-results-list').hasClass('promotions');
                        const collection = isPromotion ? this.promotionsCollection : this.documentsCollection;
                        const model = collection.get(cid);
                        this.previewModeModel.set({document: model});

                        if (!isPromotion) {
                            events().preview(collection.indexOf(model) + 1);
                        }
                    }
                }
            },
            'click .document-detail-mode [data-cid]': function(e) {
                if (String(window.getSelection()).length >= 2) {
                    // If the user is partway selecting text for selection-entity-search, we suppress the click.
                    return;
                }

                const $target = $(e.currentTarget);
                const cid = $target.data('cid');
                const isPromotion = $target.closest('.main-results-list').hasClass('promotions');
                const collection = isPromotion ? this.promotionsCollection : this.documentsCollection;
                const model = collection.get(cid);
                vent.navigateToDetailRoute(model);
            }
        },

        initialize: function(options) {
            this.fetchStrategy = options.fetchStrategy;
            this.documentRenderer = options.documentRenderer;

            this.queryModel = options.queryModel;
            this.showPromotions = this.fetchStrategy.promotions(this.queryModel) && !options.hidePromotions;
            this.documentsCollection = options.documentsCollection;

            this.indexesCollection = options.indexesCollection;
            this.scrollModel = options.scrollModel;

            this.loadingTracker = {
                resultsFinished: true,
                promotionsFinished: true,
                questionsFinished: true
            };

            // Preview mode is enabled when a preview mode model is provided
            this.previewModeModel = options.previewModeModel;

            if (this.indexesCollection) {
                this.selectedIndexesCollection = options.queryState.selectedIndexes;
            }

            if (this.showPromotions) {
                this.promotionsCollection = new PromotionsCollection();
            }

            if (this.fetchStrategy.answers(this.queryModel)) {
                const QuestionsView = this.getQuestionsViewConstructor();

                if (QuestionsView) {
                    this.questionsView = new QuestionsView({
                        queryModel: this.queryModel,
                        loadingTracker: this.loadingTracker,
                        clearLoadingSpinner: _.bind(this.clearLoadingSpinner, this)
                    });
                }
            }

            this.sortView = new SortView({
                queryModel: this.queryModel
            });

            this.resultsNumberView = new ResultsNumberView({
                documentsCollection: this.documentsCollection
            });

            addChangeListener(this,
                this.queryModel,
                ['sort', 'autoCorrect'].concat(this.fetchStrategy.queryModelAttributes),
                this.refreshResults);

            this.infiniteScroll = _.debounce(infiniteScroll, 500, true);

            this.listenTo(this.scrollModel, 'change', function() {
                if (this.$el.is(':visible') && this.scrollModel.get('scrollTop') > this.scrollModel.get('scrollHeight') - INFINITE_SCROLL_POSITION_PIXELS - this.scrollModel.get('innerHeight')) {
                    this.infiniteScroll();
                }
            });

            if (this.previewModeModel) {
                this.listenTo(this.previewModeModel, 'change:document', this.updateSelectedDocument);
            }
        },

        render: function() {
            this.$el.html(html);

            this.$loadingSpinner = this.$('.results-view-loading')
                .html(this.loadingHtml);

            this.sortView.setElement(this.$('.sort-container')).render();
            this.resultsNumberView.setElement(this.$('.results-number-container')).render();

            if (this.questionsView) {
                this.questionsView.setElement(this.$('.main-results-content .answered-questions')).render();
            }

            if (this.showPromotions) {
                this.listenTo(this.promotionsCollection, 'add', function(model) {
                    if (this.documentRenderer.loadPromise.state() === 'resolved') {
                        this.formatResult(model, true);
                    }
                });

                this.listenTo(this.promotionsCollection, 'sync', function() {
                    this.loadingTracker.promotionsFinished = true;
                    this.clearLoadingSpinner();
                });

                // TODO: We're basically ignoring promotions errors here -- implement robust logging procedure.
                // The Find User shouldn't hear about promotions, but the way we are doing it now, the DataAdmin or
                // SysAdmin may never find out that promotions-related errors are affecting Users' searches.
                this.listenTo(this.promotionsCollection, 'error', function(collection, xhr) {
                    if (xhr.statusText !== 'abort') {
                        this.loadingTracker.promotionsFinished = true;
                        this.clearLoadingSpinner();
                    }
                });
            }

            this.listenTo(this.documentsCollection, 'add', function(model) {
                if (this.documentRenderer.loadPromise.state() === 'resolved') {
                    this.formatResult(model, false);
                }
            });

            this.listenTo(this.documentsCollection, 'sync reset', function() {
                this.loadingTracker.resultsFinished = true;
                this.clearLoadingSpinner();

                this.endOfResults = this.maxResults >= this.documentsCollection.totalResults;

                if (this.endOfResults && !this.documentsCollection.isEmpty()) {
                    this.$('.main-results-content .results')
                        .append(this.messageTemplate({message: i18n["search.noMoreResults"]}));
                } else if (this.documentsCollection.isEmpty()) {
                    this.$('.main-results-content .results')
                        .append(this.messageTemplate({message: i18n["search.noResults"]}));
                }
            });

            this.listenTo(this.documentsCollection, 'error', function(collection, xhr) {
                if (xhr.statusText !== 'abort') {
                    this.loadingTracker.resultsFinished = true;
                    this.clearLoadingSpinner();
                    this.handleError(xhr);
                }
            });

            if (this.indexesCollection) {
                this.indexesCollection.currentRequest
                    .always(function() {
                        this.refreshResults();
                    }.bind(this));
            } else {
                this.refreshResults();
            }

            if (this.entityCollection) {
                this.updateEntityHighlighting();
            }

            if (this.previewModeModel) {
                this.$('.main-results-content').addClass('preview-mode');
                this.updateSelectedDocument();
            } else {
                this.$('.main-results-content').addClass('document-detail-mode');
            }

            this.documentRenderer.loadPromise
                .done(function() {
                    this.documentsCollection.each(function(model) {
                        this.formatResult(model, false);
                    }.bind(this));

                    if (this.showPromotions) {
                        this.promotionsCollection.each(function(model) {
                            this.formatResult(model, true);
                        }.bind(this));
                    }
                }.bind(this))
                .fail(this.handleError.bind(this))
                .always(this.clearLoadingSpinner.bind(this));
        },

        refreshResults: function() {
            if (this.fetchStrategy.validateQuery(this.queryModel) && this.documentRenderer.loadPromise.state() !== 'rejected') {
                if (this.fetchStrategy.waitForIndexes(this.queryModel)) {
                    this.$loadingSpinner.addClass('hide');
                    this.$('.main-results-content .results')
                        .html(this.messageTemplate({message: i18n_indexes['search.error.noIndexes']}));
                } else {
                    this.endOfResults = false;
                    this.start = 1;
                    this.maxResults = SCROLL_INCREMENT;
                    this.loadData(false);
                    this.$('.main-results-content .promotions').empty();

                    if (this.$loadingSpinner) {
                        this.$loadingSpinner.removeClass('hide');
                    }
                    this.toggleError(false);
                    this.$('.main-results-content .results').empty();
                }
            }
        },

        clearLoadingSpinner: function() {
            const notLoading = this.documentRenderer.loadPromise.state() !== 'pending' &&
                this.loadingTracker.resultsFinished && this.loadingTracker.questionsFinished
                && this.loadingTracker.promotionsFinished || !this.showPromotions;

            if (notLoading) {
                this.$loadingSpinner.addClass('hide');
            }
        },

        updateSelectedDocument: function() {
            const documentModel = this.previewModeModel.get('document');
            this.$('.main-results-container').removeClass('selected-document');

            if (documentModel !== null) {
                this.$('.main-results-container[data-cid="' + documentModel.cid + '"]').addClass('selected-document');
            }
        },

        formatResult: function(model, isPromotion) {
            const resultHtml = isPromotion
                ? this.documentRenderer.renderPromotion(model)
                : this.documentRenderer.renderResult(model);

            const $el = isPromotion
                ? this.$('.main-results-content .promotions')
                : this.$('.main-results-content .results');

            $el.append(this.resultTemplate({
                cid: model.cid,
                content: resultHtml
            }));
        },

        generateErrorMessage: function(xhr) {
            if (xhr.responseJSON) {
                return generateErrorHtml({
                    errorDetails: xhr.responseJSON.message,
                    errorUUID: xhr.responseJSON.uuid,
                    errorLookup: xhr.responseJSON.backendErrorCode,
                    isUserError: xhr.responseJSON.isUserError
                });
            } else {
                return generateErrorHtml();
            }
        },

        handleError: function(xhr) {
            this.toggleError(true);
            this.$('.main-results-content .results-view-error')
                .html(this.generateErrorMessage(xhr));
        },

        toggleError: function(on) {
            this.$('.main-results-content .promotions').toggleClass('hide', on);
            this.$('.main-results-content .results').toggleClass('hide', on);
            this.$('.main-results-content .results-view-error').toggleClass('hide', !on);

            if (this.questionsView) {
                this.questionsView.$el.toggleClass('hide', on);
            }
        },

        loadData: function(infiniteScroll) {
            if (this.$loadingSpinner) {
                this.$loadingSpinner.removeClass('hide');
            }

            if (this.questionsView && !infiniteScroll) {
                this.questionsView.fetchData();
            }

            this.loadingTracker.resultsFinished = false;

            const requestData = _.extend({
                start: this.start,
                max_results: this.maxResults,
                sort: this.queryModel.get('sort'),
                auto_correct: this.queryModel.get('autoCorrect'),
                queryType: 'MODIFIED'
            }, this.fetchStrategy.requestParams(this.queryModel, infiniteScroll));

            if (!infiniteScroll) {
                this.documentsCollection.reset();
            }

            this.documentsCollection.fetch({
                data: requestData,
                reset: false,
                remove: !infiniteScroll,
                error: function(collection, xhr) {
                    // if returns an error remove previous models from documentsCollection
                    if (collection && xhr.statusText !== 'abort') {
                        collection.reset();
                    }
                },
                success: function() {
                    if (this.indexesCollection && this.documentsCollection.warnings && this.documentsCollection.warnings.invalidDatabases) {
                        // Invalid databases have been deleted from IDOL; mark them as such in the indexes collection
                        this.documentsCollection.warnings.invalidDatabases.forEach(function(name) {
                            const indexModel = this.indexesCollection.findWhere({name: name});

                            if (indexModel) {
                                indexModel.set('deleted', true);
                            }

                            this.selectedIndexesCollection.remove({name: name});
                        }.bind(this));
                    }
                }.bind(this)
            });

            if (!infiniteScroll && this.showPromotions) {
                this.loadingTracker.promotionsFinished = false;

                const promotionsRequestData = _.extend({
                    start: this.start,
                    max_results: this.maxResults,
                    sort: this.queryModel.get('sort'),
                    queryType: 'PROMOTIONS'
                }, this.fetchStrategy.promotionsRequestParams(this.queryModel, infiniteScroll));

                this.promotionsCollection.fetch({
                    data: promotionsRequestData,
                    reset: false
                }, this);

                // we're not scrolling, so should be a new search
                events().reset(requestData.text);
            }
        },

        remove: function() {
            this.sortView.remove();
            this.resultsNumberView.remove();

            if (this.questionsView) {
                this.questionsView.remove();
            }

            Backbone.View.prototype.remove.call(this);
        }
    });
});
