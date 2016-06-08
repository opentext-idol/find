/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/model/find-base-collection',
    'find/app/page/search/filters/parametric/numeric-widget',
    'parametric-refinement/prettify-field-name',
    'parametric-refinement/selected-values-collection',
    'i18n!find/nls/bundle'
], function (Backbone, $, _, FindBaseCollection, numericWidget, prettifyFieldName, SelectedParametricValuesCollection, i18n) {
    "use strict";
    const GRAPH_HEIGHT = 110;

    function resetSelectedParametricValues(selectedParametricValues, fieldName) {
        const existingRestrictions = selectedParametricValues.where({field: fieldName});
        existingRestrictions.forEach(function (model) {
            selectedParametricValues.remove(model);
        });
    }

    function updateRestrictions(selectedParametricValues, fieldName, numericRestriction, min, max) {
        selectedParametricValues.add({
            field: fieldName,
            range: [min, max],
            numeric: numericRestriction
        });
    }

    function roundInputNumber(x1) {
        return Math.round(x1 * 10) / 10;
    }

    function calibrateBuckets(buckets, min, max, bucketSize) {
        let calibratedBuckets = buckets;
        if (buckets.length > 0) {
            // Remove buckets not in range when zooming in
            //noinspection JSUnresolvedFunction
            calibratedBuckets = _.filter(buckets, function (value) {
                return value.min >= min && value.max <= max;
            });

            // Add empty buckets to the beginning if zooming out
            while (calibratedBuckets[0].min > min) {
                calibratedBuckets.unshift({
                    min: Math.max(calibratedBuckets[0].min - bucketSize, min),
                    max: calibratedBuckets[0].min,
                    count: 0
                });
            }

            // Add empty buckets to the end if zooming out
            //noinspection JSUnresolvedFunction
            while (_.last(calibratedBuckets).max < max) {
                //noinspection JSUnresolvedFunction
                calibratedBuckets.push({
                    min: _.last(calibratedBuckets).max,
                    max: Math.min(_.last(calibratedBuckets).max + bucketSize, max),
                    count: 0
                });
            }
        }
        
        return calibratedBuckets;
    }

    return Backbone.View.extend({
        className: 'animated fadeIn',

        events: {
            'click .numeric-parametric-no-min': function () {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateMinInput(this.absoluteMinValue, true);
            },
            'click .numeric-parametric-no-max': function () {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateMaxInput(this.absoluteMaxValue, true);
            },
            'click .numeric-parametric-reset': function () {
                //noinspection JSUnresolvedVariable
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName);
                //noinspection JSUnresolvedVariable,JSUnresolvedFunction
                this.updateModel(this.absoluteMinValue, this.absoluteMaxValue);
            },
            'change .numeric-parametric-min-input': function () {
                //noinspection JSUnresolvedVariable,JSUnresolvedFunction
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, this.readMinInput(), this.readMaxInput())();
                //noinspection JSUnresolvedFunction
                this.drawSelection();
            },
            'change .numeric-parametric-max-input': function () {
                //noinspection JSUnresolvedVariable,JSUnresolvedFunction
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, this.readMinInput(), this.readMaxInput());
                //noinspection JSUnresolvedFunction
                this.drawSelection();
            },
            'dp.change .results-filter-date[data-date-attribute="min-date"]': function (event) {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateMinInput(event.date.unix(), true);
            },
            'dp.change .results-filter-date[data-date-attribute="max-date"]': function (event) {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateMaxInput(event.date.unix(), true);
            }
        },

        initialize: function (options) {
            this.template = _.template(options.template);
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.pixelsPerBucket = options.pixelsPerBucket;
            this.viewWidth = options.viewWidth;
            this.selectionEnabled = options.selectionEnabled;
            this.zoomEnabled = options.zoomEnabled;
            this.buttonsEnabled = options.selectionEnabled && options.buttonsEnabled;
            this.fieldName = this.model.id;
            this.numericRestriction = options.numericRestriction;
            //noinspection JSUnresolvedVariable
            this.formatValue = options.stringFormatting && options.stringFormatting.format || _.identity;
            //noinspection JSUnresolvedVariable
            this.parseValue = options.stringFormatting && options.stringFormatting.parse || _.identity;
            //noinspection JSUnresolvedVariable
            this.renderCustomFormatting = options.stringFormatting && options.stringFormatting.render || _.noop;
            this.widget = numericWidget({
                formattingFn: this.formatValue
            });
            this.localBucketingCollection = new (FindBaseCollection.extend({
                url: '../api/public/parametric/buckets'
            }))();

            this.absoluteMinValue = this.model.get('min');
            this.absoluteMaxValue = this.model.get('max');
        },

        render: function () {
            //noinspection JSUnresolvedVariable,JSUnresolvedFunction
            this.$el.empty().append(this.template({
                i18n: i18n,
                fieldName: prettifyFieldName(this.model.get('name')),
                id: _.uniqueId('numeric-parametric-field'),
                selectionEnabled: this.selectionEnabled,
                buttonsEnabled: this.buttonsEnabled
            }));

            if (this.selectionEnabled) {
                //noinspection JSUnresolvedVariable
                this.renderCustomFormatting(this.$el);

                //noinspection JSUnresolvedFunction
                this.$minInput = this.$('.numeric-parametric-min-input');
                //noinspection JSUnresolvedFunction
                this.$maxInput = this.$('.numeric-parametric-max-input');

                this.updateMinInput(this.absoluteMinValue);
                this.updateMaxInput(this.absoluteMaxValue);
            }

            const updateCallback = function (x1, x2) {
                // rounding to one decimal place
                this.updateMinInput(Math.max(roundInputNumber(x1), this.model.get('min')));
                this.updateMaxInput(Math.min(roundInputNumber(x2), this.model.get('max')));
            }.bind(this);
            const selectionCallback = function (x1, x2) {
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, Math.max(x1, this.model.get('min')), Math.min(x2, this.model.get('max')));
            }.bind(this);
            const deselectionCallback = function () {
                this.updateMinInput(this.absoluteMinValue);
                this.updateMaxInput(this.absoluteMaxValue);
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName);
            }.bind(this);
            const zoomCallback = function (newMin, newMax) {
                this.updateModel(newMin, newMax);
            }.bind(this);
            //noinspection JSUnresolvedFunction
            const buckets = calibrateBuckets(this.model.get('values'), this.model.get('min'), this.model.get('max'), this.model.get('bucketSize'));
            //noinspection JSUnresolvedFunction
            this.graph = this.widget.drawGraph({
                chart: this.$('.chart')[0],
                data: {
                    buckets: buckets,
                    bucketSize: this.model.get('bucketSize'),
                    maxCount: _.max(_.pluck(buckets, 'count')),
                    minValue: this.model.get('min'),
                    maxValue: this.model.get('max')
                },
                updateCallback: updateCallback,
                selectionCallback: selectionCallback,
                deselectionCallback: deselectionCallback,
                zoomCallback: zoomCallback,
                xRange: this.viewWidth,
                yRange: GRAPH_HEIGHT,
                tooltip: i18n['search.numericParametricFields.tooltip'],
                dragEnabled: this.selectionEnabled,
                zoomEnabled: this.zoomEnabled
            });

            this.drawSelection();
        },
        
        drawSelection: function () {
            const graph = this.graph;
            
            this.selectedParametricValues.where({
                field: this.fieldName
            }).forEach(function (restriction) {
                const range = restriction.get('range');
                if (range) {
                    this.updateMinInput(roundInputNumber(range[0]));
                    this.updateMaxInput(roundInputNumber(range[1]));

                    graph.selectionRect.init(graph.chart, GRAPH_HEIGHT, graph.scale.barWidth(range[0]));
                    graph.selectionRect.update(graph.scale.barWidth(range[1]));
                    graph.selectionRect.focus();
                }
            }, this);
        },

        updateModel: function (newMin, newMax) {
            // immediate update, just rescaling the existing buckets
            this.model.set({
                min: newMin,
                max: newMax
            });
    
            // proper update, regenerating the buckets from the given bounds
            //noinspection JSUnresolvedVariable
            this.localBucketingCollection.fetch({
                data: {
                    fieldNames: [this.model.get('id')],
                    databases: this.queryModel.get('indexes'),
                    queryText: this.queryModel.get('queryText'),
                    targetNumberOfBuckets: [Math.floor(this.$el.width() / this.pixelsPerBucket)],
                    bucketMin: [newMin],
                    bucketMax: [newMax]
                },
                reset: true,
                remove: true,
                success: function() {
                    const result = this.localBucketingCollection.models[0];
                    this.model.set({
                        count: result.get('count'),
                        min: result.get('min'),
                        max: result.get('max'),
                        bucketSize: result.get('bucketSize'),
                        values: result.get('values')
                    });
                }.bind(this)
            });
        },

        readMinInput: function () {
            return this.parseValue(this.$minInput.val());
        },

        readMaxInput: function () {
            return this.parseValue(this.$maxInput.val());
        },

        updateMinInput: function (newValue, triggerChange) {
            if (triggerChange) {
                const $minInput = this.$minInput;
                if (newValue !== this.readMinInput()) {
                    this.$minInput.val(this.formatValue(newValue));
                    $minInput.trigger('change');
                }
            } else {
                this.$minInput.val(this.formatValue(newValue));
            }
        },

        updateMaxInput: function (newValue, triggerChange) {
            if (triggerChange) {
                const $maxInput = this.$maxInput;
                if (newValue !== this.readMaxInput()) {
                    this.$maxInput.val(this.formatValue(newValue));
                    $maxInput.trigger('change');
                }
            } else {
                this.$maxInput.val(this.formatValue(newValue));
            }
        }
    });
});