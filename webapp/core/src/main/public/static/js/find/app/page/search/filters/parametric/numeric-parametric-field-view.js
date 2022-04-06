/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'moment',
    'find/app/vent',
    'find/app/model/find-base-collection',
    'find/app/model/numeric-field-details-model',
    'find/app/model/date-field-details-model',
    'find/app/page/search/filters/parametric/calibrate-buckets',
    'find/app/page/search/filters/parametric/numeric-range-rounder',
    'find/app/page/search/filters/parametric/numeric-widget',
    'find/app/model/bucketed-numeric-collection',
    'find/app/model/bucketed-date-collection',
    'find/app/util/date-picker',
    'find/app/util/search-data-util',
    'js-whatever/js/model-any-changed-attribute-listener',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view-numeric-input.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view-date-input.html',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle'
], function(_, $, Backbone, moment, vent, FindBaseCollection, NumericFieldDetailsModel,
            DateFieldDetailsModel, calibrateBuckets, rounder, numericWidget,
            BucketedNumericParametricCollection, BucketedDateParametricCollection, datePicker,
            searchDataUtil, addChangeListener, template, numericInputTemplate, dateInputTemplate,
            loadingTemplate, i18n) {
    'use strict';

    const ZOOM_IN_RATIO = 1.4;
    const ZOOM_OUT_RATIO = 1.96;
    const PAN_RATIO = 0.3;

    function sum(a, b) {
        return a + b;
    }

    // Update the inputs as the user drags a selection on the graph.
    // Note that this means the value in the input does not depend
    // on just the selected parametric range model.
    function updateCallback(x1, x2) {
        // rounding to one decimal place
        this.updateMinInput(x1);
        this.updateMaxInput(x2);
    }

    function selectionCallback(x1, x2) {
        const newMin = this.parseBoundarySelection(x1);
        const newMax = this.parseBoundarySelection(x2);
        this.updateRestrictions([newMin, newMax]);
    }

    function mouseMoveCallback(x) {
        this.$numericParametricCoords.text(this.formatValue(x));
    }

    function mouseLeaveCallback() {
        this.$numericParametricCoords.text('');
    }

    function zoomCallback(min, max) {
        this.model.set({
            currentMin: min,
            currentMax: max
        });
    }

    // This view must be visible before it is rendered
    const NumericParametricFieldView = Backbone.View.extend({
        className: 'animated fadeIn',
        loadingSpinnerHtml: _.template(loadingTemplate)({i18n: i18n, large: true}),
        svgTemplate: _.template('<svg class="chart <%- selectionEnabled ? \'chart-selection-enabled\' : \'\' %>"></svg>'),
        template: _.template(template),

        events: {
            'click .numeric-parametric-no-min': function() {
                this.updateRestrictions([this.getSelection().defaultRange.min, null]);
            },
            'click .numeric-parametric-no-max': function() {
                this.updateRestrictions([null, this.getSelection().defaultRange.max]);
            },
            'click .numeric-parametric-reset': function() {
                this.clearRestrictions();
                this.model.resetCurrent();
            },
            'click .numeric-parametric-pan-left': function () {
                const range = this.model.getNumericRange();
                const offset = PAN_RATIO * (range.max - range.min);
                this.model.set({ currentMin: range.min - offset, currentMax: range.max - offset });
            },
            'click .numeric-parametric-pan-right': function () {
                const range = this.model.getNumericRange();
                const offset = PAN_RATIO * (range.max - range.min);
                this.model.set({ currentMin: range.min + offset, currentMax: range.max + offset });
            },
            'click .numeric-parametric-zoom-out': function () {
                const range = this.model.getNumericRange();
                const offset = 0.5 * (ZOOM_OUT_RATIO - 1) * (range.max - range.min);
                this.model.set({ currentMin: range.min - offset, currentMax: range.max + offset });
            },
            'click .numeric-parametric-zoom-in': function () {
                const range = this.model.getNumericRange();
                const offset = 0.5 * (1 - 1 / ZOOM_IN_RATIO) * (range.max - range.min);
                this.model.set({ currentMin: range.min + offset, currentMax: range.max - offset });
            },

            'change .numeric-parametric-min-input': function() {
                this.updateRestrictions([this.readMinInput(), null]);
            },
            'change .numeric-parametric-max-input': function() {
                this.updateRestrictions([null, this.readMaxInput()]);
            },
            'dp.change .results-filter-date[data-date-attribute="min-date"]': function(event) {
                // The first time a user clicks the calendar a change event will be fired even though there is no change
                const minInput = event.date.valueOf();
                if(minInput !== this.minInput) {
                    this.updateRestrictions([minInput, null]);
                }
            },
            'dp.change .results-filter-date[data-date-attribute="max-date"]': function(event) {
                // The first time a user clicks the calendar a change event will be fired even though there is no change
                const maxInput = event.date.valueOf();
                if(maxInput !== this.maxInput) {
                    this.updateRestrictions([null, maxInput]);
                }
            },
            'click .clickable-widget': function() {
                this.clickCallback();
            }
        },

        initialize: function(options) {
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
                ? NumericParametricFieldView.dateFormatting
                : NumericParametricFieldView.defaultFormatting;
            this.formatValue = function(value) {
                const range = this.model.getRange();
                return formatting.format(value, range.min, range.max);
            }.bind(this);
            this.parseValue = formatting.parse;
            this.renderCustomFormatting = formatting.render;
            this.parseBoundarySelection = formatting.parseBoundarySelection;

            this.widget = numericWidget({formattingFn: this.formatValue});

            this.fieldDetailsModel = this.type === 'NumericDate' ?
                new DateFieldDetailsModel() : new NumericFieldDetailsModel();
            // The view's this.model contains the current and
            // absolute ranges, the bucketModel contains the values
            this.bucketModel = this.type === 'NumericDate'
                ? new BucketedDateParametricCollection.Model({id: this.fieldName})
                : new BucketedNumericParametricCollection.Model({id: this.fieldName});

            // Bind the selection rectangle to the selected parametric range
            this.listenTo(this.selectedParametricValues, 'add remove change', function(model) {
                if(this.isTargetModel(model)) {
                    this.updateSelection();
                }
            });

            this.throttledFetchBuckets = _.throttle(this.fetchBuckets, 500);

            addChangeListener(this, this.model, ['currentMin', 'currentMax'], function() {
                // Immediately update the graph for the new range; we calibrate
                // the buckets to remove buckets outside of the range
                this.updateGraph();

                // Fetch new buckets when the range changes
                this.throttledFetchBuckets();
            });

            addChangeListener(this, this.queryModel,
                ['queryText', 'fieldText', 'indexes', 'minDate', 'maxDate', 'minScore'],
                function() {
                    // Existing buckets are incorrect when the query changes
                    this.bucketModel.set({values: []});
                    this.fetchBuckets();
                });

            this.listenTo(vent, 'vent:resize', this.delayedFetchBucketsAndUpdate);

            this.listenTo(this.fieldDetailsModel, 'error', this.updateGraph);
            this.listenTo(this.bucketModel, 'change:values request sync error', this.updateGraph);
        },

        render: function() {
            this.$el
                .html(this.template({
                    i18n: i18n,
                    fieldName: this.hideTitle
                        ? undefined
                        : this.model.get('displayName'),
                    clickable: !!this.clickCallback,
                    buttonsEnabled: this.buttonsEnabled,
                    inputsRowClass: this.selectionEnabled || this.coordinatesEnabled
                        ? ''
                        : 'hide',
                    inputColumnClass: this.selectionEnabled
                        ? (this.coordinatesEnabled ? 'col-xs-4' : 'col-xs-6')
                        : 'hide',
                    inputTemplate: this.inputTemplate,
                    loadingSpinnerHtml: this.loadingSpinnerHtml,
                    coordinatesColumnClass: this.coordinatesEnabled
                        ? (this.selectionEnabled ? 'col-xs-4' : 'col-xs-12')
                        : 'hide'
                }));

            if(this.selectionEnabled) {
                this.renderCustomFormatting(this.$el);
                this.$minInput = this.$('.numeric-parametric-min-input');
                this.$maxInput = this.$('.numeric-parametric-max-input');
            }

            this.$numericParametricCoords = this.$('.numeric-parametric-co-ordinates');

            this.updateGraph();

            // Width may have changed, re-fetch the buckets
            this.fetchBuckets();
        },

        // Draw the graph with the current data and ranges, or display
        // the loading spinner if we don't have any data yet
        updateGraph: function() {
            const noError = !this.fieldDetailsModel.error && !this.bucketModel.error;
            const fetching = this.fieldDetailsModel.fetching || this.bucketModel.fetching;
            const hasValues = this.model.get('totalValues') !== 0;
            const modelBuckets = this.type === 'NumericDate'
                ? _.map(this.bucketModel.get('values'), function(bucket) {
                    return _.extend(bucket, {
                        min: moment(bucket.min),
                        max: moment(bucket.max)
                    });
                })
                : this.bucketModel.get('values');

            this.$('.numeric-parametric-error-text').toggleClass('hide', noError);
            this.$('.numeric-parametric-empty-text').toggleClass('hide', !noError || hasValues);

            const hideLoadingIndicator = !(noError && hasValues && fetching && modelBuckets.length === 0);
            this.$('.numeric-parametric-loading-indicator').toggleClass('hide', hideLoadingIndicator);

            const $chartRow = this.$('.numeric-parametric-chart-row');
            $chartRow.find('.chart').remove();
            const width = $chartRow.width();

            if(noError && hasValues && hideLoadingIndicator && width > 0) {
                const $chart = $(this.svgTemplate({selectionEnabled: this.selectionEnabled}));
                $chartRow.append($chart);

                const bucketRange =
                    this.model.getRangeForQuery(this.fieldDetailsModel.pick('min', 'max'));
                const buckets = calibrateBuckets(modelBuckets, [bucketRange.min, bucketRange.max]);

                this.graph = this.widget.drawGraph({
                    chart: $chart.get(0),
                    data: buckets,
                    updateCallback: updateCallback.bind(this),
                    selectionCallback: selectionCallback.bind(this),
                    deselectionCallback: this.clearRestrictions.bind(this),
                    mouseMoveCallback: mouseMoveCallback.bind(this),
                    mouseLeaveCallback: mouseLeaveCallback.bind(this),
                    zoomCallback: zoomCallback.bind(this),
                    xRange: width,
                    yRange: $chart.height(),
                    tooltip: i18n['search.numericParametricFields.tooltip'],
                    dragEnabled: this.selectionEnabled,
                    zoomEnabled: this.zoomEnabled,
                    coordinatesEnabled: this.coordinatesEnabled
                });

                this.updateSelection();
            }

            this.listenTo(this.selectedParametricValues, 'remove', function(model) {
                if(model.get('field') == this.fieldName) {
                    this.updateSelection();
                }
            });
        },

        // Update the rendered selection rectangle and
        // inputs to match the selected parametric range model
        updateSelection: function() {
            if(this.graph) {
                const selection = this.getSelection();
                this.updateMinInput(selection.range.min);
                this.updateMaxInput(selection.range.max);
                if (selection.selectionModel) {
                    this.graph.setSelection([selection.range.min, selection.range.max]);
                } else {
                    this.graph.clearSelection();
                }
            }
        },

        // Apply a new range selection; a null boundary will not be updated
        // Should be called with values that are already parsed
        updateRestrictions: function(newRange) {
            const selection = this.getSelection();
            const existingRange = [selection.range.min, selection.range.max];

            const newAttributes = {
                field: this.fieldName,
                displayName: this.displayName,
                type: this.type,
                range: _.map(newRange, function(value, index) {
                    // Explicitly check null since 0 is falsy
                    return value === null
                        ? existingRange[index]
                        : value;
                })
            };

            // Prevent user from manually setting min > max or max < min
            if(newAttributes.range[0] > newAttributes.range[1]) {
                if(existingRange.reduce(sum) - newAttributes.range.reduce(sum) > 0) { // if max was decreased
                    newAttributes.range[0] = newAttributes.range[1]; //set min to equal max
                } else { // if min was increased
                    newAttributes.range[1] = newAttributes.range[0]; //set max to equal min
                }
            }

            if(selection.selectionModel) {
                selection.selectionModel.set(newAttributes);
            } else {
                this.selectedParametricValues.add(newAttributes);
            }
        },

        clearRestrictions: function() {
            this.selectedParametricValues.remove(
                this.selectedParametricValues
                    .filter(this.isTargetModel.bind(this))
            );
        },

        fetchBuckets: function() {
            if(!(this.collapseModel && this.collapseModel.get('collapsed'))) {
                const width = this.$('.numeric-parametric-chart-row').width();
                // If the SVG has no width or there are no values, there is no point fetching new data
                if(!(width === 0 || this.model.get('totalValues') === 0)) {
                    const filteredQueryParams = this.filteredQueryParams();
                    const detailsParams = _.defaults({
                        fieldName: this.fieldName
                    }, filteredQueryParams);

                    // first retrieve min + max values taking into account other query restrictions
                    this.fieldDetailsModel.fetch({ data: detailsParams }).done(_.bind(function () {
                        // then split values into buckets within that range
                        const bucketRange =
                            this.model.getRangeForQuery(this.fieldDetailsModel.pick('min', 'max'));
                        this.bucketModel.fetch({
                            data: _.defaults({
                                targetNumberOfBuckets: Math.floor(width / this.pixelsPerBucket),
                                bucketMin: this.type === 'NumericDate'
                                    ? moment(bucketRange.min).utc().milliseconds(0).format()
                                    : bucketRange.min,
                                bucketMax: this.type === 'NumericDate'
                                    ? moment(bucketRange.max).utc().milliseconds(0).format()
                                    : bucketRange.max
                            }, filteredQueryParams)
                        });
                    }, this));
                }
            }
        },

        delayedFetchBucketsAndUpdate: function() {
            this.updateGraph();
            this.delayedFetchBuckets();
        },

        readMinInput: function() {
            return this.parseValue(this.$minInput.val());
        },

        readMaxInput: function() {
            return this.parseValue(this.$maxInput.val());
        },

        updateMinInput: function(newValue) {
            if(this.selectionEnabled) {
                this.minInput = newValue;
                this.$minInput.val(this.formatValue(newValue));
            }
        },

        updateMaxInput: function(newValue) {
            if(this.selectionEnabled) {
                this.maxInput = newValue;
                this.$maxInput.val(this.formatValue(newValue));
            }
        },

        isTargetModel: function(model) {
            return model.get('field') === this.fieldName &&
                model.get('range') &&
                model.get('type') === this.type;
        },

        /**
         * Get standard parameters used to restrict query's results to the current search.
         */
        filteredQueryParams: function () {
            // Exclude any restrictions for this field from the field text
            const fieldText = searchDataUtil.buildMergedFieldText(
                this.selectedParametricValues.reject(this.isTargetModel.bind(this)),
                this.queryModel.queryState.geographyModel,
                this.queryModel.queryState.documentSelectionModel);
            return {
                databases: this.queryModel.get('indexes'),
                queryText: this.queryModel.get('queryText'),
                fieldText: fieldText,
                minDate: this.queryModel.getIsoDate('minDate'),
                maxDate: this.queryModel.getIsoDate('maxDate'),
                minScore: this.queryModel.get('minScore')
            };
        },

        /**
         * Get information about the selected range of values.
         *
         * @returns object with properties:
         * @property selectionModel The relevant model in the selectedParametricValues collection,
         *                          or null if there's no selection
         * @property defaultRange The full range of values ignoring the selection, as a { min, max }
         *                        object with number values
         * @property range The selected range as a { min, max } object with number values
         */
        getSelection: function () {
            const selectionModel = this.selectedParametricValues
                .find(this.isTargetModel.bind(this));
            const defaultsModel = this.fieldDetailsModel.has('min') ?
                this.fieldDetailsModel : this.model;
            const defaultRange = {
                min: this.parseValue(defaultsModel.get('min')),
                max: this.parseValue(defaultsModel.get('max'))
            };

            let range;
            if (selectionModel) {
                const selectionRange = selectionModel.get('range');
                range = { min: selectionRange[0], max: selectionRange[1] };
            } else {
                range = defaultRange;
            }

            return {
                selectionModel: selectionModel || null,
                defaultRange: defaultRange,
                range: range
            };
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
            format: function(unformattedString) {
                return moment(unformattedString).format(datePicker.DATE_WIDGET_FORMAT);
            },
            parse: function(formattedString) {
                return moment(formattedString, datePicker.DATE_WIDGET_FORMAT).valueOf();
            },
            parseBoundarySelection: function(input) {
                return input;
            },
            render: function($el) {
                datePicker.render($el.find('.results-filter-date[data-date-attribute="min-date"]'),
                    function() {
                        this.updateRestrictions([this.readMinInput(), null]);
                    }.bind(this));
                datePicker.render($el.find('.results-filter-date[data-date-attribute="max-date"]'),
                    function() {
                        this.updateRestrictions([null, this.readMaxInput()]);
                    }.bind(this));
            }
        }
    });

    return NumericParametricFieldView;
});
