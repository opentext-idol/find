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

        if(model) {
            var range;
            if(this.dataType === 'numeric') {
                range = _.map(model.get('range'), function(entry) {
                    // TODO: implement proper rounding to significant figures, rather than decimal places
                    return Math.round(entry * 10) / 10;
                });
            } else if (this.dataType === 'date') {
                range = _.map(model.get('range'), function(entry) {
                    return NumericParametricFieldView.dateFormatting.format(entry);
                });
            }
            return range.join('\u2013');
        } else {
            return i18n['app.unfiltered'];
        }
    }

    return Backbone.View.extend({
        initialize: function (options) {
            this.selectedParametricValues = options.selectedParametricValues;
            this.dataType = options.dataType;
            this.timeBarModel = options.timeBarModel;

            var clickCallback = null;

            if (this.timeBarModel) {
                clickCallback = function() {
                    var isCurrentField = this.isCurrentField();

                    this.timeBarModel.set({
                        graphedDataType: isCurrentField ? null : options.dataType,
                        graphedFieldName: isCurrentField ? null : this.model.id
                    });
                }.bind(this);
            }

            this.fieldView = new NumericParametricFieldView(_.extend({
                hideTitle: true,
                clickCallback: clickCallback
            }, options));

            this.collapsible = new Collapsible({
                title: prettifyFieldName(this.model.id),
                subtitle: getSubtitle.call(this),
                view: this.fieldView,
                collapsed: true,
                renderOnOpen: true
            });

            this.listenTo(this.timeBarModel, 'change', this.updateHighlightState);
            this.listenTo(this.selectedParametricValues, 'update change:range', this.setFieldSelectedValues);
            this.listenTo(vent, 'vent:resize', this.fieldView.render.bind(this.fieldView));

            this.collapsible.$el.on('show.bs.collapse', _.bind(function() {
                this.collapsible.$('.collapsible-subtitle').removeClass('hide');
            }, this));
            this.collapsible.$el.on('hide.bs.collapse', _.bind(function() {
                this.toggleSubtitle();
            }, this));
        },

        render: function () {
            this.$el.append(this.collapsible.$el);
            this.collapsible.render();

            this.toggleSubtitle();
            this.updateHighlightState();
        },

        // Is the field represented by the view currently displayed in the time bar?
        isCurrentField: function() {
            return this.timeBarModel.get('graphedFieldName') === this.model.id && this.timeBarModel.get('graphedDataType') === this.dataType;
        },

        updateHighlightState: function () {
            if (this.timeBarModel) {
                this.fieldView.$el.toggleClass('highlighted-widget', this.isCurrentField());
            }
        },

        toggleSubtitle: function() {
            var subtitleUnfiltered = this.collapsible.$('.collapsible-subtitle').text() === i18n['app.unfiltered'];
            var widgetCollapsed = this.collapsible.$('.collapsible-header').hasClass('collapsed');

            this.collapsible.$('.collapsible-subtitle').toggleClass('hide', widgetCollapsed && subtitleUnfiltered);
        },

        setFieldSelectedValues: function() {
            this.collapsible.setSubTitle(getSubtitle.call(this));
        },

        remove: function() {
            this.collapsible.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

});
