define([
    'backbone',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/filter-display/filter-display-item.html'
], function(Backbone, ListView, itemTemplate) {

    // Each of the collection's models should have an id and a text attribute
    return Backbone.View.extend({
        itemTemplate: _.template(itemTemplate),

        events: {
            'click .filters-remove-icon': function(e) {
                var id = $(e.currentTarget).closest('[data-id]').attr('data-id');
                this.collection.remove(id);
            }
        },

        initialize: function() {
            this.listView = new ListView({
                collection: this.collection,
                itemOptions: {
                    className: 'label filter-label border filters-margin inline-block',
                    template: this.itemTemplate
                }
            });

            this.listenTo(this.collection, 'reset update', this.updateVisibility);
        },

        render: function() {
            this.updateVisibility();

            this.listView.render();
            this.$el.append(this.listView.$el);

            return this;
        },

        updateVisibility: function() {
            this.$el.toggleClass('hide', this.collection.isEmpty());
        }
    });

});