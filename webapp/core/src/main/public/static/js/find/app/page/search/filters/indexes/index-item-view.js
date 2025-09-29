/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
        initialize: function(options) {
            this.parametricCollection = options.parametricCollection;
            ListItemView.prototype.initialize.call(this, {template: templateFunction});
        },

        render: function() {
            ListItemView.prototype.render.call(this);
            this.updateDeleted();
            this.updateCount();
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
        },

        updateCount: function() {
            const dbName = this.model.get('name');
            const model = this.parametricCollection.findWhere({ id: 'AUTN_DATABASE' });
            const db = model ? _.find(model.get('values'),
                db => db.value.toLowerCase() === dbName.toLowerCase()
            ) : null;
            this.$('.database-doc-count').text(' (' + (db ? db.count : 0 ) + ')');
        }
    });
});
