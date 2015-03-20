/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/base-page',
    'find/app/model/entity-collection',
    'find/app/model/documents-collection',
    'find/app/model/indexes-collection',
    'find/app/router',
    'find/app/vent',
    'i18n!find/nls/bundle',
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
    'colorbox'
], function(BasePage, EntityCollection, DocumentsCollection, IndexesCollection, router, vent, i18n, $, _, template, resultsTemplate,
            suggestionsTemplate, loadingSpinnerTemplate, colorboxControlsTemplate, indexPopover, indexPopoverContents, topResultsPopoverContents) {

    return BasePage.extend({

        template: _.template(template),
        resultsTemplate: _.template(resultsTemplate),
        noResultsTemplate: _.template('<div class="no-results span10"><%- i18n["search.noResults"] %> </div>'),
        suggestionsTemplate: _.template(suggestionsTemplate),
        indexPopover: _.template(indexPopover),
        indexPopoverContents: _.template(indexPopoverContents),
        topResultsPopoverContents: _.template(topResultsPopoverContents),

        events: {
            'keyup .find-input': 'keyupAnimation',
            'click .list-indexes': _.debounce(function(){
                this.$('.popover-content label').html('');

                this.indexesCollection.fetch();
            }, 500, true),
            'change .indexCheckbox': function(e) {
                var toggledIndex = $(e.currentTarget).val();
                var checked = $(e.currentTarget).is(':checked');

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
                        index: this.index
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
            this.topResultsCollection = new DocumentsCollection();
            this.indexesCollection = new IndexesCollection();

            router.on('route:search', function(text) {
                this.entityCollection.reset();
                this.documentsCollection.set([]);

                if (text) {
                    this.$('.find-input').val(text); //when clicking one of the suggested search links
                    this.keyupAnimation();
                } else {
                    this.reverseAnimation(); //when clicking the small 'find' logo
                }
            }, this);

            this.indexesCollection.once('sync', function() {
                // Default to searching against all indexes
                this.indexesCollection.forEach(_.bind(function(indexModel) {
                    this.indexes[indexModel.get('index')] = true;
                }, this))
            }, this);

            this.indexes = {};
            this.indexesCollection.fetch();
        },

        render: function() {
            this.$el.html(this.template);

            this.$('.find-form').submit(function(e){ //preventing input form submit and page reload
                e.preventDefault();
            });

            this.$('.list-indexes').popover({
                html: true,
                content: this.indexPopover(),
                placement: 'bottom'
            });

            /*indices popover*/
            this.listenTo(this.indexesCollection, 'request', function(){
                if(this.$('.find-form .popover-content').length === 1) {
                    this.$('.find-form  .popover-content').append(_.template(loadingSpinnerTemplate));
                }
            });

            this.listenTo(this.indexesCollection, 'add', function(model){
                this.$('.find-form  .popover-content .loading-spinner').remove();

                var htmlTemplateOutput = $(this.indexPopoverContents({
                    index: model.get('index')
                }));

                this.$('.find-form .popover-content ul').append(htmlTemplateOutput);

                if (this.indexes[model.get('index')]) { // If index is selected, set the checkbox to checked
                    htmlTemplateOutput.find('input').prop('checked', true);
                }
            });

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
                }
            });

            /*main results content*/
            this.listenTo(this.documentsCollection, 'request', function() {
                this.$('.main-results-content').empty();
                this.$('.main-results-content').append(_.template(loadingSpinnerTemplate));
            });

            this.listenTo(this.documentsCollection, 'add', function(model) {
                var reference = model.get('reference');
                var summary = model.get('summary');

                summary = this.addLinksToSummary(summary);

                this.$('.main-results-content .loading-spinner').remove();

                var $newResult = $(_.template(resultsTemplate ,{
                    title: model.get('title'),
                    reference: reference,
                    index: model.get('index'),
                    summary: summary
                }));

                this.$('.main-results-content').append($newResult);

                $newResult.find('.result-header').colorbox({
                    iframe: true,
                    width:'70%',
                    height:'70%',
                    href: reference,
                    rel: 'results',
                    current: '{current} of {total}',
                    onComplete: _.bind(function() {
                        $('#cboxPrevious, #cboxNext').remove(); //removing default colorbox nav buttons
                    }, this)
                });

                $newResult.find('.dots').click(function (e) {
                    e.preventDefault();
                    $newResult.find('.result-header').trigger('click'); //dot-dot-dot triggers the colorbox event
                });
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

                this.$('.suggested-links-container.span2').show();
                this.searchRequest(this.$('.find-input').val());
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

            this.$('.main-results-content').empty();
            this.$('.suggested-links-container.span2').hide();
            this.$('.find-input').val('');
            this.$('.popover').remove();
        },

        searchRequest: function(input) {
            if (this.indexes) { // Do we have the list of indexes yet?
                var selectedIndexes = _.chain(this.indexes).map(function(value, key) {
                    return (value ? key : undefined); // Return names of selected indexes and undefined for unselected ones
                }).compact().value();

                this.documentsCollection.fetch({
                    data: {
                        text: input,
                        max_results: 30,
                        summary: 'quick',
                        index: selectedIndexes
                    }
                }, this);

                this.entityCollection.fetch({
                    data: {
                        text: input,
                        index: selectedIndexes
                    }
                });

                vent.navigate('find/search/' + encodeURIComponent(input), {trigger: false});
            }
            else {
                this.indexesCollection.once('sync', function() {
                    this.searchRequest(input);
                }, this);
            }
        }
    });
});