/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'text!find/templates/app/page/search/filters/parametric/parametric-value-view.html'
], function($, Backbone, template) {
    'use strict';

    return Backbone.View.extend({
        className: 'parametric-value-element selectable-table-item clickable',
        tagName: 'tr',

        initialize: function(options) {
            this.selectedValuesCollection = options.selectedValuesCollection;

            this.$el.attr('data-value', this.model.get('value'));
            this.$el.attr('data-display-value', this.model.get('displayValue'));

            this.listenTo(this.selectedValuesCollection, 'update', this.updateSelected);
        },

        render: function() {
            this.$el.html(template);

            this.$text = this.$('.parametric-value-text');
            this.$name = this.$('.parametric-value-name');
            this.$count = this.$('.parametric-value-count');
            this.$check = this.$('.parametric-value-icon');

            this.updateText();
            this.updateCount();
            this.updateSelected();
        },

        updateText: function() {
            this.$text.tooltip('destroy');

            const name = this.model.get('displayValue');
            this.$name.text(name);

            this.$text.tooltip({
                placement: 'top',
                title: name,
                container: 'body'
            });
        },

        updateCount: function() {
            if(this.$count) {
                const count = this.model.get('count');
                this.$count.text(count === null
                    ? ''
                    : (' (' + count + ')'));
            }
        },

        updateSelected: function() {
            if(this.$check) {
                this.$check.toggleClass('hide', !this.selectedValuesCollection.get(this.model.get('value')));
            }
        },

        remove: function() {
            this.$text.tooltip('destroy');
            Backbone.View.prototype.remove.apply(this);
        }
    });
});
