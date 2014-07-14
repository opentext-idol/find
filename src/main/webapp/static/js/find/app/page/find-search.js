define([
    'js-utils/js/base-page',
    'find/app/model/entity-collection',
    'find/app/model/documents-collection',
    'find/app/router',
    'find/app/vent',
    'text!find/templates/app/page/find-search.html',
    'text!find/templates/app/page/results-container.html',
    'text!find/templates/app/page/suggestions-container.html',
    'colorbox'
], function(BasePage, EntityCollection, DocumentsCollection, router, vent, template, resultsTemplate, suggestionsTemplate) {

    return BasePage.extend({

        template: _.template(template),
        resultsTemplate: _.template(resultsTemplate),
        suggestionsTemplate: _.template(suggestionsTemplate),

        events: {
            'keyup .find-input': 'keyupAnimation'
        },

        initialize: function() {
            this.entityCollection = new EntityCollection();
            this.documentsCollection = new DocumentsCollection();

            this.searchRequest = _.debounce(_.bind(this.searchRequest, this), 500);

            router.on('route:search', this.searchNavigation, this);

            router.on('route:find', function(routeName) {
                if(routeName === 'find-search') {
                    this.entityCollection.reset();
                    this.documentsCollection.set([]);

                    this.reverseAnimation();
                }
            }, this)
        },

        render: function() {
            this.$el.html(this.template());

            this.$('.find-form').submit(function(e){
                e.preventDefault();
            });

            this.listenTo(this.entityCollection, 'reset', function() {
                this.$('.suggestions-content').empty();

                var clusters = this.entityCollection.groupBy('cluster');

                _.each(clusters, function(entities) {
                    this.$('.suggestions-content').append(this.suggestionsTemplate({
                        entities: entities
                    }))
                }, this);
            });

            this.listenTo(this.documentsCollection, 'add', function(model) {
                var reference = model.get('reference');

                var $newResult = $(_.template(resultsTemplate ,{
                    title: model.get('title'),
                    reference: reference,
                    weight: model.get('weight')
                }));

                this.$('.main-results-content').append($newResult);

                $newResult.find('.result-header').colorbox({
                    iframe: true,
                    width:'70%',
                    height:'70%',
                    href: reference,
                    rel: 'results',
                    current: '{current} of {total}'
                });
            });

            this.listenTo(this.documentsCollection, 'remove', function(model) {
                var reference = model.get('reference');

                this.$('[data-reference="' + reference + '"]').remove();
            });
        },

        keyupAnimation: function() {
            /*fancy animation*/
            if($.trim(this.$('.find-input').val()).length) { //checking if input doesn't have any spaces or empty
                this.$('.find-logo').slideUp('slow');
                this.$('.find').addClass('animated-container ').removeClass('reverse-animated-container');
                this.$('.form-search').addClass('animated-form ').removeClass('reverse-animated-form');

                this.$('.suggested-links-container.span2').show();

                this.searchRequest(this.$('.find-input').val());
            } else {
                this.reverseAnimation();
                vent.navigate('find/find-search', {trigger: false});
                this.$('.main-results-content').empty();
            }
        },

        reverseAnimation: function() {
            /*fancy reverse animation*/
            this.$('.find-logo').slideDown('slow');
            this.$('.find').removeClass('animated-container ').addClass('reverse-animated-container');
            this.$('.form-search').removeClass('animated-form').addClass('reverse-animated-form');

            this.$('.suggested-links-container.span2').hide();
            this.$('.find-input').val('');
        },

        searchNavigation: function(input) {
            this.$('.find-input').val(input);

            this.keyupAnimation();
        },

        searchRequest: function(input) {
            this.documentsCollection.fetch({
                data: {
                    text: input,
                    max_results: 30
                }
            }, this);

            this.entityCollection.fetch({
                data: {
                    text: input
                }
            });

            vent.navigate('find/find-search/' + encodeURIComponent(input), {trigger: false});
        }
    });
});