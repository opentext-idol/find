define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle'
], function(Backbone, _, $, i18n) {
    "use strict";

    return Backbone.View.extend({

        initialize: function (options) {
            this.collection = options.savedSearchesCollection;
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

        }
    });
});
