/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'find/app/model/entity-collection',
    'find/app/model/documents-collection',
    'find/app/model/promotions-collection',
    'find/app/model/indexes-collection',
    'find/app/model/parametric-collection',
    'find/app/page/parametric/parametric-controller',
    'find/app/page/date/dates-filter-view',
    'find/app/router',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'find/app/util/view-server-client',
    'moment',
    'jquery',
    'underscore',
    'text!find/templates/app/page/find-search.html',
    'text!find/templates/app/page/results-container.html',
    'text!find/templates/app/page/suggestions-container.html',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/colorbox-controls.html',
    'text!find/templates/app/page/index-popover.html',
    'text!find/templates/app/page/index-popover-contents.html',
    'text!find/templates/app/page/top-results-popover-contents.html',
    'text!find/templates/app/page/view/audio-player.html',
    'colorbox'
], function(BasePage, EntityCollection, DocumentsCollection, PromotionsCollection, IndexesCollection, ParametricCollection, ParametricController, DateView, router, vent, i18n, viewClient, moment,
            $, _, template, resultsTemplate, suggestionsTemplate, loadingSpinnerTemplate, colorboxControlsTemplate, indexPopover, indexPopoverContents, topResultsPopoverContents, audioPlayer) {

    return BasePage.extend({

        template: _.template(template),
        resultsTemplate: _.template(resultsTemplate),
        noResultsTemplate: _.template('<div class="no-results span10"><%- i18n["search.noResults"] %> </div>'),
        suggestionsTemplate: _.template(suggestionsTemplate),
        indexPopover: _.template(indexPopover),
        indexPopoverContents: _.template(indexPopoverContents),
        topResultsPopoverContents: _.template(topResultsPopoverContents),
        audioPlayerTemplate: _.template(audioPlayer),

        events: {
            'keyup .find-input': 'keyupAnimation',
            'change .indexCheckbox': function(e) {
                var toggledIndex = $(e.currentTarget).val();
                var checked = $(e.currentTarget).is(':checked');

                this.parametricController.view.clearFieldText();

                this.indexes[toggledIndex] = checked;

                if(this.$('.find-input').val()){
                    this.searchRequest(this.$('.find-input').val());
                }
            },
            'mouseover .suggestions-content a': _.debounce(function(e) {
                this.$('.suggestions-content  .popover-content').append(_.template(loadingSpinnerTemplate));

                this.topResultsCollection.fetch({
                    data: {
                        text: $(e.currentTarget).html(),
                        max_results: 3,
                        summary: 'quick',
                        index: this.selectedIndexes()
                    }
                });
            }, 800),
            'mouseover .entity-to-summary': function(e) {
                var title = $(e.currentTarget).find('a').html();
                this.$('[data-title="'+ title +'"]').addClass('label label-primary entity-to-summary').removeClass('label-info');
            },
            'mouseleave .entity-to-summary': function() {
                this.$('.suggestions-content li a').removeClass('label label-primary entity-to-summary');
                this.$('.main-results-content .entity-to-summary').removeClass('label-primary').addClass('label-info');
            }
        },

        initialize: function() {
            this.entityCollection = new EntityCollection();
            this.documentsCollection = new DocumentsCollection();
            this.promotionsCollection = new PromotionsCollection();
            this.topResultsCollection = new DocumentsCollection();
            this.indexesCollection = new IndexesCollection();
            this.parametricCollection = new ParametricCollection([], {singleRequest: true});

            this.parametricController = new ParametricController({
                parametricCollection: this.parametricCollection
            });

            this.dateView = new DateView();

            this.listenTo(this.dateView, 'change', function(a) {
                this[a.type + "_date"] = a.date;
                this.searchRequest();
            });

            router.on('route:search', function(text) {
                this.entityCollection.reset();
                this.documentsCollection.set([]);
                this.promotionsCollection.set([]);

                if (text) {
                    this.$('.find-input').val(text); //when clicking one of the suggested search links
                    this.keyupAnimation();
                } else {
                    this.reverseAnimation(); //when clicking the small 'find' logo
                }
            }, this);

            this.indexes = {};
            this.indexesCollection.fetch();

            this.listenTo(this.indexesCollection, 'sync', function() {
                // Default to searching against all indexes
                this.indexesCollection.forEach(_.bind(function(indexModel) {
                    this.indexes[indexModel.get('index')] = true;
                }, this));

                this.$indexesDisplay = $(this.indexPopover());

                this.indexesCollection.each(function(model) {
                    var htmlTemplateOutput = $(this.indexPopoverContents({
                        index: model.get('index')
                    }));

                    this.$indexesDisplay.find('.indexes-list').append(htmlTemplateOutput);

                    if (this.indexes[model.get('index')]) { // If index is selected, set the checkbox to checked
                        htmlTemplateOutput.find('input').prop('checked', true);
                    }
                }, this);

                this.$('.list-indexes').popover({
                    html: true,
                    content: this.$indexesDisplay,
                    placement: 'bottom'
                });
            }, this);

            this.listenTo(this.parametricController.logic, 'change', function(fieldText) {
                var newFieldText = fieldText;

                if(newFieldText) {
                    this.fieldText = newFieldText.toString();
                } else {
                    this.fieldText = null;
                }

                this.searchRequest();
            });
        },

        render: function() {
            this.$el.html(this.template);

            this.$('.find-form').submit(function(e){ //preventing input form submit and page reload
                e.preventDefault();
            });

            this.parametricController.view.setElement(this.$('.parametric-container')).render();
            this.dateView.setElement(this.$('.date-container')).render();

            /*top 3 results popover*/
            this.listenTo(this.topResultsCollection, 'add', function(model){
                this.$('.suggestions-content .popover-content .loading-spinner').remove();

                this.$('.suggestions-content .popover-content').append(this.topResultsPopoverContents({
                    title: model.get('title'),
                    summary: model.get('summary').trim().substring(0, 100) + "..."
                }));
            });

            /*suggested links*/
            this.listenTo(this.entityCollection, 'request', function() {
                if(!this.$('.suggestions-content ul').length) {
                    this.$('.suggestions-content').append(_.template(loadingSpinnerTemplate));
                }

                this.$('.suggested-links-header').removeClass('hide')
            });

            this.listenTo(this.entityCollection, 'reset', function() {
                this.$('.suggestions-content').empty();

                if (this.entityCollection.isEmpty()) {
                    this.$('.suggested-links-header').addClass('hide')
                }
                else {
                    var clusters = this.entityCollection.groupBy('cluster');

                    _.each(clusters, function(entities) {
                        this.$('.suggestions-content').append(this.suggestionsTemplate({
                            entities: entities
                        }));

                        this.$('.suggestions-content li a').popover({
                            html: true,
                            content: '<h6>Top Results</h6>',
                            placement: 'right',
                            trigger: 'hover'
                        })
                    }, this);

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

            /*main results content*/
            this.listenTo(this.documentsCollection, 'request', function() {
                this.$('.main-results-content').empty();
                this.$('.main-results-content').append(_.template(loadingSpinnerTemplate));
            });

            this.listenTo(this.promotionsCollection, 'add', function(model) {
                this.formatResult(model, true)
            });

            this.listenTo(this.documentsCollection, 'add', function(model) {
                this.formatResult(model, false);
            });

            this.listenTo(this.documentsCollection, 'sync', function() {
                if(this.documentsCollection.isEmpty()) {
                    this.$('.main-results-content .loading-spinner').remove();
                    this.$('.main-results-content').append(this.noResultsTemplate({i18n: i18n}));
                }
            });

            /*colorbox fancy button override*/
            $('#colorbox').append(_.template(colorboxControlsTemplate));
            $('.nextBtn').on('click', this.handleNextResult);
            $('.prevBtn').on('click', this.handlePrevResult);
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
                summary = summary.replace(new RegExp(entity.id, 'g'), '<span class="label label-info entity-to-summary" data-title="'+entity.text+'"><a href="#find/search/'+entity.text+'">' + entity.text + '</a></span>');
            });

            return summary;
        },

        keyupAnimation: _.debounce(function() {
            /*fancy animation*/
            if($.trim(this.$('.find-input').val()).length) { // input has at least one non whitespace character
                this.$('.find').addClass('animated-container').removeClass('reverse-animated-container');

                this.$('.main-results-content').show();
                this.$('.suggested-links-container').show();
                this.$('.parametric-container').show();
                this.changeQueryText(this.$('.find-input').val());
                this.searchRequest();
            } else {
                this.reverseAnimation();
                vent.navigate('find/search', {trigger: false});
            }
            this.$('.popover').remove();
        }, 500),

        handlePrevResult: function() {
            $.colorbox.prev();
        },

        handleNextResult: function() {
            $.colorbox.next();
        },

        reverseAnimation: function() {
            /*fancy reverse animation*/
            this.$('.find').removeClass('animated-container').addClass('reverse-animated-container');

            this.$('.main-results-content').hide();
            this.$('.suggested-links-container').hide();
            this.$('.parametric-container').hide();
            this.$('.find-input').val('');
            this.$('.popover').remove();
        },

        changeQueryText: function(queryText) {
            this.queryText = queryText;
            this.parametricController.view.clearFieldText();
        },

        searchRequest: function() {
            if (!_.isEmpty(this.indexes)) { // Do we have the list of indexes yet?
                var selectedIndexes = this.selectedIndexes();

                this.parametricController.logic.setQueryText(this.queryText);
                this.parametricController.logic.setIndexes(selectedIndexes);

                this.documentsCollection.fetch({
                    data: {
                        text: this.queryText,
                        max_results: 30,
                        summary: 'quick',
                        index: selectedIndexes,
                        field_text: this.fieldText || null,
                        min_date: this.min_date || null,
                        max_date: this.max_date || null
                    }
                }, this);

                this.promotionsCollection.fetch({
                    data: {
                        text: this.queryText,
                        max_results: 30, // TODO maybe less?
                        summary: 'quick',
                        index: selectedIndexes,
                        field_text: this.fieldText || null,
                        min_date: this.min_date || null,
                        max_date: this.max_date || null
                    }
                }, this);

                this.entityCollection.fetch({
                    data: {
                        text: this.queryText,
                        index: selectedIndexes,
                        field_text: this.fieldText || null
                    }
                });

                vent.navigate('find/search/' + encodeURIComponent(this.queryText), {trigger: false});
            }
            else {
                this.indexesCollection.once('sync', function() {
                    this.searchRequest();
                }, this);
            }
        },

        selectedIndexes: function() {
            return _.chain(this.indexes).map(function(value, key) {
                return (value ? key : undefined); // Return names of selected indexes and undefined for unselected ones
            }).compact().value();
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
        }
    });
});
