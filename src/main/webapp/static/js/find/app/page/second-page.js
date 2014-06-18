define([
    'js-utils/js/base-page',
    'text!find/templates/app/page/second-page.html'
], function(BasePage, template) {

    return BasePage.extend({

        template: _.template(template),

        render: function() {
            this.$el.html(this.template());
        }

    });

})