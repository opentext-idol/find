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
    'i18n!find/nls/bundle'
], function(Backbone, $, _, moment, FindBaseCollection, numericWidget, BucketedParametricCollection, prettifyFieldName, SelectedParametricValuesCollection, i18n) {

    'use strict';

    var GRAPH_HEIGHT = 110;
    var DATE_WIDGET_FORMAT = 'YYYY-MM-DD HH:mm';

    function rangeModelMatching(fieldName, numericRestriction) {
        return function(model) {
            return model.get('field') === fieldName && model.get('range') && model.get('numeric') === numericRestriction;
        };
    }

    function resetSelectedParametricValues(selectedParametricValues, fieldName, numericRestriction) {
        selectedParametricValues.remove(selectedParametricValues.filter(rangeModelMatching(fieldName, numericRestriction)));
    }

    function updateRestrictions(selectedParametricValues, fieldName, numericRestriction, min, max) {
        var existing = selectedParametricValues.find(rangeModelMatching(fieldName, numericRestriction));

        var newAttributes = {
            field: fieldName,
            range: [min, max],
            numeric: numericRestriction
        };

        if (existing) {
            existing.set(newAttributes);
        } else {
            selectedParametricValues.add(newAttributes);
        }
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

    var NumericParametricFieldView = Backbone.View.extend({
        className: 'animated fadeIn',

        events: {
            'click .numeric-parametric-no-min': function() {
                this.updateMinInput(this.absoluteMinValue);
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, this.readMinInput(), this.readMaxInput());
                this.drawSelection();
            },
            'click .numeric-parametric-no-max': function() {
                this.updateMaxInput(this.absoluteMaxValue);
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, this.readMinInput(), this.readMaxInput());
                this.drawSelection();
            },
            'click .numeric-parametric-reset': function() {
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName, this.numericRestriction);
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
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, event.date.unix(), this.readMaxInput());
                this.drawSelection();
            },
            'dp.change .results-filter-date[data-date-attribute="max-date"]': function(event) {
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, this.readMinInput(), event.date.unix());
                this.drawSelection();
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
            this.numericRestriction = options.numericRestriction || false;

            this.fieldName = this.model.id;

            var formatting = options.formatting  || NumericParametricFieldView.defaultFormatting;
            this.formatValue = formatting.format;
            this.parseValue = formatting.parse;
            this.renderCustomFormatting = formatting.render;
            this.parseBoundarySelection = formatting.parseBoundarySelection;

            this.widget = numericWidget({formattingFn: this.formatValue});

            this.bucketModel = new BucketedParametricCollection.Model({
                id: this.model.get('id'),
                name: this.model.get('id')
            });

            this.absoluteMinValue = this.model.get('min');
            this.absoluteMaxValue = this.model.get('max');
        },

        render: function() {
            var inputColumnClass = this.selectionEnabled ? (this.coordinatesEnabled ? 'col-xs-4' : 'col-xs-6') : 'hide';
            var coordinatesColumnClass = this.coordinatesEnabled ? (this.selectionEnabled ? 'col-xs-4' : 'col-xs-12') : 'hide';

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

            var updateCallback = function(x1, x2) {
                // rounding to one decimal place
                this.updateMinInput(Math.max(roundInputNumber(x1), this.model.get('min')));
                this.updateMaxInput(Math.min(roundInputNumber(x2), this.model.get('max')));
            }.bind(this);

            var selectionCallback = function(x1, x2) {
                var newMin = this.parseBoundarySelection(Math.max(x1, this.model.get('min')));
                var newMax = this.parseBoundarySelection(Math.min(x2, this.model.get('max')));
                updateRestrictions(this.selectedParametricValues, this.fieldName, this.numericRestriction, newMin, newMax);
            }.bind(this);

            var deselectionCallback = function() {
                this.updateMinInput(this.absoluteMinValue);
                this.updateMaxInput(this.absoluteMaxValue);
                resetSelectedParametricValues(this.selectedParametricValues, this.fieldName, this.numericRestriction);
            }.bind(this);

            var mouseMoveCallback = function(x) {
                //noinspection JSPotentiallyInvalidUsageOfThis
                this.$('.numeric-parametric-co-ordinates').text(this.formatValue(Math.min(roundInputNumber(x), this.model.get('max'))));
            }.bind(this);

            var mouseLeaveCallback = function() {
                //noinspection JSPotentiallyInvalidUsageOfThis
                this.$('.numeric-parametric-co-ordinates').text('');
            }.bind(this);

            //noinspection JSUnresolvedFunction
            var buckets = calibrateBuckets(this.model.get('values'), this.model.get('min'), this.model.get('max'), this.model.get('bucketSize'));

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

            if (this.selectionEnabled) {
                this.drawSelection();
            }

            this.listenTo(this.selectedParametricValues, 'remove', function(model) {
                if (model.get('field') == this.fieldName) {
                    this.drawSelection();
                    this.updateModel(this.absoluteMinValue, this.absoluteMaxValue);
                }
            });
        },

        drawSelection: function() {
            this.graph.selectionRect.remove();

            var rangeModel = this.selectedParametricValues.find(rangeModelMatching(this.fieldName, this.numericRestriction));

            if (rangeModel) {
                var range = rangeModel.get('range');

                if (range) {
                    this.updateMinInput(roundInputNumber(range[0]));
                    this.updateMaxInput(roundInputNumber(range[1]));

                    this.graph.selectionRect.init(this.graph.chart, GRAPH_HEIGHT, this.graph.scale.barWidth(range[0]));
                    this.graph.selectionRect.update(this.graph.scale.barWidth(range[1]));
                    this.graph.selectionRect.focus();
                }
            }
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