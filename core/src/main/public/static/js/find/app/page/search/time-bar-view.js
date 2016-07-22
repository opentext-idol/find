/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'parametric-refinement/prettify-field-name',
    'text!find/templates/app/page/search/time-bar-view.html'
], function(Backbone, _, $, i18n, NumericParametricFieldView, prettifyFieldName, timeBarTemplate) {

    var PIXELS_PER_BUCKET = 20;

    var graphConfiguration = {
        date: {
            collectionName: 'dateParametricFieldsCollection',
            template: NumericParametricFieldView.dateInputTemplate,
            formatting: NumericParametricFieldView.dateFormatting
        },
        numeric: {
            collectionName: 'numericParametricFieldsCollection',
            template: NumericParametricFieldView.numericInputTemplate,
            formatting: NumericParametricFieldView.defaultFormatting
        }
    };

    return Backbone.View.extend({
        className: 'middle-container-time-bar',
        template: _.template(timeBarTemplate),

        events: {
            'click .time-bar-container-icon': function() {
                this.timeBarModel.set({
                    graphedFieldName: null,
                    graphedDataType: null
                });
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.queryState.selectedParametricValues;
            this.timeBarModel = options.timeBarModel;

            this.fieldName = this.timeBarModel.get('graphedFieldName');
            this.dataType = this.timeBarModel.get('graphedDataType');

            var currentGraphConfig = graphConfiguration[this.dataType];
            var fieldModel = options[currentGraphConfig.collectionName].get(this.fieldName);

            this.graphView = new NumericParametricFieldView({
                buttonsEnabled: true,
                pixelsPerBucket: PIXELS_PER_BUCKET,
                queryModel: this.queryModel,
                selectionEnabled: true,
                selectedParametricValues: this.selectedParametricValues,
                zoomEnabled: true,
                dataType: this.dataType,
                numericRestriction: this.dataType === 'numeric',
                model: fieldModel,
                formatting: currentGraphConfig.formatting,
                inputTemplate: currentGraphConfig.template
            });

            this.listenTo(options.previewModeModel, 'change:document', this.graphView.render.bind(this.graphView));
        },

        render: function() {
            this.$el.html(this.template({
                fieldName: prettifyFieldName(this.fieldName)
            }));

            this.$('.middle-container-time-bar-content').append(this.graphView.$el);
            this.graphView.render();
        },

        remove: function() {
            this.graphView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });
});
