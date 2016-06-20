/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'moment',
    'find/app/model/find-base-collection',
    'find/app/page/search/filters/parametric/numeric-widget',
    'parametric-refinement/prettify-field-name',
    'parametric-refinement/selected-values-collection',
    'i18n!find/nls/bundle'
], function(Backbone, $, _, moment, FindBaseCollection, numericWidget, prettifyFieldName, SelectedParametricValuesCollection, i18n) {

    'use strict';

    const GRAPH_HEIGHT = 110;
    const DATE_WIDGET_FORMAT = 'YYYY-MM-DD HH:mm';

    function resetSelectedParametricValues(selectedParametricValues, fieldName) {
        selectedParametricValues
            .where({field: fieldName})
            .forEach(function(model) {
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
            calibratedBuckets = _.filter(buckets, function(value) {
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

    const NumericParametricFieldView = Backbone.View.extend({
        className: 'animated fadeIn',

        events: {
            'click .numeric-parametric-no-min': function() {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateMinInput(this.absoluteMinValue, true);
            },
            'click .numeric-parametric-no-max': function() {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateMaxInput(this.absoluteMaxValue, true);
            },
            'click .numeric-parametric-reset': function() {
                //noinspection JSUnresolvedVariable
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName);
                //noinspection JSUnresolvedVariable,JSUnresolvedFunction
                this.updateModel(this.absoluteMinValue, this.absoluteMaxValue);
            },
            'change .numeric-parametric-min-input': function() {
                //noinspection JSUnresolvedVariable,JSUnresolvedFunction
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, this.readMinInput(), this.readMaxInput());
                //noinspection JSUnresolvedFunction
                this.drawSelection();
            },
            'change .numeric-parametric-max-input': function() {
                //noinspection JSUnresolvedVariable,JSUnresolvedFunction
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, this.readMinInput(), this.readMaxInput());
                //noinspection JSUnresolvedFunction
                this.drawSelection();
            },
            'dp.change .results-filter-date[data-date-attribute="min-date"]': function(event) {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateMinInput(event.date.unix(), true);
            },
            'dp.change .results-filter-date[data-date-attribute="max-date"]': function(event) {
                //noinspection JSUnresolvedFunction,JSUnresolvedVariable
                this.updateMaxInput(event.date.unix(), true);
            }
        },

        initialize: function(options) {
            this.template = _.template(options.template);
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.pixelsPerBucket = options.pixelsPerBucket;
            this.viewWidth = options.viewWidth;
            this.selectionEnabled = options.selectionEnabled;
            this.zoomEnabled = options.zoomEnabled;
            this.buttonsEnabled = options.selectionEnabled && options.buttonsEnabled;
            this.coordinatesEnabled = options.coordinatesEnabled === undefined ? true : options.coordinatesEnabled;
            this.fieldName = this.model.id;
            this.numericRestriction = options.numericRestriction;

            const formatting = options.formatting  || NumericParametricFieldView.defaultFormatting;
            this.formatValue = formatting.format;
            this.parseValue = formatting.parse;
            this.renderCustomFormatting = formatting.render;
            this.parseBoundarySelection = formatting.parseBoundarySelection;

            this.widget = numericWidget({formattingFn: this.formatValue});
            this.localBucketingCollection = new (FindBaseCollection.extend({url: '../api/public/parametric/buckets'}))();

            this.absoluteMinValue = this.model.get('min');
            this.absoluteMaxValue = this.model.get('max');
        },

        render: function() {
            const inputColumnClass = this.selectionEnabled ? (this.coordinatesEnabled ? 'col-xs-4' : 'col-xs-6') : 'hide';
            const coordinatesColumnClass = this.coordinatesEnabled ? (this.selectionEnabled ? 'col-xs-4' : 'col-xs-12') : 'hide';

            this.$el
                .empty()
                .append(this.template({
                    i18n: i18n,
                    fieldName: prettifyFieldName(this.model.get('name')),
                    buttonsEnabled: this.buttonsEnabled,
                    inputsRowClass: this.selectionEnabled || this.coordinatesEnabled ? '' : 'hide',
                    inputColumnClass: inputColumnClass,
                    coordinatesColumnClass: coordinatesColumnClass
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

            const updateCallback = function(x1, x2) {
                // rounding to one decimal place
                this.updateMinInput(Math.max(roundInputNumber(x1), this.model.get('min')));
                this.updateMaxInput(Math.min(roundInputNumber(x2), this.model.get('max')));
            }.bind(this);

            const selectionCallback = function(x1, x2) {
                var newMin = this.parseBoundarySelection(Math.max(x1, this.model.get('min')));
                var newMax = this.parseBoundarySelection(Math.min(x2, this.model.get('max')));
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, newMin, newMax);
            }.bind(this);

            const deselectionCallback = function() {
                this.updateMinInput(this.absoluteMinValue);
                this.updateMaxInput(this.absoluteMaxValue);
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName);
            }.bind(this);

            const mouseMoveCallback = function(x) {
                //noinspection JSPotentiallyInvalidUsageOfThis
                this.$('.numeric-parametric-co-ordinates').text(this.formatValue(Math.min(roundInputNumber(x), this.model.get('max'))));
            }.bind(this);

            const mouseLeaveCallback = function() {
                //noinspection JSPotentiallyInvalidUsageOfThis
                this.$('.numeric-parametric-co-ordinates').text('');
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
                mouseMoveCallback: mouseMoveCallback,
                mouseLeaveCallback: mouseLeaveCallback,
                zoomCallback: this.updateModel.bind(this),
                xRange: this.viewWidth,
                yRange: GRAPH_HEIGHT,
                tooltip: i18n['search.numericParametricFields.tooltip'],
                dragEnabled: this.selectionEnabled,
                zoomEnabled: this.zoomEnabled,
                coordinatesEnabled: this.coordinatesEnabled
            });

            this.drawSelection();
        },

        drawSelection: function() {
            this.selectedParametricValues
                .where({field: this.fieldName})
                .forEach(function(restriction) {
                    const range = restriction.get('range');

                    if (range) {
                        this.updateMinInput(roundInputNumber(range[0]));
                        this.updateMaxInput(roundInputNumber(range[1]));

                        this.graph.selectionRect.init(this.graph.chart, GRAPH_HEIGHT, this.graph.scale.barWidth(range[0]));
                        this.graph.selectionRect.update(this.graph.scale.barWidth(range[1]));
                        this.graph.selectionRect.focus();
                    }
                }.bind(this));
        },

        updateModel: function(newMin, newMax) {
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

        readMinInput: function() {
            return this.parseValue(this.$minInput.val());
        },

        readMaxInput: function() {
            return this.parseValue(this.$maxInput.val());
        },

        updateMinInput: function(newValue, triggerChange) {
            if (this.selectionEnabled) {
                if (triggerChange) {
                    if (newValue !== this.readMinInput()) {
                        this.$minInput
                            .val(this.formatValue(newValue))
                            .trigger('change');
                    }
                } else {
                    this.$minInput.val(this.formatValue(newValue));
                }
            }
        },

        updateMaxInput: function(newValue, triggerChange) {
            if (this.selectionEnabled) {
                if (triggerChange) {
                    if (newValue !== this.readMaxInput()) {
                        this.$maxInput
                            .val(this.formatValue(newValue))
                            .trigger('change');
                    }
                } else {
                    this.$maxInput.val(this.formatValue(newValue));
                }
            }
        }
    }, {
        defaultFormatting: {
            format: _.identity,
            parse: _.identity,
            parseBoundarySelection: _.identity,
            render: _.noop
        },
        dateFormatting: {
            format: function(unformattedString) {
                return moment(unformattedString * 1000).format(DATE_WIDGET_FORMAT);
            },
            parse: function(formattedString) {
                return moment(formattedString, DATE_WIDGET_FORMAT).unix();
            },
            parseBoundarySelection: function(input) {
                // Epoch seconds are not decimal
                return Math.round(input);
            },
            render: function($el) {
                $el.find('.results-filter-date').datetimepicker({
                    format: DATE_WIDGET_FORMAT,
                    icons: {
                        time: 'hp-icon hp-fw hp-clock',
                        date: 'hp-icon hp-fw hp-calendar',
                        up: 'hp-icon hp-fw hp-chevron-up',
                        down: 'hp-icon hp-fw hp-chevron-down',
                        next: 'hp-icon hp-fw hp-chevron-right',
                        previous: 'hp-icon hp-fw hp-chevron-left'
                    }
                });
            }
        }
    });

    return NumericParametricFieldView;

});