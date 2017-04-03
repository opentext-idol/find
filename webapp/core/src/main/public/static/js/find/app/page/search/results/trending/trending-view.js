/*
 *  Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 *  Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'd3',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/vent',
    'find/app/util/generate-error-support-message',
    'find/app/page/search/results/parametric-results-view',
    'find/app/page/search/results/field-selection-view',
    'find/app/page/search/filters/parametric/calibrate-buckets',
    'find/app/model/bucketed-parametric-collection',
    'find/app/model/parametric-field-details-model',
    'find/app/model/parametric-collection',
    'find/app/page/search/results/trending/trending',
    'parametric-refinement/to-field-text-node',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/search/results/trending/trending-results-view.html'

], function (_, $, d3, Backbone, i18n, configuration, vent, generateErrorHtml, ParametricResultsView, FieldSelectionView,
             calibrateBuckets, BucketedParametricCollection, ParametricDetailsModel, ParametricCollection, Trending,
             toFieldTextNode, loadingSpinnerHtml, template) {
    'use strict';

    const MILLISECONDS_TO_SECONDS = 1000;
    const DEBOUNCE_TIME = 500;
    const ERROR_MESSAGE_ARGUMENTS = {messageToUser: i18n['search.resultsView.trending.error.query']};
    const SECONDS_IN_ONE_DAY = 86400;

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

    return Backbone.View.extend({
        template: _.template(template),
        loadingHtml: _.template(loadingSpinnerHtml),

        initialize: function(options) {
            let config = configuration();
            this.dateField = config.trending.dateField;
            //noinspection JSUnresolvedVariable
            this.targetNumberOfBuckets = config.trending.numberOfBuckets;
            //noinspection JSUnresolvedVariable
            this.numberOfValuesToDisplay = config.trending.numberOfValues;
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.queryState.selectedParametricValues;
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.parametricCollection = options.parametricCollection;

            this.debouncedFetchBucketingData = _.debounce(this.fetchBucketingData, DEBOUNCE_TIME);
            this.bucketedValues = {};
            this.trendingFieldsCollection = new ParametricCollection([], {url: 'api/public/parametric/values'});

            this.model = new Backbone.Model();
            this.viewStateModel = new Backbone.Model({
                currentState: renderState.RENDERING_NEW_DATA,
                searchStateChanged: false
            });

            this.listenTo(this.queryModel, 'change', function() {
                if(this.$el.is(':visible')) {
                    this.fetchFieldData();
                } else {
                    this.viewStateModel.set('searchStateChanged', true);
                }
            });
            this.listenTo(vent, 'vent:resize', this.update);
            this.listenTo(this.viewStateModel, 'change:dataState', this.onDataStateChange);
            this.listenTo(this.parametricFieldsCollection, 'error', function(collection, xhr) {
                this.onDataError(xhr);
            });
            this.listenTo(this.model, 'change:field', this.fetchFieldData);
            this.listenTo(this.parametricCollection, 'sync', this.setFieldSelector);
            this.listenTo(this.parametricCollection, 'error', function(collection, xhr) {
                this.onDataError(xhr);
            });
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: this.loadingHtml
            }));
            this.$errorMessage = this.$('.trending-error');
            this.viewStateModel.set('dataState', dataState.LOADING);

            if(this.trendingChart) {
                this.trendingChart.remove();
            }
            this.trendingChart = new Trending({
                el: this.$('.trending-chart').get(0),
                tooltipText: i18n['search.resultsView.trending.tooltipText']
            });

            if(!this.parametricCollection.isEmpty()) {
                this.setFieldSelector();
            }
        },

        remove: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');
            Backbone.View.prototype.remove.call(this);
        },

        update: function() {
            if(this.$el.is(':visible')) {
                if(this.viewStateModel.get('searchStateChanged')) {
                    this.setFieldSelector();
                    this.fetchFieldData();
                    this.viewStateModel.set('searchStateChanged', false);
                } else {
                    if(!_.isEmpty(this.bucketedValues)) {
                        this.updateChart();
                    }
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
                    .map(function (m) {
                        const id = m.get('id');
                        const totalValues = this.parametricCollection.where({id: id})[0] ?
                            this.parametricCollection.where({id: id})[0].get('totalValues')
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

        fetchFieldData: function() {
            this.viewStateModel.set('dataState', dataState.LOADING);

            this.trendingFieldsCollection.fetch({
                data: {
                    fieldNames: [this.model.get('field')],
                    databases: this.queryModel.get('indexes'),
                    queryText: this.queryModel.get('autoCorrect') && this.queryModel.get('correctedQuery')
                        ? this.queryModel.get('correctedQuery')
                        : this.queryModel.get('queryText'),
                    fieldText: toFieldTextNode(this.getFieldText()),
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    minScore: this.queryModel.get('minScore'),
                    maxValues: this.numberOfValuesToDisplay
                },
                success: function() {
                    this.selectedField = this.trendingFieldsCollection.filter(function(model) {
                        return model.get('id') === this.model.get('field');
                    }, this);

                    if(this.selectedField.length === 0) {
                        this.viewStateModel.set('dataState', dataState.EMPTY);
                    } else {
                        this.fetchRangeData();
                    }
                }.bind(this),
                error: function(collection, xhr) {
                    this.onDataError(xhr);
                }.bind(this)
            })
        },

        fetchRangeData: function() {
            const trendingValues = _.first(this.selectedField[0].get('values'), this.numberOfValuesToDisplay);
            const trendingValuesRestriction = 'MATCH{' + _.pluck(trendingValues, 'value').toString() + '}:' + this.model.get('field');
            const fieldText = this.getFieldText().length > 0 ? ' AND ' + toFieldTextNode(this.getFieldText()) : '';

            this.parametricDetailsModel = new ParametricDetailsModel();
            this.parametricDetailsModel.fetch({
                data: {
                    fieldName: this.dateField,
                    queryText: this.queryModel.get('queryText'),
                    fieldText: trendingValuesRestriction + fieldText,
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    minScore: this.queryModel.get('minScore'),
                    databases: this.queryModel.get('indexes')
                },
                success: function() {
                    this.setMinMax(this.parametricDetailsModel.get('min'), this.parametricDetailsModel.get('max'));
                    this.fetchBucketingData();
                }.bind(this),
                error: function(collection, xhr) {
                    this.onDataError(xhr);
                }.bind(this)
            });
        },

        fetchBucketingData: function() {
            this.bucketedValues = {};
            this.viewStateModel.set('fetchState', fetchState.FETCHING_BUCKETS);

            _.each(_.first(this.selectedField[0].get('values'), this.numberOfValuesToDisplay), function(value) {
                this.bucketedValues[value.value] = new BucketedParametricCollection.Model({
                    id: this.dateField,
                    valueName: value.value
                });
            }, this);

            const minDate = this.model.get('currentMin'), maxDate = this.model.get('currentMax');

            if(minDate === maxDate) {
                this.model.set('currentMin', minDate - SECONDS_IN_ONE_DAY);
                this.model.set('currentMax', maxDate + SECONDS_IN_ONE_DAY);
            }

            $.when.apply($, _.map(this.bucketedValues, function(model) {
                const fieldText = this.getFieldText().length > 0 ? ' AND ' + toFieldTextNode(this.getFieldText()) : '';
                return model.fetch({
                    data: {
                        queryText: this.queryModel.get('queryText'),
                        fieldText: 'MATCH{' + model.get('valueName') + '}:' + this.model.get('field') + fieldText,
                        minDate: this.queryModel.getIsoDate('minDate'),
                        maxDate: this.queryModel.getIsoDate('maxDate'),
                        minScore: this.queryModel.get('minScore'),
                        databases: this.queryModel.get('indexes'),
                        targetNumberOfBuckets: this.targetNumberOfBuckets,
                        bucketMin: this.model.get('currentMin'),
                        bucketMax: this.model.get('currentMax')
                    }
                });
            }, this)).done(_.bind(function() {
                this.viewStateModel.set('currentState', renderState.RENDERING_NEW_DATA);
                this.viewStateModel.set('dataState', dataState.OK);
                this.viewStateModel.set('fetchState', fetchState.NOT_FETCHING);
                this.updateChart();
            }, this)).fail(_.bind(function(xhr) {
                this.onDataError(xhr);
                this.viewStateModel.set('fetchState', fetchState.NOT_FETCHING);
            }, this));
        },

        updateChart: function() {
            this.$('[data-toggle="tooltip"]').tooltip('destroy');

            const data = this.createChartData();
            const callbacks = this.createCallbacks();

            if(this.viewStateModel.get('fetchState') !== fetchState.FETCHING_BUCKETS) {
                let minDate, maxDate;
                if(this.viewStateModel.get('currentState') === renderState.RENDERING_NEW_DATA) {
                    minDate = data[0].points[0].mid;
                    maxDate = data[data.length - 1].points[data[0].points.length - 1].mid;
                } else {
                    minDate = new Date(this.model.get('currentMin') * MILLISECONDS_TO_SECONDS);
                    maxDate = new Date(this.model.get('currentMax') * MILLISECONDS_TO_SECONDS);
                }

                this.trendingChart.draw({
                    reloaded: this.viewStateModel.get('currentState') === renderState.RENDERING_NEW_DATA,
                    data: data,
                    minDate: minDate,
                    maxDate: maxDate,
                    xAxisLabel: i18n['search.resultsView.trending.xAxis'],
                    yAxisLabel: i18n['search.resultsView.trending.yAxis'],
                    zoomCallback: callbacks.zoomCallback,
                    dragMoveCallback: callbacks.dragMoveCallback,
                    dragEndCallback: callbacks.dragEndCallback
                });
            }
        },

        createChartData: function() {
            const data = [];

            _.each(this.bucketedValues, function(model) {
                data.push({
                    points: _.map(model.get('values'), function(value) {
                        return {
                            count: value.count,
                            mid: Math.floor(value.min + ((value.max - value.min) / 2)),
                            min: value.min,
                            max: value.max
                        };
                    }),
                    name: model.get('valueName')
                });
            });

            _.each(data, function(value) {
                _.each(value.points, function(point) {
                    point.mid = new Date(point.mid * MILLISECONDS_TO_SECONDS);
                    point.min = new Date(point.min * MILLISECONDS_TO_SECONDS);
                    point.max = new Date(point.max * MILLISECONDS_TO_SECONDS);
                });
            });

            return this.adjustBuckets(data, this.model.get('currentMin'), this.model.get('currentMax'));
        },

        adjustBuckets: function(values, min, max) {
            return _.map(values, function(value) {
                return {
                    name: value.name,
                    points: _.filter(value.points, function(point) {
                        const date = new Date(point.mid).getTime() / MILLISECONDS_TO_SECONDS;
                        return date >= min && date <= max;
                    })
                }
            });
        },

        createCallbacks: function() {
            const zoomCallback = function(min, max) {
                this.setMinMax(min, max);
                this.viewStateModel.set('currentState', renderState.ZOOMING);
                this.updateChart();
                this.debouncedFetchBucketingData();
            }.bind(this);

            const dragMoveCallback = function(min, max) {
                this.setMinMax(min, max);
                this.viewStateModel.set('currentState', renderState.DRAGGING);
                this.updateChart();
            }.bind(this);

            const dragEndCallback = function(min, max) {
                this.setMinMax(min, max);
                this.viewStateModel.set('currentState', renderState.DRAGGING);
                this.debouncedFetchBucketingData();
            }.bind(this);

            return {
                zoomCallback: zoomCallback,
                dragMoveCallback: dragMoveCallback,
                dragEndCallback: dragEndCallback
            }
        },

        getFieldText: function() {
            return this.selectedParametricValues.map(function(model) {
                return model.toJSON();
            });
        },

        setMinMax: function(min, max) {
            this.model.set({
                currentMin: Math.floor(min),
                currentMax: Math.floor(max)
            });
        },

        onDataStateChange: function() {
            this.$('.trending-error').toggleClass('hide', this.viewStateModel.get('dataState') !== dataState.ERROR);
            this.$('.trending-empty').toggleClass('hide', this.viewStateModel.get('dataState') !== dataState.EMPTY);
            this.$('.trending-loading').toggleClass('hide', this.viewStateModel.get('dataState') !== dataState.LOADING);
            this.$('.trending-chart').toggleClass('hide', this.viewStateModel.get('dataState') !== dataState.OK);

            if(this.viewStateModel.get('dataState') !== dataState.ERROR && this.$errorMessage) {
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
                const errorMessage = generateErrorHtml(messageArguments);
                this.$errorMessage.html(errorMessage);
            }
        }
    });
});
