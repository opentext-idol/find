/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'text!find/templates/app/util/range-input.html'
], function(_, $, Backbone, template) {
    'use strict';

    function destroyTooltip() {
        this.$('.range-input-slider').tooltip('destroy');
    }

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'change .range-input-slider': function(e) {
                const $target = $(e.target);
                const value = $target.val();
                this.$('.tooltip-inner').text(value);

                $target.attr('data-original-title', value);
                $target.blur();
                this.model.set('value', value);
            },
            'input .range-input-slider': function(e) {
                const $target = $(e.target);
                const value = $target.val();
                this.$('.tooltip-inner').text(value);
            }
        },

        initialize: function(options) {
            this.min = options.min;
            this.max = options.max;
            this.step = options.step;
            this.leftLabel = options.leftLabel;
            this.rightLabel = options.rightLabel;
        },

        render: function() {
            destroyTooltip.call(this);

            this.$el.html(this.template({
                leftLabel: this.leftLabel,
                min: this.min,
                max: this.max,
                rightLabel: this.rightLabel,
                step: this.step,
                value: this.model.get('value')
            }));

            this.$('.range-input-slider')
                .tooltip({
                    title: this.model.get('value'),
                    placement: 'top'
                });
        },

        remove: function() {
            destroyTooltip.call(this);

            Backbone.View.prototype.remove.call(this);
        }
    });
});
