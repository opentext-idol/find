/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'find/app/page/search/abstract-section-view',
    'js-whatever/js/list-view',
    'js-whatever/js/list-item-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filter-display/filter-display.html',
    'text!find/templates/app/page/search/filter-display/filter-display-item.html',
    'bootstrap'
], function(AbstractSectionView, ListView, ListItemView, i18n, template, itemTemplate) {

    var html = _.template(template)({i18n: i18n});

    var removeAllButton = '<span class="inline clickable remove-all-filters text-muted"><i class="fa fa-ban"></i></span>';

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
    return AbstractSectionView.extend({
        template: _.template(template),
        itemTemplate: _.template(itemTemplate),

        events: {
            'click .filters-remove-icon': function(e) {
                var id = $(e.currentTarget).closest('[data-id]').attr('data-id');
                this.collection.remove(id);
            },
            'click .remove-all-filters': function() {
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
            AbstractSectionView.prototype.initialize.apply(this, arguments);

            this.listView = new ListView({
                collection: this.collection,
                ItemView: FilterListItemView,
                className: 'inline',
                itemOptions: {
                    className: 'label filter-label border filters-margin inline-block m-b-xs',
                    template: this.itemTemplate
                }
            });

            this.listenTo(this.collection, 'reset update', this.updateVisibility);
        },

        render: function() {
            AbstractSectionView.prototype.render.apply(this, arguments);

            this.getSectionControls().html(removeAllButton);

            this.getViewContainer().html(html);

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
