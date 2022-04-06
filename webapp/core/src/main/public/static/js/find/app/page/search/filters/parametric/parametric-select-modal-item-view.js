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
    'backbone',
    'underscore',
    'text!find/templates/app/page/search/filters/parametric/parametric-select-modal-item-view.html',
    'iCheck'
], function(Backbone, _, template) {
    'use strict';

    return Backbone.View.extend({
        tagName: 'li',
        template: _.template(template),

        render: function() {
            this.$el
                .html(this.template({
                    count: this.model.get('count') || 0,
                    value: this.model.get('value'),
                    displayValue: this.model.get('displayValue')
                }))
                .iCheck({checkboxClass: 'icheckbox-hp'});

            this.updateSelected();
        },

        updateSelected: function() {
            this.$('input').iCheck(this.model.get('selected') ? 'check' : 'uncheck');
        }
    });
});
