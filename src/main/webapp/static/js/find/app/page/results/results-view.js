define([
    'backbone',
    'jquery',
    'find/app/model/document-model',
    'find/app/model/documents-collection',
    'find/app/model/promotions-collection',
    'find/app/model/similar-documents-collection',
    'find/app/util/popover',
    'find/app/util/view-server-client',
    'find/app/util/document-mime-types',
    'js-whatever/js/escape-regex',
    'text!find/templates/app/page/results-popover.html',
    'text!find/templates/app/page/popover-message.html',
    'text!find/templates/app/page/results/results-view.html',
    'text!find/templates/app/page/results-container.html',
    'text!find/templates/app/page/colorbox-controls.html',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/view/media-player.html',
    'text!find/templates/app/page/view/view-document.html',
    'text!find/templates/app/page/results/entity-label.html',
    'moment',
    'i18n!find/nls/bundle',
    'colorbox'
], function(Backbone, $, DocumentModel, DocumentsCollection, PromotionsCollection, SimilarDocumentsCollection, popover,
            viewClient, documentMimeTypes, escapeRegex, popoverTemplate, popoverMessageTemplate, template, resultsTemplate,
            colorboxControlsTemplate, loadingSpinnerTemplate, mediaPlayerTemplate, viewDocumentTemplate, entityTemplate,
            moment, i18n) {

    /** Whitespace OR character in set bounded by [] */
    var boundaryChars = '\\s|[,.-:;?\'"!\\(\\)\\[\\]{}]';
    /** Start of input OR boundary chars */
    var startRegex = '(^|' + boundaryChars + ')';
    /** End of input OR boundary chars */
    var endRegex = '($|' + boundaryChars + ')';

    var mediaTypes = ['audio', 'video'];

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

    var onResize = function() {
        $.colorbox.resize({width: SIZE, height: SIZE});
    };

    return Backbone.View.extend({
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),
        resultsTemplate: _.template(resultsTemplate),
        popoverMessageTemplate: _.template(popoverMessageTemplate),
        messageTemplate: _.template('<div class="result-message span10"><%-message%> </div>'),
        mediaPlayerTemplate: _.template(mediaPlayerTemplate),
        popoverTemplate: _.template(popoverTemplate),
        entityTemplate: _.template(entityTemplate),
        viewDocumentTemplate: _.template(viewDocumentTemplate),

        events: {
            'click .query-text': function(e) {
                var $target = $(e.target);
                var queryText = $target.attr('data-title');
                this.queryModel.set('queryText', queryText);
            },
            'mouseover .entity-to-summary': function(e) {
                var title = $(e.currentTarget).find('a').html();
                this.$('[data-title="' + title + '"]').addClass('label label-primary entity-to-summary').removeClass('label-info');
            },
            'mouseleave .entity-to-summary': function() {
                this.$('.suggestions-content li a').removeClass('label label-primary entity-to-summary');
                this.$('.main-results-content .entity-to-summary').removeClass('label-primary').addClass('label-info');
            }
        },

        initialize: function(options) {
            _.bindAll(this, 'handlePopover');

            this.queryModel = options.queryModel;
            this.entityCollection = options.entityCollection;
            this.indexesCollection = options.indexesCollection;

            this.documentsCollection = new DocumentsCollection();
            this.promotionsCollection = new PromotionsCollection();

            this.listenTo(this.queryModel, 'change refresh', function() {
                if (!_.isEmpty(this.queryModel.get('indexes'))) {
                    this.documentsCollection.fetch({
                        data: {
                            text: this.queryModel.get('queryText'),
                            max_results: 30,
                            summary: 'context',
                            index: this.queryModel.get('indexes'),
                            field_text: this.queryModel.get('fieldText'),
                            min_date: this.queryModel.getIsoDate('minDate'),
                            max_date: this.queryModel.getIsoDate('maxDate'),
                            sort: this.queryModel.get('sort')
                        },
                        reset: false
                    }, this);

                    // TODO: Move out of if statement when HOD allows fetching promotions without query text
                    this.promotionsCollection.fetch({
                        data: {
                            text: this.queryModel.get('queryText'),
                            max_results: 30, // TODO maybe less?
                            summary: 'context',
                            index: this.queryModel.get('indexes'),
                            field_text: this.queryModel.get('fieldText'),
                            min_date: this.queryModel.getIsoDate('minDate'),
                            max_date: this.queryModel.getIsoDate('maxDate'),
                            sort: this.queryModel.get('sort')
                        },
                        reset: false
                    }, this);
                }
            });
        },

        clearLoadingSpinner: function() {
            if (this.resultsFinished && this.promotionsFinished) {
                this.$loadingSpinner.addClass('hide');
            }
        },

        render: function() {
            this.$el.html(template);

            this.$loadingSpinner = $(this.loadingTemplate);

            this.$el.prepend(this.$loadingSpinner);

            /*promotions content content*/
            this.listenTo(this.promotionsCollection, 'add', function(model) {
                this.formatResult(model, true);
            });

            this.listenTo(this.promotionsCollection, 'request', function() {
                this.promotionsFinished = false;
                this.$('.main-results-content .promotions').empty();
            });

            this.listenTo(this.promotionsCollection, 'sync', function() {
                this.promotionsFinished = true;
                this.clearLoadingSpinner();
            });

            this.listenTo(this.promotionsCollection, 'error', function(collection, xhr) {
                this.promotionsFinished = true;
                this.clearLoadingSpinner();

                this.$('.main-results-content .promotions').append(this.errorMessage(i18n["search.error.promotions"], xhr));
            });

            /*main results content*/
            this.listenTo(this.documentsCollection, 'request', function() {
                this.resultsFinished = false;
                this.$loadingSpinner.removeClass('hide');
                this.$('.main-results-content .results').empty();
            });

            this.listenTo(this.documentsCollection, 'add', function(model) {
                this.formatResult(model, false);
            });

            this.listenTo(this.documentsCollection, 'sync', function() {
                this.resultsFinished = true;
                this.clearLoadingSpinner();

                if (this.documentsCollection.isEmpty()) {
                    this.$('.main-results-content .results').append(this.messageTemplate({message: i18n["search.noResults"]}));
                }
            });

            this.listenTo(this.documentsCollection, 'error', function(collection, xhr) {
                this.resultsFinished = true;
                this.clearLoadingSpinner();

                this.$('.main-results-content .results').append(this.errorMessage(i18n["search.error.results"], xhr));
            });

            this.listenTo(this.entityCollection, 'reset', function() {
                if (!this.entityCollection.isEmpty()) {
                    this.documentsCollection.each(function(document) {
                        var summary = this.addLinksToSummary(document.get('summary'));

                        this.$('[data-reference="' + document.get('reference') + '"] .result-summary').html(summary);
                    }, this);

                    this.promotionsCollection.each(function(document) {
                        var summary = this.addLinksToSummary(document.get('summary'));

                        this.$('[data-reference="' + document.get('reference') + '"] .result-summary').html(summary);
                    }, this);
                }
            });

            /*colorbox fancy button override*/
            $('#colorbox').append(_.template(colorboxControlsTemplate));
            $('.nextBtn').on('click', this.handleNextResult);
            $('.prevBtn').on('click', this.handlePrevResult);
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

                    $window.resize(onResize);
                }, this)
            };

            if (options.media) {
                args.html = this.mediaPlayerTemplate({
                    media: options.media,
                    url: options.url,
                    offset: options.offset
                });
            } else {
                args.html = this.viewDocumentTemplate({
                    src: options.href,
                    i18n: i18n,
                    model: options.model,
                    arrayFields: DocumentModel.ARRAY_FIELDS,
                    dateFields: DocumentModel.DATE_FIELDS,
                    fields: ['domain', 'index', 'reference']
                });
            }

            return args;
        },

        formatResult: function(model, isPromotion) {
            var reference = model.get('reference');
            var summary = this.addLinksToSummary(model.get('summary'));

            var href;

            if (model.get('promotionType') === 'STATIC_CONTENT_PROMOTION') {
                href = viewClient.getStaticContentPromotionHref(reference);
            } else {
                href = viewClient.getHref(reference, model.get('index'), model.get('domain'));
            }

            var $newResult = $(this.resultsTemplate({
                i18n: i18n,
                title: model.get('title'),
                reference: reference,
                href: href,
                summary: summary,
                promotion: isPromotion,
                date: model.has('date') ? model.get('date').format('YYYY/MM/DD HH:mm:ss') : null,
                contentType: getContentTypeClass(model)
            }));

            if (isPromotion) {
                this.$('.main-results-content .promotions').append($newResult);
            } else {
                this.$('.main-results-content .results').append($newResult);
            }

            var contentType = model.get('contentType') || '';

            var media = _.find(mediaTypes, function(mediaType) {
                return contentType.indexOf(mediaType) === 0;
            });

            var url = model.get('url');
            var colorboxOptions;

            if (media && url) {
                // This is a multimedia file
                colorboxOptions = {
                    media: media,
                    url: url,
                    offset: model.get('offset')
                };
            } else {
                // Use the standard display
                colorboxOptions = {model: model, href: href};
            }

            $newResult.find('.result-header').colorbox(this.colorboxArguments(colorboxOptions));

            $newResult.find('.dots').click(function (e) {
                e.preventDefault();
                $newResult.find('.result-header').trigger('click'); //dot-dot-dot triggers the colorbox event
            });

            popover($newResult.find('.similar-documents-trigger'), this.handlePopover);
        },

        addLinksToSummary: function(summary) {
            // Protect us from XSS
            summary = _.escape(summary);

            // Process the search text first
            var searchText = this.queryModel.get("queryText");
            var searchTextID = _.uniqueId('Find-IOD-QueryText-Placeholder');
            summary = this.replaceBoundedText(summary, searchText, searchTextID);

            // Create an array of the entity titles, longest first
            var entities = this.entityCollection.map(function(entity) {
                return {
                    text: entity.get('text'),
                    id: _.uniqueId('Find-IOD-Entity-Placeholder')
                };
            }).sort(function(a, b) {
                return b.text.length - a.text.length;
            });

            // Loop through entities, replacing each with a unique id to prevent later replaces finding what we've
            // changed here and messing things up badly
            _.each(entities, function(entity) {
                summary = this.replaceBoundedText(summary, entity.text, entity.id);
            }, this);

            // Loop through entities again, replacing text with labels
            _.each(entities, function(entity) {
                summary = this.replaceTextWithLabel(summary, entity.id, entity.text, "entity-to-summary");
            }, this);

            // Add the search text label
            summary = this.replaceTextWithLabel(summary, searchTextID, searchText, "entity-to-summary");

            return summary;
        },

        /**
         * Finds a string that's bounded by [some regex stuff] and replaces it with something else.
         * Used as part 1 of highlighting text in result summaries.
         * @param text  The text to search in
         * @param textToFind  The text to search for
         * @param replacement  What to replace textToFind with
         * @returns {string|XML|void}  `text`, but with replacements made
         */
        replaceBoundedText: function(text, textToFind, replacement) {
            return text.replace(new RegExp(startRegex + escapeRegex(textToFind) + endRegex, 'gi'), '$1' + replacement + '$2');
        },

        /**
         * Finds a string and replaces it with an HTML label.
         * Used as part 2 of highlighting text in results summaries.
         * @param text  The text to search in
         * @param textToFind  The text to replace with a label
         * @param replacement  The term or phrase to display in the label
         * @param labelClasses Classes to add to the label
         * @returns {string|XML|*}  `text`, but with replacements made
         */
        replaceTextWithLabel: function(text, textToFind, replacement, labelClasses) {
            var label = this.entityTemplate({
                replacement: replacement,
                labelClasses: labelClasses
            });

            return text.replace(new RegExp(startRegex + textToFind + endRegex, 'g'), '$1' + label + '$2');
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
                        $content.html(this.popoverTemplate({collection: collection}));
                    }
                }, this)
            });
        },

        errorMessage: function(serviceErrorMessage, xhr) {
            var messageTemplate;

            if (xhr.responseJSON && i18n["hod.error." + xhr.responseJSON.hodErrorCode]) {
                messageTemplate = this.messageTemplate({message: serviceErrorMessage + ': ' + i18n["hod.error." + xhr.responseJSON.hodErrorCode]});
            } else {
                messageTemplate = this.messageTemplate({message: serviceErrorMessage});
            }

            return messageTemplate;
        }
    });
});
