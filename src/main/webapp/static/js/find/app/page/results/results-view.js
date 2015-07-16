define([
    'backbone',
    'find/app/model/documents-collection',
    'find/app/model/promotions-collection',
    'find/app/util/view-server-client',
    'text!find/templates/app/page/results/results-view.html',
    'text!find/templates/app/page/results-container.html',
    'text!find/templates/app/page/colorbox-controls.html',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/view/media-player.html',
    'text!find/templates/app/page/results/entity-label.html',
    'moment',
    'i18n!find/nls/bundle',
    'colorbox'
], function(Backbone, DocumentsCollection, PromotionsCollection, viewClient, resultsView, resultsTemplate, colorboxControlsTemplate, loadingSpinnerTemplate, mediaPlayerTemplate, entityTemplate, moment, i18n) {

    /** Whitespace OR character in set bounded by [] */
    var boundaryChars = '\\s|[,.-:;?\'"!\\(\\)\\[\\]{}]';
    /** Start of input OR boundary chars */
    var startRegex = '(^|' + boundaryChars + ')';
    /** End of input OR boundary chars */
    var endRegex = '($|' + boundaryChars + ')';

    var mediaTypes = ['audio', 'video'];

    return Backbone.View.extend({

        template: _.template(resultsView),
        noResultsTemplate: _.template('<div class="no-results span10"><%- i18n["search.noResults"] %> </div>'),
        mediaPlayerTemplate: _.template(mediaPlayerTemplate),
        entityTemplate: _.template(entityTemplate),

        events: {
            'click .query-text' : function(e) {
                var $target = $(e.target);
                var queryText = $target.attr('data-title');
                this.queryModel.set('queryText', queryText);
            },
            'mouseover .entity-to-summary': function(e) {
                var title = $(e.currentTarget).find('a').html();
                this.$('[data-title="'+ title +'"]').addClass('label label-primary entity-to-summary').removeClass('label-info');
            },
            'mouseleave .entity-to-summary': function() {
                this.$('.suggestions-content li a').removeClass('label label-primary entity-to-summary');
                this.$('.main-results-content .entity-to-summary').removeClass('label-primary').addClass('label-info');
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.entityCollection = options.entityCollection;

            this.documentsCollection = new DocumentsCollection();
            this.promotionsCollection = new PromotionsCollection();

            this.listenTo(this.queryModel, 'change', function() {
                if (!_.isEmpty(this.queryModel.get('indexes'))) {
                    this.documentsCollection.fetch({
                        data: {
                            text: this.queryModel.get('queryText'),
                            max_results: 30,
                            summary: 'context',
                            index: this.queryModel.get('indexes'),
                            field_text: this.queryModel.getFieldTextString(),
                            min_date: this.queryModel.get('minDate'),
                            max_date: this.queryModel.get('maxDate'),
                            sort: this.queryModel.get('sort')
                        }
                    }, this);
                }

                this.promotionsCollection.fetch({
                    data: {
                        text: this.queryModel.get('queryText'),
                        max_results: 30, // TODO maybe less?
                        summary: 'context',
                        index: this.queryModel.get('indexes'),
                        field_text: this.queryModel.getFieldTextString(),
                        min_date: this.queryModel.get('minDate'),
                        max_date: this.queryModel.get('maxDate'),
                        sort: this.queryModel.get('sort')
                    }
                }, this);
            });
        },

        render: function() {
            this.$el.html(this.template());

            this.listenTo(this.promotionsCollection, 'add', function(model) {
                this.formatResult(model, true)
            });

            /*main results content*/
            this.listenTo(this.documentsCollection, 'request', function () {
                this.$('.main-results-content').empty();
                this.$('.main-results-content').append(_.template(loadingSpinnerTemplate));
            });

            this.listenTo(this.documentsCollection, 'add', function (model) {
                this.formatResult(model, false);
            });

            this.listenTo(this.documentsCollection, 'sync', function () {
                if (this.documentsCollection.isEmpty()) {
                    this.$('.main-results-content .loading-spinner').remove();
                    this.$('.main-results-content').append(this.noResultsTemplate({i18n: i18n}));
                }
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

        colorboxArguments: function(media, url, offset) {
            var args = {
                current: '{current} of {total}',
                height:'70%',
                iframe: !Boolean(media),
                rel: 'results',
                width:'70%',
                onComplete: _.bind(function() {
                    $('#cboxPrevious, #cboxNext').remove(); //removing default colorbox nav buttons
                }, this)
            };

            if(media) {
                args.html = this.mediaPlayerTemplate({
                    media: media,
                    url: url,
                    offset: offset
                })
            }

            return args;
        },

        formatResult: function(model, isPromotion) {
            var reference = model.get('reference');
            var summary = model.get('summary');

            var date = null;
            var dateStamp = null;

            var dateArray = model.get('fields').date;

            if(dateArray) {
                dateStamp = dateArray[0];

                if(_.isFinite(dateStamp) && Math.floor(dateStamp) === dateStamp) {
                    date = moment(dateStamp * 1000).format("YYYY/MM/DD HH:mm:ss");
                }
            }

            // Remove existing document with this reference
            this.$("[data-reference='" + reference + "']").remove();

            summary = this.addLinksToSummary(summary);

            this.$('.main-results-content .loading-spinner').remove();

            var href = viewClient.getHref(reference, model.get('index'));

            var $newResult = $(_.template(resultsTemplate ,{
                title: model.get('title'),
                reference: reference,
                href: href,
                index: model.get('index'),
                summary: summary,
                promotion: isPromotion,
                date: date
            }));

            if (isPromotion) {
                this.$('.main-results-content').prepend($newResult);
            } else {
                this.$('.main-results-content').append($newResult);
            }

            var fields = model.get('fields');
            var contentType = fields.content_type ? fields.content_type[0] : '';

            var media = _.find(mediaTypes, function(mediaType) {
                return contentType.indexOf(mediaType) === 0;
            });

            if (media && fields.url) {
                var url = fields.url[0];
                var offset = fields.offset ? fields.offset[0] : 0;

                $newResult.find('.result-header').colorbox(this.colorboxArguments(media, url, offset));
            } else {
                // Use the standard Viewserver display
                $newResult.find('.result-header').colorbox(this.colorboxArguments());
            }

            $newResult.find('.dots').click(function (e) {
                e.preventDefault();
                $newResult.find('.result-header').trigger('click'); //dot-dot-dot triggers the colorbox event
            });
        },

        addLinksToSummary: function(summary) {
            // Process the search text first
            var searchText = this.queryModel.get("queryText");
            var searchTextID = _.uniqueId('Find-IOD-QueryText-Placeholder');
            summary = this.replaceBoundedText(summary, searchText, searchTextID)

            // Create an array of the entity titles, longest first
            var entities = this.entityCollection.map(function(entity) {
                return {
                    text: entity.get('text'),
                    id:  _.uniqueId('Find-IOD-Entity-Placeholder')
                }
            }).sort(function(a,b) {
                return b.text.length - a.text.length;
            });

            // Loop through entities, replacing each with a unique id to prevent later replaces finding what we've
            // changed here and messing things up badly
            _.each(entities, function(entity) {
                summary = this.replaceBoundedText(summary, entity.text, entity.id)
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
            return text.replace(new RegExp(startRegex + textToFind + endRegex, 'gi'), '$1' + replacement + '$2');
        },

        /**
         * Finds a string and replaces it with an HTML label.
         * Used as part 2 of highlighting text in results summaries.
         * @param text  The text to search in
         * @param textToFind  The text to replace with a label
         * @param replacement  The term or phrase to display in the label
         * @returns {string|XML|*}  `text`, but with replacements made
         */
        replaceTextWithLabel: function(text, textToFind, replacement, labelClasses) {
            var label = this.entityTemplate({
                replacement: replacement,
                labelClasses: labelClasses
            });

            return text.replace(new RegExp(startRegex + textToFind + endRegex, 'g'), '$1' + label + '$2');
        }
    })
});
