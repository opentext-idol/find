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
    'find/app/model/bucketed-parametric-collection',
    'parametric-refinement/prettify-field-name',
    'parametric-refinement/selected-values-collection',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view-numeric-input.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view-date-input.html',
    'i18n!find/nls/bundle'
], function(Backbone, $, _, moment, FindBaseCollection, numericWidget, BucketedParametricCollection, prettifyFieldName,
            SelectedParametricValuesCollection, template, numericInputTemplate, dateInputTemplate, i18n) {

    'use strict';

    var GRAPH_HEIGHT = 110;
    var DATE_WIDGET_FORMAT = 'YYYY-MM-DD HH:mm';

    function rangeModelMatching(fieldName, dataType) {
        return function(model) {
            return model.get('field') === fieldName && model.get('range') && model.get('dataType') === dataType;
        };
    }

    function roundInputNumber(input) {
        return Math.round(input * 10) / 10;
    }

    function calibrateBuckets(buckets, min, max, bucketSize) {
        // Remove buckets not in range when zooming in
        //noinspection JSUnresolvedFunction
        var calibratedBuckets = _.filter(buckets, function(value) {
            return value.min >= min && value.max <= max;
        });

        if (calibratedBuckets.length > 0) {
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
        } else {
            // Zooming where there are no buckets
            calibratedBuckets = _.range(min, max, bucketSize).map(function(bucketMin) {
                return {
                    min: bucketMin,
                    max: bucketMin + bucketSize,
                    count: 0
                };
            });
        }

        return calibratedBuckets;
    }

    // This view must be visible before it is rendered
    var NumericParametricFieldView = Backbone.View.extend({
        className: 'animated fadeIn',
        template: _.template(template),

        events: {
            'click .numeric-parametric-no-min': function() {
                this.updateRestrictions([this.absoluteMinValue, null]);
            },
            'click .numeric-parametric-no-max': function() {
                this.updateRestrictions([null, this.absoluteMaxValue]);
            },
            'click .numeric-parametric-reset': function() {
                this.clearRestrictions();
                this.updateModel(this.absoluteMinValue, this.absoluteMaxValue);
            },
            'change .numeric-parametric-min-input': function() {
                this.updateRestrictions([this.readMinInput(), null]);
            },
            'change .numeric-parametric-max-input': function() {
                this.updateRestrictions([null, this.readMaxInput()]);
            },
            'dp.change .results-filter-date[data-date-attribute="min-date"]': function(event) {
                this.updateRestrictions([event.date.unix(), null]);
            },
            'dp.change .results-filter-date[data-date-attribute="max-date"]': function(event) {
                this.updateRestrictions([null, event.date.unix()]);
            },
            'click .clickable-widget': function() {
                this.clickCallback();
            }
        },

        initialize: function(options) {
            this.inputTemplate = options.inputTemplate || NumericParametricFieldView.numericInputTemplate;
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.pixelsPerBucket = options.pixelsPerBucket;
            this.selectionEnabled = options.selectionEnabled;
            this.zoomEnabled = options.zoomEnabled;
            this.buttonsEnabled = options.selectionEnabled && options.buttonsEnabled;
            this.coordinatesEnabled = options.coordinatesEnabled === undefined ? true : options.coordinatesEnabled;
            this.numericRestriction = options.numericRestriction || false;
            this.hideTitle = options.hideTitle;
            this.dataType = options.dataType;
            this.clickCallback = options.clickCallback;

            this.fieldName = this.model.get('id');

            var formatting = options.formatting || NumericParametricFieldView.defaultFormatting;
            this.formatValue = formatting.format;
            this.parseValue = formatting.parse;
            this.renderCustomFormatting = formatting.render;
            this.parseBoundarySelection = formatting.parseBoundarySelection;

            this.widget = numericWidget({formattingFn: this.formatValue});

            this.bucketModel = new BucketedParametricCollection.Model({
                id: this.model.get('id'),
                name: this.model.get('name')
            });

            this.absoluteMinValue = this.model.get('min');
            this.absoluteMaxValue = this.model.get('max');

            // Bind the selection rectangle to the selected parametric range
            this.listenTo(this.selectedParametricValues, 'add remove change', function(model) {
                if (rangeModelMatching(this.fieldName, this.dataType)(model)) {
                    this.updateSelection();
                }
            });
        },

        render: function() {
            var inputColumnClass = this.selectionEnabled ? (this.coordinatesEnabled ? 'col-xs-4' : 'col-xs-6') : 'hide';
            var coordinatesColumnClass = this.coordinatesEnabled ? (this.selectionEnabled ? 'col-xs-4' : 'col-xs-12') : 'hide';

            this.$el
                .empty()
                .append(this.template({
                    i18n: i18n,
                    theFieldName: this.model.get('id'),
                    clickable: Boolean(this.clickCallback),
                    buttonsEnabled: this.buttonsEnabled,
                    inputsRowClass: this.selectionEnabled || this.coordinatesEnabled ? '' : 'hide',
                    inputColumnClass: inputColumnClass,
                    inputTemplate: this.inputTemplate,
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

            // Update the inputs as the user drags a selection on the graph. Note that this means the value in the input
            // does not depend on just the selected parametric range model.
            var updateCallback = function(x1, x2) {
                // rounding to one decimal place
                this.updateMinInput(Math.max(roundInputNumber(x1), this.model.get('min')));
                this.updateMaxInput(Math.min(roundInputNumber(x2), this.model.get('max')));
            }.bind(this);

            var selectionCallback = function(x1, x2) {
                var newMin = this.parseBoundarySelection(Math.max(x1, this.model.get('min')));
                var newMax = this.parseBoundarySelection(Math.min(x2, this.model.get('max')));
                this.updateRestrictions([newMin, newMax]);
            }.bind(this);

            var mouseMoveCallback = function(x) {
                //noinspection JSPotentiallyInvalidUsageOfThis
                this.$('.numeric-parametric-co-ordinates').text(this.formatValue(Math.min(roundInputNumber(x), this.model.get('max'))));
            }.bind(this);

            var mouseLeaveCallback = function() {
                //noinspection JSPotentiallyInvalidUsageOfThis
                this.$('.numeric-parametric-co-ordinates').text('');
            }.bind(this);

            var $chart = this.$('.chart');

            //noinspection JSUnresolvedFunction
            var buckets = calibrateBuckets(this.model.get('values'), this.model.get('min'), this.model.get('max'), this.model.get('bucketSize'));

            //noinspection JSUnresolvedFunction
            this.graph = this.widget.drawGraph({
                chart: $chart[0],
                data: {
                    buckets: buckets,
                    bucketSize: this.model.get('bucketSize'),
                    maxCount: _.max(_.pluck(buckets, 'count')),
                    minValue: this.model.get('min'),
                    maxValue: this.model.get('max')
                },
                updateCallback: updateCallback,
                selectionCallback: selectionCallback,
                deselectionCallback: this.clearRestrictions.bind(this),
                mouseMoveCallback: mouseMoveCallback,
                mouseLeaveCallback: mouseLeaveCallback,
                zoomCallback: this.updateModel.bind(this),
                xRange: $chart.width(),
                yRange: GRAPH_HEIGHT,
                tooltip: i18n['search.numericParametricFields.tooltip'],
                dragEnabled: this.selectionEnabled,
                zoomEnabled: this.zoomEnabled,
                coordinatesEnabled: this.coordinatesEnabled
            });

            if (this.selectionEnabled) {
                // see http://stackoverflow.com/questions/8638621/jquery-svg-why-cant-i-addclass for why this is necessary
                $chart.attr('class', $chart.attr('class') + ' chart-selection-enabled');
            }

            this.updateSelection();
        },

        // Update the rendered selection rectangle and inputs to match the selected parametric range model
        updateSelection: function() {
            if (this.graph) {
                var rangeModel = this.selectedParametricValues.find(rangeModelMatching(this.fieldName, this.dataType));

                if (rangeModel) {
                    var range = rangeModel.get('range');

                    this.updateMinInput(roundInputNumber(range[0]));
                    this.updateMaxInput(roundInputNumber(range[1]));

                    this.graph.setSelection(range);
                } else {
                    this.updateMinInput(this.absoluteMinValue);
                    this.updateMaxInput(this.absoluteMaxValue);
                    this.graph.clearSelection();
                }
            }
        },

        // Apply a new range selection; a null boundary will not be updated
        updateRestrictions: function(newRange) {
            var existingModel = this.selectedParametricValues.find(rangeModelMatching(this.fieldName, this.dataType));
            var existingRange = existingModel ? existingModel.get('range') : [this.absoluteMinValue, this.absoluteMaxValue];

            var newAttributes = {
                field: this.fieldName,
                dataType: this.dataType,
                // TODO: Replace numeric with the more expressive dataType
                numeric: this.dataType === 'numeric',
                range: _.map(newRange, function(value, index) {
                    // Explicitly check null since 0 is falsy
                    return value === null ? existingRange[index] : value;
                })
            };

            if (existingModel) {
                existingModel.set(newAttributes);
            } else {
                this.selectedParametricValues.add(newAttributes);
            }
        },

        clearRestrictions: function () {
            this.selectedParametricValues.remove(this.selectedParametricValues.filter(rangeModelMatching(this.fieldName, this.dataType)));
        },

        updateModel: function(newMin, newMax) {
            // immediate update, just rescaling the existing buckets
            this.model.set({
                min: newMin,
                max: newMax
            });

            // proper update, regenerating the buckets from the given bounds
            this.bucketModel.fetch({
                data: {
                    databases: this.queryModel.get('indexes'),
                    targetNumberOfBuckets: Math.floor(this.$el.width() / this.pixelsPerBucket),
                    bucketMin: newMin,
                    bucketMax: newMax
                },
                reset: true,
                remove: true,
                success: function() {
                    this.model.set(this.bucketModel.attributes);
                }.bind(this)
            });
        },

        readMinInput: function() {
            return this.parseValue(this.$minInput.val());
        },

        readMaxInput: function() {
            return this.parseValue(this.$maxInput.val());
        },

        updateMinInput: function(newValue) {
            if (this.selectionEnabled) {
                this.$minInput.val(this.formatValue(newValue));
            }
        },

        updateMaxInput: function(newValue) {
            if (this.selectionEnabled) {
                this.$maxInput.val(this.formatValue(newValue));
            }
        }
    }, {
        dateInputTemplate: _.template(dateInputTemplate),
        numericInputTemplate: _.template(numericInputTemplate),
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