define([
    'js-utils/js/base-page',
    'text!find/templates/app/page/first-page.html',
    'text!find/templates/app/page/results-container.html',
    'text!find/templates/app/page/suggestions-container.html',
    'colorbox'
], function(BasePage, template, resultsTemplate, suggestionsTemplate) {

    return BasePage.extend({

        template: _.template(template),
        resultsTemplate: _.template(resultsTemplate),
        suggestionsTemplate: _.template(suggestionsTemplate),

        events: {
            'keyup .find-input': 'keyupAnimation'
        },

        render: function() {
            this.$el.html(this.template());
        },

        keyupAnimation: function() {
            /*fancy animation*/
            this.$('.find-logo').slideUp('slow');
            this.$('.find').animate({margin:'10px'},1000);
            this.$('.form-search').animate({width:'48%'},1000);
            this.$('.suggestions-content').append(this.suggestionsTemplate);
            this.$('.suggested-links-container.span3').show();

            this.searchRequest();
        },

        searchRequest: function() {
            var input = this.$('.find-input').val();
            this.$('.main-results-content').empty();
            $.ajax({
                url: 'https://api.idolondemand.com/1/api/sync/querytextindex/v1',
                data: {
                    text: input,
                    apikey: 'XYZ123ABC'
                },
                success: function(data, status) {
                    _.each(data.documents, function(i) {
                        $('.main-results-content').append(_.template(resultsTemplate ,{
                            title: i.title,
                            reference: i.reference,
                            weight: i.weight
                        }));
                    });

                }
            });
        }
    });
});