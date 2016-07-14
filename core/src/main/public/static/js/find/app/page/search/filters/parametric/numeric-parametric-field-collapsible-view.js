/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'parametric-refinement/prettify-field-name',
    'find/app/util/collapsible',
    'find/app/vent'
], function(Backbone, $, _, i18n, NumericParametricFieldView, prettifyFieldName, Collapsible, vent) {

    'use strict';

    function getSubtitle() {
        var model = this.selectedParametricValues.findWhere({field: this.model.id});

        if (model) {
            var isNumeric = model.get('numeric');

            var range = _.map(model.get('range'), function (entry) {
                return isNumeric ? Math.round(entry * 100) / 100 : NumericParametricFieldView.dateFormatting.format(entry);
            }).join(' \u2014 ');

            return range;
        } else {
            return i18n['app.unfiltered'];
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
            
            this.collapsible.$el.on('show.bs.collapse', _.bind(function() {
                this.collapsible.$('.collapsible-subtitle').removeClass('hide');
            }, this));
            this.collapsible.$el.on('hide.bs.collapse', _.bind(function() {
                this.toggleSubtitle();
            }, this));
        },

        toggleSubtitle: function() {
            var hideSubtitle = this.collapsible.$('.collapsible-subtitle').text() == i18n['app.unfiltered'];
            this.collapsible.$('.collapsible-subtitle').toggleClass('hide', hideSubtitle);
        },

        setFieldSelectedValues: function() {
            this.collapsible.setSubTitle(getSubtitle.call(this));
        },

        render: function () {
            this.$el.append(this.collapsible.$el);
            this.collapsible.render();

            this.toggleSubtitle();
        },

        remove: function() {
            this.collapsible.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

});
