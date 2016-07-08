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
    'find/app/util/collapsible'
], function(Backbone, $, _, NumericParametricFieldView, prettifyFieldName, Collapsible) {

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

            this.collapsible = new Collapsible({
                title: prettifyFieldName(this.model.id),
                subtitle: getSubtitle.call(this),
                view: new NumericParametricFieldView(_.extend(options, {hideTitle: true})),
                collapsed: false
            });

            this.listenTo(this.selectedParametricValues, 'update change:range', this.setFieldSelectedValues)
        },

        setFieldSelectedValues: function() {
            this.collapsible.setSubTitle(getSubtitle.call(this));
        },

        render: function () {
            this.$el.empty().append(this.collapsible.$el);
            this.collapsible.render();
        }
    });

});
