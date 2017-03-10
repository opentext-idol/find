/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'moment',
    'find/app/vent',
    'find/app/model/find-base-collection',
    'find/app/page/search/filters/parametric/calibrate-buckets',
    'find/app/page/search/filters/parametric/numeric-range-rounder',
    'find/app/page/search/filters/parametric/numeric-widget',
    'find/app/model/bucketed-parametric-collection',
    'parametric-refinement/to-field-text-node',
    'find/app/util/date-picker',
    'js-whatever/js/model-any-changed-attribute-listener',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view-numeric-input.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view-date-input.html',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle'
], function(Backbone, $, _, moment, vent, FindBaseCollection, calibrateBuckets, rounder, numericWidget,
            BucketedParametricCollection, toFieldTextNode, datePicker, addChangeListener,
            template, numericInputTemplate, dateInputTemplate, loadingTemplate, i18n) {
    'use strict';

    function sum(a, b) {
        return a + b;
    }

    // This view must be visible before it is rendered
    const NumericParametricFieldView = Backbone.View.extend({
        className: 'animated fadeIn',
        loadingSpinnerHtml: _.template(loadingTemplate)({i18n: i18n, large: true}),
        svgTemplate: _.template('<svg class="chart <%- selectionEnabled ? \'chart-selection-enabled\' : \'\' %>"></svg>'),
        template: _.template(template),

        events: {
            'click .numeric-parametric-no-min': function () {
                this.updateRestrictions([this.model.get('min'), null]);
            },
            'click .numeric-parametric-no-max': function () {
                this.updateRestrictions([null, this.model.get('max')]);
            },
            'click .numeric-parametric-reset': function () {
                this.clearRestrictions();
                this.model.set(this.model.getDefaultCurrentRange());
            },
            'change .numeric-parametric-min-input': function () {
                this.updateRestrictions([this.readMinInput(), null]);
            },
            'change .numeric-parametric-max-input': function () {
                this.updateRestrictions([null, this.readMaxInput()]);
            },
            'dp.change .results-filter-date[data-date-attribute="min-date"]': function (event) {
                this.updateRestrictions([event.date.unix(), null]);
            },
            'dp.change .results-filter-date[data-date-attribute="max-date"]': function (event) {
                this.updateRestrictions([null, event.date.unix()]);
            },
            'click .clickable-widget': function () {
                this.clickCallback();
            }
        },

        initialize: function (options) {
            this.delayedFetchBuckets = _.debounce(this.fetchBuckets, 500);
            this.collapseModel = options.collapseModel || null;

            this.inputTemplate = options.inputTemplate || NumericParametricFieldView.numericInputTemplate;
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.pixelsPerBucket = options.pixelsPerBucket;
            this.selectionEnabled = options.selectionEnabled;
            this.zoomEnabled = options.zoomEnabled;
            this.buttonsEnabled = options.selectionEnabled && options.buttonsEnabled;
            this.coordinatesEnabled = _.isUndefined(options.coordinatesEnabled) || options.coordinatesEnabled;
            this.hideTitle = options.hideTitle;
            this.clickCallback = options.clickCallback;

            this.fieldName = this.model.id;
            this.displayName = this.model.get('displayName');
            this.type = this.model.get('type');

            const formatting = this.type === 'NumericDate'
                ? options.formatting
                : NumericParametricFieldView.defaultFormatting;
            this.formatValue = function (value) {
                return formatting.format(value, this.model.get('currentMin'), this.model.get('currentMax'));
            }.bind(this);
            this.parseValue = formatting.parse;
            this.renderCustomFormatting = formatting.render;
            this.parseBoundarySelection = formatting.parseBoundarySelection;

            this.widget = numericWidget({formattingFn: this.formatValue});

            // The view's this.model contains the current and
            // absolute ranges, the bucketModel contains the values
            this.bucketModel = new BucketedParametricCollection.Model({id: this.fieldName});

            // Bind the selection rectangle to the selected parametric range
            this.listenTo(this.selectedParametricValues, 'add remove change', function (model) {
                if (this.isTargetModel(model)) {
                    this.updateSelection();
                }
            });

            addChangeListener(this, this.model, ['currentMin', 'currentMax'], function () {
                // Immediately update the graph for the new range; we calibrate
                // the buckets to remove buckets outside of the range
                this.updateGraph();

                // Fetch new buckets when the range changes
                this.fetchBuckets();
            });

            addChangeListener(this, this.queryModel,
                ['queryText', 'fieldText', 'indexes', 'minDate', 'maxDate', 'minScore'],
                function () {
                    // Existing buckets are incorrect when the query changes
                    this.bucketModel.set({values: []});
                    this.fetchBuckets();
                });

            this.listenTo(vent, 'vent:resize', this.delayedFetchBucketsAndUpdate);

            this.listenTo(this.bucketModel, 'change:values request sync error', this.updateGraph);
        },

        render: function () {
            this.$el
                .empty()
                .append(this.template({
                    i18n: i18n,
                    fieldName: this.hideTitle ? undefined : this.model.get('displayName'),
                    clickable: Boolean(this.clickCallback),
                    buttonsEnabled: this.buttonsEnabled,
                    inputsRowClass: this.selectionEnabled || this.coordinatesEnabled ? '' : 'hide',
                    inputColumnClass: this.selectionEnabled
                        ? (this.coordinatesEnabled ? 'col-xs-4' : 'col-xs-6')
                        : 'hide',
                    inputTemplate: this.inputTemplate,
                    loadingSpinnerHtml: this.loadingSpinnerHtml,
                    coordinatesColumnClass: this.coordinatesEnabled
                        ? (this.selectionEnabled ? 'col-xs-4' : 'col-xs-12')
                        : 'hide'
                }));

            if (this.selectionEnabled) {
                this.renderCustomFormatting(this.$el);
                this.$minInput = this.$('.numeric-parametric-min-input');
                this.$maxInput = this.$('.numeric-parametric-max-input');
            }

            this.updateGraph();

            // Width may have changed, re-fetch the buckets
            this.fetchBuckets();
        },

        // Draw the graph with the current data and ranges, or display
        // the loading spinner if we don't have any data yet
        updateGraph: function () {
            const noError = !this.bucketModel.error;
            const fetching = this.bucketModel.fetching;
            const hasValues = this.model.get('totalValues') !== 0;
            const modelBuckets = this.bucketModel.get('values');

            this.$('.numeric-parametric-error-text').toggleClass('hide', noError);
            this.$('.numeric-parametric-empty-text').toggleClass('hide', !noError || hasValues);

            const hideLoadingIndicator = !(noError && hasValues && fetching && modelBuckets.length === 0);
            this.$('.numeric-parametric-loading-indicator').toggleClass('hide', hideLoadingIndicator);

            const $chartRow = this.$('.numeric-parametric-chart-row');
            $chartRow.find('.chart').remove();
            const width = $chartRow.width();

            if (noError && hasValues && hideLoadingIndicator && width > 0) {
                const $chart = $(this.svgTemplate({selectionEnabled: this.selectionEnabled}));
                $chartRow.append($chart);

                const buckets = calibrateBuckets(
                    modelBuckets,
                    [this.model.get('currentMin'), this.model.get('currentMax')]
                );

                // Update the inputs as the user drags a selection on the graph.
                // Note that this means the value in the input does not depend
                // on just the selected parametric range model.
                const updateCallback = function (x1, x2) {
                    // rounding to one decimal place
                    this.updateMinInput(x1);
                    this.updateMaxInput(x2);
                }.bind(this);

                const selectionCallback = function (x1, x2) {
                    const newMin = this.parseBoundarySelection(x1);
                    const newMax = this.parseBoundarySelection(x2);
                    this.updateRestrictions([newMin, newMax]);
                }.bind(this);

                const mouseMoveCallback = function (x) {
                    this.$('.numeric-parametric-co-ordinates').text(this.formatValue(x));
                }.bind(this);

                const mouseLeaveCallback = function () {
                    this.$('.numeric-parametric-co-ordinates').text('');
                }.bind(this);

                const zoomCallback = function (min, max) {
                    this.model.set({
                        currentMin: min,
                        currentMax: max
                    });
                }.bind(this);

                this.graph = this.widget.drawGraph({
                    chart: $chart.get(0),
                    data: buckets,
                    updateCallback: updateCallback,
                    selectionCallback: selectionCallback,
                    deselectionCallback: this.clearRestrictions.bind(this),
                    mouseMoveCallback: mouseMoveCallback,
                    mouseLeaveCallback: mouseLeaveCallback,
                    zoomCallback: zoomCallback,
                    xRange: width,
                    yRange: $chart.height(),
                    tooltip: i18n['search.numericParametricFields.tooltip'],
                    dragEnabled: this.selectionEnabled,
                    zoomEnabled: this.zoomEnabled,
                    coordinatesEnabled: this.coordinatesEnabled
                });

                this.updateSelection();
            }

            this.listenTo(this.selectedParametricValues, 'remove', function (model) {
                if (model.get('field') == this.fieldName) {
                    this.updateSelection();
                }
            });
        },

        // Update the rendered selection rectangle and
        // inputs to match the selected parametric range model
        updateSelection: function () {
            if (this.graph) {
                const rangeModel = this.selectedParametricValues
                    .find(this.isTargetModel.bind(this));

                if (rangeModel) {
                    const range = rangeModel.get('range');

                    this.updateMinInput(range[0]);
                    this.updateMaxInput(range[1]);
                    this.graph.setSelection(range);
                } else {
                    this.updateMinInput(this.model.get('min'));
                    this.updateMaxInput(this.model.get('max'));
                    this.graph.clearSelection();
                }
            }
        },

        // Apply a new range selection; a null boundary will not be updated
        // Should be called with values that are already parsed
        updateRestrictions: function (newRange) {
            const existingModel = this.selectedParametricValues
                .find(this.isTargetModel.bind(this));
            const existingRange = existingModel
                ? existingModel.get('range')
                : [this.model.get('min'), this.model.get('max')];

            const newAttributes = {
                field: this.fieldName,
                displayName: this.displayName,
                type: this.type,
                range: _.map(newRange, function (value, index) {
                    // Explicitly check null since 0 is falsy
                    return value === null ? existingRange[index] : value;
                })
            };

            // Fixes error where user could manually input min > max or max < min
            if (newAttributes.range[0] > newAttributes.range[1]) {
                if (existingRange.reduce(sum) - newAttributes.range.reduce(sum) > 0) { // if max was decreased
                    newAttributes.range[0] = newAttributes.range[1]; //set min to equal max
                } else { // if min was increased
                    newAttributes.range[1] = newAttributes.range[0]; //set max to equal min
                }
            }

            if (existingModel) {
                existingModel.set(newAttributes);
            } else {
                this.selectedParametricValues.add(newAttributes);
            }
        },

        clearRestrictions: function () {
            this.selectedParametricValues.remove(
                this.selectedParametricValues
                    .filter(this.isTargetModel.bind(this))
            );
        },

        fetchBuckets: function () {
            if (!(this.collapseModel && this.collapseModel.get('collapsed'))) {
                const width = this.$('.numeric-parametric-chart-row').width();

                // If the SVG has no width or there are no values, there is no point fetching new data
                if (!(width === 0 || this.model.get('totalValues') === 0)) {
                    // Exclude any restrictions for this field from the field text
                    const otherSelectedValues = this.selectedParametricValues
                        .reject(this.isTargetModel.bind(this))
                        .map(function (model) {
                            return model.toJSON();
                        });

                    this.bucketModel.fetch({
                        data: {
                            queryText: this.queryModel.get('queryText'),
                            fieldText: toFieldTextNode(otherSelectedValues),
                            minDate: this.queryModel.getIsoDate('minDate'),
                            maxDate: this.queryModel.getIsoDate('maxDate'),
                            minScore: this.queryModel.get('minScore'),
                            databases: this.queryModel.get('indexes'),
                            targetNumberOfBuckets: Math.floor(width / this.pixelsPerBucket),
                            bucketMin: this.model.get('currentMin'),
                            bucketMax: this.model.get('currentMax')
                        }
                    });
                }
            }
        },

        delayedFetchBucketsAndUpdate: function () {
            this.updateGraph();
            this.delayedFetchBuckets();
        },

        readMinInput: function () {
            return this.parseValue(this.$minInput.val());
        },

        readMaxInput: function () {
            return this.parseValue(this.$maxInput.val());
        },

        updateMinInput: function (newValue) {
            if (this.selectionEnabled) {
                this.$minInput.val(this.formatValue(newValue));
            }
        },

        updateMaxInput: function (newValue) {
            if (this.selectionEnabled) {
                this.$maxInput.val(this.formatValue(newValue));
            }
        },

        isTargetModel: function (model) {
            return model.get('field') === this.fieldName &&
                model.get('range') &&
                model.get('type') === this.type;
        }
    }, {
        dateInputTemplate: _.template(dateInputTemplate),
        numericInputTemplate: _.template(numericInputTemplate),
        defaultFormatting: {
            format: rounder().round,
            parse: Number,
            parseBoundarySelection: _.identity,
            render: _.noop
        },
        dateFormatting: {
            format: function (unformattedString) {
                return moment(Math.round(unformattedString * 1000)).format(datePicker.DATE_WIDGET_FORMAT);
            },
            parse: function (formattedString) {
                return moment(formattedString, datePicker.DATE_WIDGET_FORMAT).unix();
            },
            parseBoundarySelection: function (input) {
                // Epoch seconds are not decimal
                return Math.round(input);
            },
            render: function ($el) {
                datePicker.render($el.find('.results-filter-date[data-date-attribute="min-date"]'),
                    function () {
                        this.updateRestrictions([this.readMinInput(), null]);
                    }.bind(this));
                datePicker.render($el.find('.results-filter-date[data-date-attribute="max-date"]'),
                    function () {
                        this.updateRestrictions([null, this.readMaxInput()]);
                    }.bind(this));
            }
        }
    });

    return NumericParametricFieldView;
});
