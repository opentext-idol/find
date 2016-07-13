/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'parametric-refinement/prettify-field-name',
    'find/app/vent',
    'find/app/util/collapsible'
], function(Backbone, $, _, NumericParametricFieldView, prettifyFieldName, vent, Collapsible) {

    'use strict';

    function getSubtitle() {
        var model = this.selectedParametricValues.findWhere({field: this.model.id});

        if (model) {
            var isNumeric = model.get('numeric');

            var range = _.map(model.get('range'), function (entry) {
                return isNumeric ? Math.round(entry * 100) / 100 : NumericParametricFieldView.dateFormatting.format(entry);
            }).join(' - ');

            return range;
        } else {
            return '';
        }
    }

    return Backbone.View.extend({
        initialize: function (options) {
            this.selectedParametricValues = options.selectedParametricValues;

            var fieldView = new NumericParametricFieldView(_.extend(options, {hideTitle: true}));

            this.collapsible = new Collapsible({
                title: prettifyFieldName(this.model.id),
                subtitle: getSubtitle.call(this),
                view: fieldView,
                collapsed: true,
                renderOnOpen: true
            });

            this.listenTo(this.selectedParametricValues, 'update change:range', this.setFieldSelectedValues);
            this.listenTo(vent, 'vent:resize', fieldView.render.bind(fieldView));
        },

        setFieldSelectedValues: function() {
            this.collapsible.setSubTitle(getSubtitle.call(this));
        },

        render: function () {
            this.$el.append(this.collapsible.$el);
            this.collapsible.render();
        },

        remove: function() {
            this.collapsible.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

});
