define([
    'backbone',
    'js-whatever/js/list-view',
    'js-whatever/js/list-item-view',
    'find/app/model/search-filters-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filter-display/filter-display.html',
    'text!find/templates/app/page/search/filter-display/filter-display-item.html',
    'bootstrap'
], function(Backbone, ListView, ListItemView, SearchFiltersCollection, i18n, template, itemTemplate) {

    var html = _.template(template)({i18n: i18n});

    var FilterListItemView = ListItemView.extend({
        render: function() {
            ListItemView.prototype.render.apply(this, arguments);

            this.$tooltip = this.$('[data-toggle="tooltip"]');

            this.$tooltip.tooltip({
                container: 'body',
                placement: 'bottom'
            });
        },

        remove: function() {
            this.$tooltip.tooltip('destroy');

            ListItemView.prototype.remove.apply(this, arguments);
        }
    });

    // Each of the collection's models should have an id and a text attribute
    return Backbone.View.extend({
        template: _.template(template),
        itemTemplate: _.template(itemTemplate),

        events: {
            'click .filters-remove-icon': function(e) {
                var id = $(e.currentTarget).closest('[data-id]').attr('data-id');
                this.collection.remove(id);
            },
            'click .remove-all-filters': function() {
                // Separate picking attributes from calling removeFilter so we don't modify the collection while iterating
                _.chain(this.collection.models)
                    .map(function(model) {
                        return model.id;
                    })
                    .each(function(id) {
                        this.collection.remove(id);
                    }, this);
            }
        },

        initialize: function() {
            this.listView = new ListView({
                collection: this.collection,
                ItemView: FilterListItemView,
                className: 'inline-block',
                itemOptions: {
                    className: 'label filter-label border filters-margin inline-block m-b-xs',
                    template: this.itemTemplate
                }
            });

            this.listenTo(this.collection, 'reset update', this.updateVisibility);
        },

        render: function() {
            this.$el.html(html);

            this.updateVisibility();

            this.listView.render();
            this.$('.filters-labels').append(this.listView.$el);

            return this;
        },

        updateVisibility: function() {
            this.$el.toggleClass('hide', this.collection.isEmpty());
        }
    });

});