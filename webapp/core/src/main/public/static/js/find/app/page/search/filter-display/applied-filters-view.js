/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'jquery',
    'find/app/page/search/abstract-section-view',
    'js-whatever/js/list-view',
    'js-whatever/js/list-item-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filter-display/applied-filters-view.html',
    'text!find/templates/app/page/search/filter-display/applied-filters-view-item.html',
    'bootstrap'
], function(_, $, AbstractSectionView, ListView, ListItemView, i18n, template, itemTemplate) {
    'use strict';

    const html = _.template(template)({i18n: i18n});

    const removeAllButton = '<span class="inline clickable hyperlink text-muted remove-all-filters">' +
        i18n["search.filters.removeAll"] + '</span>';

    const FilterListItemView = ListItemView.extend({
        render: function() {
            ListItemView.prototype.render.apply(this);

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
        itemTemplate: _.template(itemTemplate),

        events: {
            'click .filters-remove-icon': function(e) {
                const id = $(e.currentTarget).closest('[data-id]').attr('data-id');
                this.collection.remove(id);
            },
            'click .remove-all-filters': function() {
                // get cids as an array so we don't modify the collection while iterating
                _.chain(this.collection.models)
                    .map(function(model) {
                        return model.cid;
                    })
                    .each(function(cid) {
                        this.collection.remove(cid);
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

            this.listenTo(this.collection, 'reset update', this.updateView);
        },

        render: function() {
            AbstractSectionView.prototype.render.apply(this);

            this.getSectionControls().html(removeAllButton);

            this.getViewContainer().html(html);

            this.updateView();

            this.listView.render();
            this.$('.filters-labels').append(this.listView.$el);

            return this;
        },

        updateVisibility: function() {
            this.getViewContainer().toggleClass('hide', this.collection.isEmpty());
            this.getSectionControls().toggleClass('hide', this.collection.isEmpty());
        },

        updateHeaderCounter: function() {
            this.getHeaderCounter().text('(' + this.collection.length + ')');
        },

        updateView: function() {
            this.updateVisibility();
            this.updateHeaderCounter();
        }
    });
});
