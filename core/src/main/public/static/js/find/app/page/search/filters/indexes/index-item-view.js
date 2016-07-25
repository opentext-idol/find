/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/list-item-view',
    'underscore',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/filters/indexes/index-item-view.html',
    'bootstrap'
], function(ListItemView, _, i18n, template) {

    var templateFunction = _.template(template);

    return ListItemView.extend({
        initialize: function() {
            ListItemView.prototype.initialize.call(this, {template: templateFunction});
        },

        render: function() {
            ListItemView.prototype.render.call(this);
            this.updateDeleted();
        },

        updateDeleted: function() {
            var deleted = this.model.get('deleted');
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
