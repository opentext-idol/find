/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
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
    'find/app/page/search/filters/parametric/numeric-widget',
    'find/app/model/bucketed-parametric-collection',
    'parametric-refinement/prettify-field-name',
    'parametric-refinement/to-field-text-node',
    'parametric-refinement/selected-values-collection',
    'find/app/util/model-any-changed-attribute-listener',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view-numeric-input.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view-date-input.html',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle'
], function(Backbone, $, _, moment, vent, FindBaseCollection, calibrateBuckets, numericWidget, BucketedParametricCollection,
            prettifyFieldName, toFieldTextNode, SelectedParametricValuesCollection, addChangeListener, template,
            numericInputTemplate, dateInputTemplate, loadingTemplate, i18n) {

    'use strict';

    var DATE_WIDGET_FORMAT = 'YYYY-MM-DD HH:mm';

    function rangeModelMatching(fieldName, dataType) {
        return function(model) {
            return model.get('field') === fieldName && model.get('range') && model.get('dataType') === dataType;
        };
    }

    function roundInputNumber(input) {
        return Math.round(input * 10) / 10;
    }

    // This view must be visible before it is rendered
    var NumericParametricFieldView = Backbone.View.extend({
        className: 'animated fadeIn',
        loadingSpinnerHtml: _.template(loadingTemplate)({i18n: i18n, large: true}),
        svgTemplate: _.template('<svg class="chart <%- selectionEnabled ? \'chart-selection-enabled\' : \'\' %>"></svg>'),
        template: _.template(template),

        events: {
            'click .numeric-parametric-no-min': function() {
                this.updateRestrictions([this.model.get('min'), null]);
            },
            'click .numeric-parametric-no-max': function() {
                this.updateRestrictions([null, this.model.get('max')]);
            },
            'click .numeric-parametric-reset': function() {
                this.clearRestrictions();
                this.model.set(this.model.getDefaultCurrentRange());
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

            this.fieldName = this.model.id;

            var formatting = options.formatting || NumericParametricFieldView.defaultFormatting;
            this.formatValue = formatting.format;
            this.parseValue = formatting.parse;
            this.renderCustomFormatting = formatting.render;
            this.parseBoundarySelection = formatting.parseBoundarySelection;

            this.widget = numericWidget({formattingFn: this.formatValue});

            // The view's this.model contains the current and absolute ranges, the bucketModel contains the values
            this.bucketModel = new BucketedParametricCollection.Model({id: this.model.get('id')});

            // Bind the selection rectangle to the selected parametric range
            this.listenTo(this.selectedParametricValues, 'add remove change', function(model) {
                if (rangeModelMatching(this.fieldName, this.dataType)(model)) {
                    this.updateSelection();
                }
            });

            addChangeListener(this, this.model, ['currentMin', 'currentMax'], function() {
                // Immediately update the graph for the new range; we calibrate the buckets to remove buckets outside of the range
                this.updateGraph();

                // Fetch new buckets when the range changes
                this.fetchBuckets();
            });

            addChangeListener(this, this.queryModel, ['queryText', 'fieldText', 'indexes', 'minDate', 'maxDate', 'minScore'], function() {
                // Existing buckets are incorrect when the query changes
                this.bucketModel.set({values: []});

                this.fetchBuckets();
            });

            // TODO: Only update graph rather than render?
            this.listenTo(vent, 'vent:resize', this.render);

            this.listenTo(this.bucketModel, 'change:values request sync error', this.updateGraph);
        },

        render: function() {
            var inputColumnClass = this.selectionEnabled ? (this.coordinatesEnabled ? 'col-xs-4' : 'col-xs-6') : 'hide';
            var coordinatesColumnClass = this.coordinatesEnabled ? (this.selectionEnabled ? 'col-xs-4' : 'col-xs-12') : 'hide';

            this.$el
                .empty()
                .append(this.template({
                    i18n: i18n,
                    fieldName: this.hideTitle ? undefined : prettifyFieldName(this.model.get('name')),
                    clickable: Boolean(this.clickCallback),
                    buttonsEnabled: this.buttonsEnabled,
                    inputsRowClass: this.selectionEnabled || this.coordinatesEnabled ? '' : 'hide',
                    inputColumnClass: inputColumnClass,
                    inputTemplate: this.inputTemplate,
                    loadingSpinnerHtml: this.loadingSpinnerHtml,
                    coordinatesColumnClass: coordinatesColumnClass
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

        // Draw the graph with the current data and ranges, or display the loading spinner if we don't have any data yet
        updateGraph: function() {
            var hadError = this.bucketModel.error;
            var fetching = this.bucketModel.fetching;
            var noValues = this.model.get('totalValues') === 0;
            var modelBuckets = this.bucketModel.get('values');

            this.$('.numeric-parametric-error-text').toggleClass('hide', !hadError);
            this.$('.numeric-parametric-empty-text').toggleClass('hide', hadError || !noValues);

            var showLoadingIndicator = !hadError && !noValues && (fetching && modelBuckets.length === 0);
            this.$('.numeric-parametric-loading-indicator').toggleClass('hide', !showLoadingIndicator);

            var $chartRow = this.$('.numeric-parametric-chart-row');
            $chartRow.find('.chart').remove();
            var width = $chartRow.width();

            if (!hadError && !noValues && !showLoadingIndicator && width > 0) {
                var $chart = $(this.svgTemplate({selectionEnabled: this.selectionEnabled}));
                $chartRow.append($chart);

                var buckets = calibrateBuckets(modelBuckets, [this.model.get('currentMin'), this.model.get('currentMax')]);

                // Update the inputs as the user drags a selection on the graph. Note that this means the value in the input
                // does not depend on just the selected parametric range model.
                var updateCallback = function(x1, x2) {
                    // rounding to one decimal place
                    this.updateMinInput(roundInputNumber(x1));
                    this.updateMaxInput(roundInputNumber(x2));
                }.bind(this);

                var selectionCallback = function(x1, x2) {
                    var newMin = this.parseBoundarySelection(x1);
                    var newMax = this.parseBoundarySelection(x2);
                    this.updateRestrictions([newMin, newMax]);
                }.bind(this);

                var mouseMoveCallback = function(x) {
                    this.$('.numeric-parametric-co-ordinates').text(this.formatValue(roundInputNumber(x)));
                }.bind(this);

                var mouseLeaveCallback = function() {
                    this.$('.numeric-parametric-co-ordinates').text('');
                }.bind(this);

                var zoomCallback = function(min, max) {
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

            this.listenTo(this.selectedParametricValues, 'remove', function(model) {
                if (model.get('field') == this.fieldName) {
                    this.updateSelection();
                }
            });
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
                    this.updateMinInput(this.model.get('min'));
                    this.updateMaxInput(this.model.get('max'));
                    this.graph.clearSelection();
                }
            }
        },

        // Apply a new range selection; a null boundary will not be updated
        // Should be called with values that are already parsed
        updateRestrictions: function(newRange) {
            var existingModel = this.selectedParametricValues.find(rangeModelMatching(this.fieldName, this.dataType));
            var existingRange = existingModel ? existingModel.get('range') : [this.model.get('min'), this.model.get('max')];

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

        fetchBuckets: function() {
            var width = this.$('.numeric-parametric-chart-row').width();

            // If the SVG has no width or there are no values, there is no point fetching new data
            if (width !== 0 && this.model.get('totalValues') !== 0) {
                // Exclude any restrictions for this field from the field text
                var otherSelectedValues = this.selectedParametricValues
                    .reject(rangeModelMatching(this.fieldName, this.dataType))
                    .map(function(model) {
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