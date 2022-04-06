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
    'js-whatever/js/list-item-view',
    'underscore',
    'text!find/templates/app/util/csv-field-selection-list-item.html',
    'i18n!find/nls/bundle',
    'iCheck'
], function(ListItemView, _, template, i18n) {
    'use strict';

    return ListItemView.extend({
        template: _.template(template),

        initialize: function(options) {
            ListItemView.prototype.initialize.call(this, _.defaults({
                template: this.template,
                templateOptions: {
                    fieldDataId: options.model.id,
                    fieldPrintedLabel: i18n['search.document.' + options.model.id] || options.model.get('displayName')
                }
            }, options));
        },

        render: function() {
            ListItemView.prototype.render.apply(this);

            this.$el.iCheck({checkboxClass: 'icheckbox-hp'});
            this.updateSelected();
        },

        updateSelected: function() {
            this.$el.iCheck(this.model.get('selected') ? 'check' : 'uncheck');
        }
    });
});
