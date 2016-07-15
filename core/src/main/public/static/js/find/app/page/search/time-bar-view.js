/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'find/app/model/bucketed-parametric-collection',
    'parametric-refinement/prettify-field-name',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/search/time-bar-view.html'
], function(Backbone, _, $, vent, i18n, NumericParametricFieldView, BucketedParametricCollection, prettifyFieldName, loadingSpinnerTemplate, timeBarTemplate) {

    var PIXELS_PER_BUCKET = 20;

    var graphConfiguration = {
        date: {
            template: NumericParametricFieldView.dateInputTemplate,
            formatting: NumericParametricFieldView.dateFormatting
        },
        numeric: {
            template: NumericParametricFieldView.numericInputTemplate,
            formatting: NumericParametricFieldView.defaultFormatting
        }
    };

    return Backbone.View.extend({
        className: 'middle-container-time-bar',
        graphView: null,
        loadingSpinnerHtml: _.template(loadingSpinnerTemplate)({i18n: i18n, large: true}),
        errorHtml: _.template('<p class="p-t-xl text-center"><%-i18n["search.timeBar.error"]%></p>')({i18n: i18n}),
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

            this.listenTo(vent, 'vent:resize', this.render);
            this.listenTo(options.previewModeModel, 'change:document', this.render);
        },

        render: function() {
            this.destroyGraph();
            this.abortActiveRequest();

            if (this.bucketModel) {
                this.stopListening(this.bucketModel);
            }

            this.$el.html(this.template({
                fieldName: prettifyFieldName(this.fieldName)
            }));

            var $loadingSpinner = $(this.loadingSpinnerHtml);
            var $content = this.$('.middle-container-time-bar-content');
            $content.append($loadingSpinner);

            this.bucketModel = new BucketedParametricCollection.Model({id: this.fieldName, name: this.fieldName});

            var currentGraphConfig = graphConfiguration[this.dataType];

            this.activeRequest = this.bucketModel
                .fetch({
                    data: {
                        targetNumberOfBuckets: Math.floor(this.$el.width() / PIXELS_PER_BUCKET)
                    }
                })
                .fail(function() {
                    $content.append(this.errorHtml);
                }.bind(this))
                .done(function() {
                    this.graphView = new NumericParametricFieldView({
                        buttonsEnabled: true,
                        pixelsPerBucket: PIXELS_PER_BUCKET,
                        queryModel: this.queryModel,
                        selectionEnabled: true,
                        selectedParametricValues: this.selectedParametricValues,
                        zoomEnabled: true,
                        dataType: this.dataType,
                        model: this.bucketModel,
                        formatting: currentGraphConfig.formatting,
                        inputTemplate: currentGraphConfig.template
                    });

                    $content.append(this.graphView.$el);
                    this.graphView.render();
                    this.listenTo(this.bucketModel, 'change', this.graphView.render.bind(this.graphView));
                }.bind(this))
                .always(function() {
                    $loadingSpinner.remove();
                }.bind(this));
        },

        abortActiveRequest: function() {
            if (this.activeRequest) {
                this.activeRequest.abort();
            }
        },

        destroyGraph: function() {
            if (this.graphView) {
                this.graphView.remove();
                this.graphView = null;
            }
        },

        remove: function() {
            this.abortActiveRequest();
            this.destroyGraph();
            Backbone.View.prototype.remove.call(this);
        }
    });
});
