define([
    'js-utils/js/base-page',
    'find/app/model/entity-collection',
    'find/app/model/documents-collection',
    'text!find/templates/app/page/find-search.html',
    'text!find/templates/app/page/results-container.html',
    'text!find/templates/app/page/suggestions-container.html',
    'colorbox'
], function(BasePage, EntityCollection, DocumentsCollection, template, resultsTemplate, suggestionsTemplate) {

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
                    rel: 'results'
                });
            });

            this.listenTo(this.documentsCollection, 'remove', function(model) {
                var reference = model.get('reference');

                this.$('[data-reference="' + reference + '"]').remove();
            });
        },

        keyupAnimation: function() {
            /*fancy animation*/
            this.$('.find-logo').slideUp('slow');
            this.$('.find').animate({margin:'10px'},1000);
            this.$('.form-search').animate({
                width: '63.5%',
                'margin-left': '26%'
            },1000);
            this.$('.suggested-links-container.span3').show();

            this.searchRequest();
        },

        searchRequest: function() {
            var input = this.$('.find-input').val();

            this.documentsCollection.fetch({
                data: {
                    text: input
                }
            }, this);

            this.entityCollection.fetch({
                data: {
                    text: input
                }
            });
        }
    });
});