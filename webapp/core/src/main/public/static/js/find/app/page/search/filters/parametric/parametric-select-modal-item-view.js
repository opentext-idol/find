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

const Backbone = require('backbone');
const _ = require('underscore');
const template = require('find/templates/app/page/search/filters/parametric/parametric-select-modal-item-view.html');

// Loaded for side effects only - do not remove.
require('iCheck');

module.exports = Backbone.View.extend({
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

