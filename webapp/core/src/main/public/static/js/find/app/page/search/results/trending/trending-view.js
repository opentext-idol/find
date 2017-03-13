define([
    'backbone',
    'underscore',
    'jquery',
    'd3',
    'i18n!find/nls/bundle',
    'find/app/util/generate-error-support-message',
    'find/app/page/search/results/parametric-results-view',
    'find/app/model/bucketed-parametric-collection',
    'find/app/model/parametric-field-details-model',
    'find/app/model/parametric-collection',
    'find/app/page/search/results/trending/trending',
    'parametric-refinement/to-field-text-node',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/templates/app/page/search/results/trending/trending-results-view.html',
    'find/app/vent'
], function (Backbone, _, $, d3, i18n, generateErrorHtml, ParametricResultsView, BucketedParametricCollection,
             ParametricDetailsModel, ParametricCollection, Trending, toFieldTextNode, loadingSpinnerHtml, template, vent) {
    'use strict';

    const MILLISECONDS_TO_SECONDS = 1000;

    return Backbone.View.extend({
        template: _.template(template),
        loadingHtml: _.template(loadingSpinnerHtml),
        dateField: 'AUTN_DATE',
        fieldName: '/DOCUMENT/CATEGORY',
        targetNumberOfBuckets: 10,
        numberOfParametricValuesToShow: 10,

        initialize: function(options) {
            this.parametricCollection = options.parametricCollection;
            this.queryModel = options.queryModel;
            this.selectedParametricValues = options.queryState.selectedParametricValues;

            this.bucketedValues = {};

            this.listenTo(this.parametricCollection, 'sync', this.updateFieldInfo);

            this.listenTo(this.queryModel, 'change', function() {
                if(this.$el.is(':visible')) {
                    this.removeChart();
                    this.fetchRangeData();
                }
            });

            this.listenTo(vent, 'vent:resize', function() {
                if(this.trendingChart && this.$el.is(':visible')) {
                    this.removeChart();
                    this.renderChart();
                }
            });
        },

        render: function() {
            this.bucketedValues = {};
            this.$el.html(this.template({
                i18n: i18n,
                loadingHtml: this.loadingHtml
            }));
            if(this.$el.is(':visible')) {
                this.fetchRangeData();
            }
        },

        fetchRangeData: function () {
            let selectedValues = this.selectedParametricValues.map(function (model) {
                return model.toJSON();
            });

            this.parametricDetailsModel = new ParametricDetailsModel();
            this.parametricDetailsModel.fetch({
                data: {
                    fieldName: this.dateField,
                    queryText: this.queryModel.get('queryText'),
                    fieldText: toFieldTextNode(selectedValues),
                    minDate: this.queryModel.getIsoDate('minDate'),
                    maxDate: this.queryModel.getIsoDate('maxDate'),
                    minScore: this.queryModel.get('minScore'),
                    databases: this.queryModel.get('indexes')
                },
                success: _.bind(function () {
                    this.fetchBucketingData();
                }, this)
            });
        },

        fetchBucketingData: function() {
            // ToDo After rebasing make sure the the new parametric collection changes depending on the query
            // Currently only the max and min dates will change, the values are static

            _.each(_.first(this.selectedField[0].get('values'), this.numberOfParametricValuesToShow), function(value) {
                this.bucketedValues[value.value] = new BucketedParametricCollection.Model({
                    id: 'AUTN_DATE',
                    valueName: value.value
                });
            }, this);

            $.when.apply($, _.map(this.bucketedValues, function(model) {
                return model.fetch({
                    data: {
                        queryText: this.queryModel.get('queryText'),
                        fieldText: 'MATCH{' + model.get('valueName') + '}:' + this.fieldName,
                        minDate: this.queryModel.getIsoDate('minDate'),
                        maxDate: this.queryModel.getIsoDate('maxDate'),
                        minScore: this.queryModel.get('minScore'),
                        databases: this.queryModel.get('indexes'),
                        targetNumberOfBuckets: this.targetNumberOfBuckets,
                        bucketMin: this.parametricDetailsModel.get('min'),
                        bucketMax: this.parametricDetailsModel.get('max')
                    }
                });
            }, this)).done(_.bind(function() {
                this.renderChart();
            }, this));
        },

        renderChart: function() {
            if (!this.trendingChart){
                this.trendingChart = Trending({
                    getContainerCallback: function() {
                        return this.$('#trending-chart').get(0);
                    }.bind(this)
                });
            }

            const data = [];
            const names = [];

            _.each(this.bucketedValues, function (model) {
                data.push(_.zip(_.pluck(model.get('values'), 'max'), _.pluck(model.get('values'), 'count')));
                names.push(model.get('valueName'));
            });

            _.each(data, function (value) {
                _.each(value, function (point) {
                    point[0] = new Date(point[0] * MILLISECONDS_TO_SECONDS);
                });
            });

            this.trendingChart.draw({
                data: data,
                names: names,
                minDate: new Date(this.parametricDetailsModel.get('min') * MILLISECONDS_TO_SECONDS),
                maxDate: new Date(this.parametricDetailsModel.get('max') * MILLISECONDS_TO_SECONDS),
                containerWidth: this.$('#trending-chart').width(),
                containerHeight: this.$('#trending-chart').height()
            });
        },

        removeChart: function() {
            this.$('#trending-chart').empty();
        },

        updateFieldInfo: function() {
            this.selectedField = this.parametricCollection.filter(function(model) {
                return model.get('id') === this.fieldName;
            }, this);
        }
    });
});