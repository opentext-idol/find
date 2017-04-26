/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'text!find/templates/app/util/range-input.html'
], function(Backbone, template) {
    'use strict';

    return Backbone.View.extend({

        template: _.template(template),

        events: {
            'change .speed-slider': function(e) {
                const $target = $(e.target);
                const value = $target.val();
                $target.attr('data-original-title', value);
                this.$('.tooltip-inner').text(value);
                $target.blur();
                this.model.set('value', value);
            },
            'input .speed-slider': function(e) {
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
            this.$el.html(this.template({
                leftLabel: this.leftLabel,
                min: this.min,
                max: this.max,
                rightLabel: this.rightLabel,
                step: this.step,
                value: this.model.get('value')
            }));

            this.$('.speed-slider')
                .tooltip({
                    title: this.model.get('value'),
                    placement: 'top'
                });
        },

        destroyTooltip: function() {
            this.$('.speed-slider').tooltip('destroy');
        },

        remove: function() {
            this.destroyTooltip();

            Backbone.View.prototype.remove.apply(this, arguments);
        }

    });

});
