define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/preview-mode-view.html'
], function(Backbone, _, $, i18n, template) {
    "use strict";

    return Backbone.View.extend({
        template: _.template(template),

        render: function() {
            this.$el.html(this.template({
                i18n:i18n
            }));
        }

    });

});
