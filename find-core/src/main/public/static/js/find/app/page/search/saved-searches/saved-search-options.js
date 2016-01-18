define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/saved-searches/saved-search-options.html'
], function(Backbone, _, $, i18n, template) {
    "use strict";

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .saved-search-option-delete': function(e) {
                var id = $(e.currentTarget).closest('[data-id]');
                this.collection.remove(id);
            },
            'click .saved-search-option-rename': function() {
                //TODO: populate these
            },
            'click .saved-search-option-reset': function() {

            }
        },

        initialize: function (options) {
            this.collection = options.savedSearchCollection;
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));
        }
    });
});
