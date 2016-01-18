define([
    'backbone',
    'underscore',
    'jquery',
    'js-whatever/js/list-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/saved-searches/saved-searches-view.html',
    'text!find/templates/app/page/search/saved-searches/saved-search-item.html'
], function(Backbone, _, $, ListView, i18n, template, itemTemplate) {
    "use strict";

    return Backbone.View.extend({

        template: _.template(template),
        itemTemplate: _.template(itemTemplate),

        initialize: function (options) {
            this.collection = options.savedSearchesCollection;

            this.listView = new ListView({
                collection: this.collection,
                itemOptions: {
                    tagName: 'li',
                    template: this.itemTemplate
                }
            });

            this.listenTo(this.collection, 'reset update', this.updateVisibility);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.listView.setElement(this.$('.saved-searches-tab-list')).render();
            this.updateVisibility();
        },

        updateVisibility: function() {
            this.$('.saved-searches').toggleClass('hide', this.collection.isEmpty());
        }

    });
});
