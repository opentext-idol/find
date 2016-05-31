/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'js-whatever/js/list-view',
    'find/app/page/search/filters/parametric/abstract-parametric-view',
    'find/app/page/search/filters/parametric/parametric-field-view',
    'parametric-refinement/display-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/parametric-view.html'
], function(Backbone, $, ListView, AbstractView, FieldView, DisplayCollection, i18n, template) {

    return AbstractView.extend({
        template: _.template(template)({i18n: i18n}),
        
        events: {
            'click [data-field] [data-value]': function(e) {
                var $target = $(e.currentTarget);
                var $field = $target.closest('[data-field]');

                var attributes = {
                    field: $field.attr('data-field'),
                    value: $target.attr('data-value')
                };

                if (this.selectedParametricValues.get(attributes)) {
                    this.selectedParametricValues.remove(attributes);
                } else {
                    this.selectedParametricValues.add(attributes);
                }
            }
        },

        initialize: function(options) {
            this.selectedParametricValues = options.queryState.selectedParametricValues;
            this.displayCollection = options.displayCollection;

            this.monitorCollection(options.parametricCollection);

            this.fieldNamesListView = new ListView({
                collection: this.displayCollection,
                ItemView: FieldView,
                itemOptions: {
                    // collection is not passed to the individual views
                    parametricDisplayCollection: this.displayCollection,
                    selectedParametricValues: this.selectedParametricValues
                }
            });
        },

        remove: function() {
            this.fieldNamesListView.remove();
            this.displayCollection.stopListening();
            Backbone.View.prototype.remove.call(this);
        },

        updateEmpty: function() {
            if (this.$emptyMessage) {
                var showEmptyMessage = this.model.get('empty') && !(this.model.get('error') || this.model.get('processing'));
                this.$emptyMessage.toggleClass('hide', !showEmptyMessage);
            }
        }
    });

});
