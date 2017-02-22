/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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

        initialize: function(options) {
            this.showGraphButtons = options.showGraphButtons;
        },

        render: function() {
            this.$el
                .html(this.template({
                    count: this.model.get('count') || 0,
                    value: this.model.get('value'),
                    displayValue: this.model.get('displayValue'),
                    showGraphButtons: this.showGraphButtons
                }))
                .iCheck({checkboxClass: 'icheckbox-hp'});

            this.updateSelected();
        },

        updateSelected: function() {
            this.$('input').iCheck(this.model.get('selected') ? 'check' : 'uncheck');
        }
    });
});
