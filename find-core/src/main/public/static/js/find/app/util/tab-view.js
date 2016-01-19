define([
    'backbone',
    'underscore',
    'jquery',
    'js-whatever/js/list-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/util/tab-view/tab-view.html',
    'text!find/templates/app/util/tab-view/tab-item.html'
], function(Backbone, _, $, ListView, i18n, template, itemTemplate) {
    "use strict";

    return Backbone.View.extend({

        template: _.template(template),
        itemTemplate: _.template(itemTemplate),

        initialize: function (options) {
            this.collection = options.collection;

            this.collection.add([
                new Backbone.Model({title: 'Toronto'}),
                new Backbone.Model({title: 'Sandwich'}),
                new Backbone.Model({title: 'Lake of Death'})
            ]);

            this.listView = new ListView({
                collection: this.collection,
                itemOptions: {
                    tagName: 'li',
                    template: this.itemTemplate
                }
            });
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.listView.setElement(this.$('.saved-searches-list')).render();
        }

    });
});
