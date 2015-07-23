define([
    'backbone',
    'js-whatever/js/list-view',
    'text!find/templates/app/page/filter-display/filter-display-view.html',
    'text!find/templates/app/page/filter-display/filter-display-item.html',
    'i18n!find/nls/bundle'
], function(Backbone, ListView, template, itemTemplate, i18n) {

    // Each of the collection's models should have an id and a text attribute
    return Backbone.View.extend({
        template: _.template(template)({i18n: i18n}),
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

            this.listenTo(this.collection, 'reset update', function() {
                this.updateFilterCount();
                this.updateVisibility();
            });
        },

        render: function() {
            this.$el.html(this.template);
            this.updateVisibility();

            this.listView.setElement(this.$('.filter-display-list')).render();

            this.$filterDisplayCount = this.$('.filter-display-count');
            this.updateFilterCount();

            return this;
        },

        updateFilterCount: function() {
            if (this.$filterDisplayCount) {
                this.$filterDisplayCount.text(i18n['search.filtersDisplay.nfilters'](this.collection.length));
            }
        },

        updateVisibility: function() {
            this.$el.toggleClass('hide', this.collection.isEmpty());
        }
    });

});