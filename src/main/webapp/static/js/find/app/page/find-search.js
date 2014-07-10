define([
    'js-utils/js/base-page',
    'find/app/model/entity-collection',
    'text!find/templates/app/page/find-search.html',
    'text!find/templates/app/page/results-container.html',
    'text!find/templates/app/page/suggestions-container.html',
    'colorbox'
], function(BasePage, EntityCollection, template, resultsTemplate, suggestionsTemplate) {

    return BasePage.extend({

        template: _.template(template),
        resultsTemplate: _.template(resultsTemplate),
        suggestionsTemplate: _.template(suggestionsTemplate),

        events: {
            'keyup .find-input': 'keyupAnimation'
        },

        initialize: function() {
            this.entityCollection = new EntityCollection();
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
            $.ajax({
                url: 'https://api.idolondemand.com/1/api/sync/querytextindex/v1',
                data: {
                    text: input,
                    apikey: 'XYZ123ABC'
                },
                success: _.bind(function(data, status) {
                    this.$('.main-results-content').empty();

                    _.each(data.documents, function(doc) {
                        var $newResult = $(_.template(resultsTemplate ,{
                            title: doc.title,
                            reference: doc.reference,
                            weight: doc.weight
                        }));

                        $('.main-results-content').append($newResult);

                        $newResult.find('.result-header').colorbox({
                            iframe: true,
                            width:'70%',
                            height:'70%',
                            href: doc.reference,
                            rel: 'results'
                        });
                    }, this);
                }, this)
            });

            this.entityCollection.fetch({
                data: {
                    text: input
                }
            });
        }
    });
});