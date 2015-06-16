define([
    'backbone',
    'find/app/model/documents-collection',
    'find/app/model/promotions-collection',
    'find/app/util/view-server-client',
    'text!find/templates/app/page/results/results-view.html',
    'text!find/templates/app/page/results-container.html',
    'text!find/templates/app/page/colorbox-controls.html',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/view/audio-player.html',
    'moment',
    'colorbox'
], function(Backbone, DocumentsCollection, PromotionsCollection, viewClient, resultsView, resultsTemplate, colorboxControlsTemplate, loadingSpinnerTemplate, audioPlayerTemplate, moment) {

    return Backbone.View.extend({

        template: _.template(resultsView),
        noResultsTemplate: _.template('<div class="no-results span10"><%- i18n["search.noResults"] %> </div>'),
        audioPlayerTemplate: _.template(audioPlayerTemplate),

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
                            summary: 'quick',
                            index: this.queryModel.get('indexes'),
                            field_text: this.queryModel.getFieldTextString(),
                            min_date: this.queryModel.get('minDate'),
                            max_date: this.queryModel.get('maxDate')
                        }
                    }, this);
                }

                this.promotionsCollection.fetch({
                    data: {
                        text: this.queryModel.get('queryText'),
                        max_results: 30, // TODO maybe less?
                        summary: 'quick',
                        index: this.queryModel.get('indexes'),
                        field_text: this.queryModel.getFieldTextString(),
                        min_date: this.queryModel.get('minDate'),
                        max_date: this.queryModel.get('maxDate')
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
            if (fields.content_type && fields.url && fields.content_type[0].indexOf('audio') == 0) {
                // This is an audio file with a URL, use the audio player template

                var url = fields.url[0];
                var offset = fields.offset ? fields.offset[0] : 0;

                $newResult.find('.result-header').colorbox({
                    iframe: false,
                    width:'70%',
                    height:'70%',
                    rel: 'results',
                    current: '{current} of {total}',
                    onComplete: _.bind(function() {
                        $('#cboxPrevious, #cboxNext').remove(); //removing default colorbox nav buttons
                    }, this),
                    html: this.audioPlayerTemplate({
                        url: url,
                        offset: offset
                    })
                })

            } else {
                // Use the standard Viewserver display
                $newResult.find('.result-header').colorbox({
                    iframe: true,
                    width:'70%',
                    height:'70%',
                    rel: 'results',
                    current: '{current} of {total}',
                    onComplete: _.bind(function() {
                        $('#cboxPrevious, #cboxNext').remove(); //removing default colorbox nav buttons
                    }, this)
                });
            }

            $newResult.find('.dots').click(function (e) {
                e.preventDefault();
                $newResult.find('.result-header').trigger('click'); //dot-dot-dot triggers the colorbox event
            });
        },

        addLinksToSummary: function(summary) {
            //creating an array of the entity titles, longest first
            var entities = this.entityCollection.map(function(entity) {
                return {
                    text: entity.get('text'),
                    id:  _.uniqueId('Find-IOD-Entity-Placeholder')
                }
            }).sort(function(a,b) {
                return b.text.length - a.text.length;
            });

            _.each(entities, function(entity) {
                summary = summary.replace(new RegExp('(^|\\s|[,.-:;?\'"!\\(\\)\\[\\]{}])' + entity.text + '($|\\s|[,.-:;?\'"!\\(\\)\\[\\]{}])', 'gi'), '$1' + entity.id + '$2');
            });

            _.each(entities, function(entity) {
                // TODO: use a template
                summary = summary.replace(new RegExp(entity.id, 'g'),
                    '<span class="label label-info entity-to-summary" data-title="' + entity.text +'">'
                    + '<a class="query-text" data-title="' + entity.text + '">'
                    + entity.text
                    + '</a></span>');
            });

            return summary;
        }

    })

});