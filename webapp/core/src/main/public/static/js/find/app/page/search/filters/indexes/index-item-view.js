/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'js-whatever/js/list-item-view',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/filters/indexes/index-item-view.html',
    'bootstrap'
], function(_, ListItemView, i18n, template) {
    'use strict';

    const templateFunction = _.template(template);

    return ListItemView.extend({
        initialize: function() {
            ListItemView.prototype.initialize.call(this, {template: templateFunction});
        },

        render: function() {
            ListItemView.prototype.render.call(this);
            this.updateDeleted();
        },

        remove: function() {
            this.$el.tooltip('destroy');
            ListItemView.prototype.remove.call(this);
        },

        updateDeleted: function() {
            const deleted = this.model.get('deleted');
            this.$el.toggleClass('disabled-index', deleted);
            this.$('.database-input').toggleClass('clickable', !deleted);
            this.$('.database-icon').toggleClass('disabled', deleted);

            if (deleted) {
                this.$el.tooltip({
                    placement: 'bottom',
                    title: i18n['search.indexes.invalidIndex']
                });
            } else {
                this.$el.tooltip('destroy');
            }
        }
    });
});
