define([
    'js-utils/js/base-page',
    'find/app/model/entity-collection',
    'find/app/model/documents-collection',
    'find/app/model/indexes-collection',
    'find/app/router',
    'find/app/vent',
    'text!find/templates/app/page/find-search.html',
    'text!find/templates/app/page/results-container.html',
    'text!find/templates/app/page/suggestions-container.html',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/colorbox-controls.html',
    'text!find/templates/app/page/index-popover-contents.html',
    'text!find/templates/app/page/top-results-popover-contents.html',
    'colorbox'
], function(BasePage, EntityCollection, DocumentsCollection, IndexesCollection, router, vent, template, resultsTemplate,
            suggestionsTemplate, loadingSpinnerTemplate, colorboxControlsTemplate, indexPopoverContents, topResultsPopoverContents) {

    return BasePage.extend({

        template: _.template(template),
        resultsTemplate: _.template(resultsTemplate),
        suggestionsTemplate: _.template(suggestionsTemplate),
        indexPopoverContents: _.template(indexPopoverContents),
        topResultsPopoverContents: _.template(topResultsPopoverContents),

        events: {
            'keyup .find-input': 'keyupAnimation',
            'click .list-indexes': _.debounce(function(){
                this.indexesCollection.fetch();
            }, 500, true),
            'change [name="indexRadios"]': function(e) {
                this.indexes = $(e.currentTarget).val();

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
                        indexes: this.indexes
                    }
                });
            }, 400),
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
            this.indexes = 'wiki_eng'; //hardcoding a default value

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
        },

        render: function() {
            this.$el.html(this.template);

            this.$('.find-form').submit(function(e){ //preventing input form submit and page reload
                e.preventDefault();
            });

            this.$('.list-indexes').popover({
                html: true,
                content: '<h6>Public Indexes</h6>',
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

                this.$('.find-form .popover-content').append(this.indexPopoverContents({
                    index: model.get('index')
                }));

                model.get('index') === this.indexes ? this.$('[name="indexRadios"]').val([this.indexes]): false;
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
            });

            this.listenTo(this.entityCollection, 'reset', function() {
                this.$('.suggestions-content').empty();

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
            });

            /*main results content*/
            this.listenTo(this.documentsCollection, 'request', function() {
                if(!this.$('.main-results-container').length) {
                    this.$('.main-results-content').append(_.template(loadingSpinnerTemplate));
                }
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

            this.listenTo(this.documentsCollection, 'remove', function(model) {
                var reference = model.get('reference');

                this.$('[data-reference="' + reference + '"]').remove();
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
            if($.trim(this.$('.find-input').val()).length) { //checking if input doesn't have any spaces or empty
                this.$('.find').addClass('animated-container').removeClass('reverse-animated-container');

                this.$('.suggested-links-container.span2').show();
                this.searchRequest(this.$('.find-input').val());
            } else {
                this.reverseAnimation();
                vent.navigate('find/search', {trigger: false});
                this.$('.main-results-content').empty();
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
            this.$('.find').removeClass('animated-container ').addClass('reverse-animated-container');

            this.$('.suggested-links-container.span2').hide();
            this.$('.find-input').val('');
            this.$('.popover').remove();
        },

        searchRequest: function(input) {
            this.documentsCollection.fetch({
                data: {
                    text: input,
                    max_results: 30,
                    summary: 'quick',
                    indexes: this.indexes
                }
            }, this);

            this.entityCollection.fetch({
                data: {
                    text: input,
                    indexes: this.indexes
                }
            });

            vent.navigate('find/search/' + encodeURIComponent(input), {trigger: false});
        }
    });
});