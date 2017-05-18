/*
 *  Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 *  Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'moment',
    'd3',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/vent',
    'find/app/util/range-input',
    'find/app/util/generate-error-support-message',
    'find/app/util/date-picker',
    'find/app/page/search/results/parametric-results-view',
    'find/app/page/search/results/field-selection-view',
    'find/app/page/search/filters/parametric/calibrate-buckets',
    'find/app/model/bucketed-date-collection',
    'find/app/model/parametric-collection',
    'find/app/page/search/results/trending/trending',
    'find/app/page/search/results/trending/trending-strategy',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/search/results/trending/trending-results-view.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view-date-input.html'
], function(_, $, moment, d3, Backbone, i18n, configuration, vent, RangeInput, generateErrorHtml,
            datePicker, ParametricResultsView, FieldSelectionView, calibrateBuckets,
            BucketedParametricCollection, ParametricCollection,
            Trending, trendingStrategy, loadingSpinnerHtml, template, dateInputTemplate) {
    'use strict';

    const DEBOUNCE_TIME = 500;
    const ERROR_MESSAGE_ARGUMENTS = {messageToUser: i18n['search.resultsView.trending.error.query']};

    const renderState = {
        RENDERING_NEW_DATA: 'RENDERING NEW DATA',
        ZOOMING: 'ZOOMING',
        DRAGGING: 'DRAGGING'
    };

    const dataState = {
        LOADING: 'LOADING',
        EMPTY: 'EMPTY',
        ERROR: 'ERROR',
        OK: 'OK'
    };

    const fetchState = {
        FETCHING_BUCKETS: 'FETCHING_BUCKETS',
        NOT_FETCHING: 'NOT_FETCHING'
    };

    function zoomCallback(min, max) {
        this.setMinMax(moment.unix(min), moment.unix(max));
        this.viewStateModel.set('currentState', renderState.ZOOMING);
        this.updateChart();
        this.debouncedFetchBucketedData();
    }

    function dragMoveCallback(min, max) {
        this.setMinMax(moment.unix(min), moment.unix(max));
        this.viewStateModel.set('currentState', renderState.DRAGGING);
        this.updateChart();
    }

    function dragEndCallback(min, max) {
        this.setMinMax(moment.unix(min), moment.unix(max));
        this.viewStateModel.set('currentState', renderState.DRAGGING);
        this.debouncedFetchBucketedData();
    }

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .trending-snap-to-now': function() {
                this.$snapToNow.blur();
                this.snapToNow();
            },
            'dp.change .results-filter-date[data-date-attribute="min-date"]': function(event) {
                this.inputMinValue(moment(event.date));
            },
            'dp.change .results-filter-date[data-date-attribute="max-date"]': function(event) {
                this.inputMaxValue(moment(event.date));
            },
        },
        dateInputTemplate: _.template(dateInputTemplate),

        initialize: function(options) {
            const config = configuration();
            this.dateField = config.trending.dateField;
            this.numberOfValuesToDisplay = config.trending.numberOfValues;
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.queryState.selectedParametricValues;
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.parametricCollection = options.parametricCollection;

            this.debouncedFetchBucketedData = _.debounce(this.fetchBucketedData, DEBOUNCE_TIME);
            this.bucketedValues = {};

            this.model = new Backbone.Model({
                value: config.trending.defaultNumberOfBuckets
            });

            this.minBuckets = config.trending.minNumberOfBuckets;
            this.maxBuckets = config.trending.maxNumberOfBuckets;

            this.viewStateModel = new Backbone.Model({
                currentState: renderState.RENDERING_NEW_DATA,
                searchStateChanged: false
            });

            this.slider = new RangeInput({
                leftLabel: i18n['search.resultsView.trending.bucketSlider.fewerBuckets'],
                max: this.maxBuckets,
                min: this.minBuckets,
                model: this.model,
                rightLabel: i18n['search.resultsView.trending.bucketSlider.moreBuckets'],
                step: 1
            });

            this.listenTo(this.queryModel, 'change', function() {
                if(this.$el.is(':visible')) {
                    this.fetchFieldAndRangeData();
                } else {
                    this.viewStateModel.set('searchStateChanged', true);
                }
            });

            this.listenTo(vent, 'vent:resize', this.update);
            this.listenTo(this.viewStateModel, 'change:dataState', this.onDataStateChange);

            this.listenTo(this.parametricFieldsCollection, 'error', function(collection, xhr) {
                this.onDataError(xhr);
            });

            this.listenTo(this.model, 'change:field', this.fetchFieldAndRangeData);
            this.listenTo(this.model, 'change:value', this.debouncedFetchBucketedData);
            this.listenTo(this.parametricCollection, 'sync', this.setFieldSelector);

            this.listenTo(this.parametricCollection, 'error', function(collection, xhr) {
                this.onDataError(xhr);
            });
            this.listenTo(this.model, 'change:currentMin', function() {
                this.updateDateInput(this.$minInput, 'currentMin');
            });
            this.listenTo(this.model, 'change:currentMax', function() {
                this.updateDateInput(this.$maxInput, 'currentMax');
            });
        },

        render: function() {
            if(this.$snapToNow) {
                this.$snapToNow.tooltip('destroy');
            }

            if(this.trendingChart) {
                this.trendingChart.remove();
            }

            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: _.template(loadingSpinnerHtml)
            }));

            this.$errorMessage = this.$('.trending-error');
            this.$snapToNow = this.$('.trending-snap-to-now');
            this.$chart = this.$('.trending-chart');
            this.$trendingSlider = this.$('.trending-slider');

            this.viewStateModel.set('dataState', dataState.LOADING);

            this.trendingChart = new Trending({
                el: this.$chart.get(0),
                tooltipText: i18n['search.resultsView.trending.tooltipText'],
                zoomEnabled: true,
                dragEnabled: true,
                hoverEnabled: true,
                yAxisLabelForUnit: i18n['search.resultsView.trending.yAxis'],
                yAxisUnitsText: function(yUnit) {
                    return i18n['search.resultsView.trending.unit.' + yUnit];
                }
            });

            this.$snapToNow.tooltip({
                placement: 'top',
                container: 'body',
                title: i18n['search.resultsView.trending.snapToNow']
            });

            this.slider.setElement(this.$trendingSlider).render();

            if(!this.parametricCollection.isEmpty()) {
                this.setFieldSelector();
            }
            this.setRangeSelector();
        },

        remove: function() {
            this.slider.remove();
            this.$('[data-toggle="tooltip"]').tooltip('destroy');

            if(this.$snapToNow) {
                this.$snapToNow.tooltip('destroy');
            }

            Backbone.View.prototype.remove.call(this);
        },

        update: function() {
            if(this.$el.is(':visible') && !this.parametricCollection.isEmpty()) {
                if(this.viewStateModel.get('searchStateChanged')) {
                    this.setFieldSelector();
                    this.fetchFieldAndRangeData();
                    this.viewStateModel.set('searchStateChanged', false);
                } else if(!_.isEmpty(this.bucketedValues)) {
                    this.updateChart();
                }
            }
        },

        setFieldSelector: function() {
            if(this.$el.is(':visible')) {
                if(this.fieldSelector) {
                    this.fieldSelector.remove();
                }

                const fields = this.parametricFieldsCollection
                    .where({type: 'Parametric'})
                    .map(function(m) {
                        const id = m.get('id');
                        const field = this.parametricCollection.where({id: id})[0];
                        const totalValues = field
                            ? field.get('totalValues')
                            : 0;
                        return {
                            id: id,
                            displayName: m.get('displayName') + ' (' + totalValues + ')'
                        }
                    }.bind(this));

                this.fieldSelector = new FieldSelectionView({
                    model: this.model,
                    name: 'parametric-fields',
                    fields: fields,
                    allowEmpty: false
                });

                this.$('.trending-field-selector').prepend(this.fieldSelector.$el);
                this.fieldSelector.render();
            }
        },

        setRangeSelector: function() {
            if(this.$el.is(':visible') && !this.$minInput) {
                this.$('.trending-range-selector').prepend(this.dateInputTemplate({minOrMax: 'max'}));
                this.$('.trending-range-selector').prepend(this.dateInputTemplate({minOrMax: 'min'}));

                this.$minInput = this.$('.numeric-parametric-min-input.form-control');
                this.$maxInput = this.$('.numeric-parametric-max-input.form-control');

                const dateInputs = [{
                    $el: this.$minInput,
                    inputFunction: this.inputMinValue.bind(this),
                    tooltipText: i18n['search.resultsView.trending.minDate']
                }, {
                    $el: this.$maxInput,
                    inputFunction: this.inputMaxValue.bind(this),
                    tooltipText: i18n['search.resultsView.trending.maxDate']
                }];

                dateInputs.forEach(function(options) {
                    options.$el.tooltip('destroy');

                    options.$el
                        .tooltip({
                            placement: 'top',
                            container: 'body'
                        })
                        .attr('data-original-title', options.tooltipText)
                        .tooltip('fixTitle');

                    datePicker.render(
                        options.$el.closest('.results-filter-date'),
                        function() {
                            if (this.validateDateFormat(options.$el.val())) {
                                options.inputFunction(this.parseDate(options.$el.val()));
                            } else {
                                this.indicateNonValidDateInput({
                                    $inputEl: options.$el,
                                    errorMessage: i18n['search.resultsView.trending.error.invalidDate'],
                                    originalTooltipMessage: options.tooltipText
                                });
                            }
                        }.bind(this));
                }.bind(this));
            }
        },

        fetchFieldAndRangeData: function() {
            this.viewStateModel.set('dataState', dataState.LOADING);

            if(this.bucketedDataReqest) {
                this.bucketedDataReqest.abort();
            }

            const fetchOptions = {
                queryModel: this.queryModel,
                selectedParametricValues: this.selectedParametricValues,
                field: this.model.get('field'),
                dateField: this.dateField,
                numberOfValuesToDisplay: this.numberOfValuesToDisplay
            };

            $.when(trendingStrategy.fetchField(fetchOptions))
                .then(function(values) {
                    this.selectedFieldValues = values;

                    if(this.selectedFieldValues.length === 0) {
                        this.viewStateModel.set('dataState', dataState.EMPTY);
                        return $.when();
                    } else {
                        return $.when(trendingStrategy.fetchRange(this.selectedFieldValues, fetchOptions))
                            .then(function(data) {
                                this.setMinMax(moment(data.min), moment(data.max));
                            }.bind(this));
                    }
                }.bind(this))
                .fail(function(xhr) {
                    this.onDataError(xhr);
                    this.viewStateModel.set('fetchState', fetchState.NOT_FETCHING);
                }.bind(this));
        },

        fetchBucketedData: function() {
            this.viewStateModel.set('fetchState', fetchState.FETCHING_BUCKETS);

            const minDate = this.model.get('currentMin'), maxDate = this.model.get('currentMax');

            if(minDate === maxDate) {
                this.setMinMax(minDate.clone().subtract(1, 'day'), maxDate.clone().add(1, 'day'));
            }

            if(this.bucketedDataReqest) {
                this.bucketedDataReqest.abort();
            }

            const fetchOptions = {
                queryModel: this.queryModel,
                selectedFieldValues: this.selectedFieldValues,
                selectedParametricValues: this.selectedParametricValues,
                field: this.model.get('field'),
                currentMax: this.model.get('currentMax'),
                currentMin: this.model.get('currentMin'),
                dateField: this.dateField,
                numberOfValuesToDisplay: this.numberOfValuesToDisplay,
                targetNumberOfBuckets: this.model.get('value')
            };

            this.bucketedDataReqest = trendingStrategy.fetchBucketedData(fetchOptions)
                .done(_.bind(function() {
                    this.viewStateModel.set({
                        currentState: renderState.RENDERING_NEW_DATA,
                        dataState: dataState.OK,
                        fetchState: fetchState.NOT_FETCHING
                    });
                    this.bucketedValues = Array.prototype.slice.call(arguments);
                    this.updateChart();
                }, this))
                .fail(_.bind(function(xhr) {
                    this.onDataError(xhr);
                    this.viewStateModel.set('fetchState', fetchState.NOT_FETCHING);
                }, this))
                .always(function() {
                    this.bucketedDataReqest = null;
                }.bind(this));

            return this.bucketedDataReqest;
        },

        updateChart: function() {
            if(this.viewStateModel.get('fetchState') !== fetchState.FETCHING_BUCKETS
                && !this.$chart.hasClass('hide')) {

                const chartData = trendingStrategy.createChartData({
                    bucketedValues: this.bucketedValues,
                    currentMin: this.model.get('currentMin'),
                    currentMax: this.model.get('currentMax')
                });

                const haveData = chartData.data.some(function(datum) {
                    return datum.points.length > 0;
                });

                if(haveData) {
                    this.$('[data-toggle="tooltip"]').tooltip('destroy');

                    const reloaded = this.viewStateModel.get('currentState') === renderState.RENDERING_NEW_DATA;

                    this.trendingChart.draw({
                        reloaded: reloaded,
                        chartData: chartData,
                        minDate: reloaded
                            ? chartData.data[0].points[0].mid
                            : this.model.get('currentMin').toDate(),
                        maxDate: reloaded
                            ? chartData.data[chartData.data.length - 1].points[chartData.data[0].points.length - 1].mid
                            : this.model.get('currentMax').toDate(),
                        zoomCallback: zoomCallback.bind(this),
                        dragMoveCallback: dragMoveCallback.bind(this),
                        dragEndCallback: dragEndCallback.bind(this)
                    });
                }
            }
        },

        setMinMax: function(min, max) {
            this.model.set({
                currentMin: min,
                currentMax: max
            });
        },

        snapToNow: function() {
            const currentMin = this.model.get('currentMin');
            const now = moment(Date.now());
            this.setMinMax(
                currentMin.isBefore(now)
                    ? currentMin
                    : now.clone().subtract((this.model.get('currentMax').clone().subtract(currentMin))),
                now
            );
            this.fetchBucketedData();
        },

        updateDateInput: function($el, dateAttribute) {
            $el.val(this.formatDate(this.model.get(dateAttribute)));
            this.updateChart();
            this.debouncedFetchBucketedData();
        },

        inputMinValue: function(min) {
            if (min.isBefore(this.model.get('currentMax'))) {
                this.model.set('currentMin', min);
            } else {
                this.indicateNonValidDateInput({
                    $inputEl: this.$minInput,
                    errorMessage: i18n['search.resultsView.trending.error.minBiggerThanMax'],
                    originalTooltipMessage: i18n['search.resultsView.trending.minDate']
                });
            }
        },

        inputMaxValue: function(max) {
            if(this.model.get('currentMin').isBefore(max)) {
                this.model.set('currentMax', max);
            } else {
                this.indicateNonValidDateInput({
                    $inputEl: this.$maxInput,
                    errorMessage: i18n['search.resultsView.trending.error.maxSmallerThanMin'],
                    originalTooltipMessage: i18n['search.resultsView.trending.maxDate']
                });
            }
        },

        formatDate: function(date) {
            return date.format(datePicker.DATE_WIDGET_FORMAT);
        },

        parseDate: function(dateString) {
            return moment(dateString, datePicker.DATE_WIDGET_FORMAT);
        },

        validateDateFormat: function(dateString) {
            return moment(dateString, datePicker.DATE_WIDGET_FORMAT, true).isValid();
        },

        indicateNonValidDateInput: function(options) {
            options.$inputEl.css('border-color', 'red');
            options.$inputEl.attr('data-original-title', options.errorMessage)
                .tooltip('fixTitle')
                .tooltip('show');
            setTimeout(function() {
                options.$inputEl.css('border-color', '');
                options.$inputEl.attr('data-original-title', options.originalTooltipMessage)
                    .tooltip('fixTitle')
                    .tooltip('hide');
            }, 2000);
        },

        onDataStateChange: function() {
            const state = this.viewStateModel.get('dataState');

            this.$errorMessage.toggleClass('hide', state !== dataState.ERROR);
            this.$('.trending-empty').toggleClass('hide', state !== dataState.EMPTY);
            this.$('.trending-loading').toggleClass('hide', state !== dataState.LOADING);
            this.$chart.toggleClass('hide', state !== dataState.OK);
            this.$snapToNow.toggleClass('hide', state !== dataState.OK);
            this.$trendingSlider.toggleClass('hide', state !== dataState.OK);
            this.$('.trending-range-selector').toggleClass('hide', state !== dataState.OK);

            if(state !== dataState.ERROR && this.$errorMessage) {
                this.$errorMessage.empty();
            }
        },

        onDataError: function(xhr) {
            if(xhr.status !== 0) {
                this.viewStateModel.set('dataState', dataState.ERROR);
                const messageArguments = _.extend({
                    errorDetails: xhr.responseJSON.message,
                    errorUUID: xhr.responseJSON.uuid
                }, ERROR_MESSAGE_ARGUMENTS);
                this.$errorMessage.html(generateErrorHtml(messageArguments));
            }
        }
    });
});
