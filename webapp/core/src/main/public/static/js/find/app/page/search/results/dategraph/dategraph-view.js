/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'underscore',
    'd3',
    'find/app/util/topic-map-view',
    'find/app/model/entity-collection',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/page/search/filters/parametric/calibrate-buckets',
    'find/app/page/search/results/field-selection-view',
    'find/app/model/bucketed-parametric-collection',
    'parametric-refinement/to-field-text-node',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/dategraph/dategraph-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'iCheck',
    'slider/bootstrap-slider',
    'flot.time'
], function(Backbone, _, d3, TopicMapView, EntityCollection, i18n, configuration, calibrateBuckets, FieldSelectionView,
            BucketedParametricCollection, toFieldTextNode, generateErrorHtml, template,
            loadingTemplate) {
    'use strict';

    var loadingHtml = _.template(loadingTemplate)({i18n: i18n, large: true});

    var category10 = d3.scale.category10();

    function rangeModelMatching(fieldName, dataType) {
        var upperCase = fieldName.toUpperCase();

        return function(model) {
            return model.get('field').toUpperCase() === upperCase && model.get('range') && model.get('dataType') === dataType;
        };
    }

    function dateModelMatching(fieldName, dataType) {
        var upperCase = fieldName.toUpperCase();

        return function(model) {
            return model.id.toUpperCase() === upperCase && model.get('dataType') === dataType;
        };
    }

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .dategraph-view-pptx': function(evt){
                evt.preventDefault();

                var modelBuckets = this.bucketModel.get('values');
                var rows = [];
                var multiAxes = !this.hideMainPlot && this.plots.length > 1;

                if (!this.hideMainPlot) {
                    rows.push({
                        color: '#00B388',
                        label: i18n['search.resultsView.dategraph.defaultSeriesLabel'],
                        secondaryAxis: false,
                        values: _.pluck(modelBuckets, 'count')
                    })
                }

                _.each(this.plots, function(plot){
                    var label = plot.field.replace(/^.*\//, '').replace(/_/g, '\u00A0') + ': ' + plot.value;

                    rows.push({
                        color: category10(label),
                        label: label,
                        secondaryAxis: multiAxes,
                        values: _.pluck(plot.model.get('values'), 'count')
                    })
                })

                var data = {
                    rows: rows,
                    timestamps: _.map(modelBuckets, function(a) {
                        return Math.round(0.5*(a.min+a.max));
                    })
                }

                var $form = $('<form class="hide" enctype="multipart/form-data" method="post" target="_blank" action="api/bi/export/ppt/dategraph"><textarea name="data"></textarea><input type="submit"></form>');
                $form[0].data.value = JSON.stringify(data)
                $form.appendTo(document.body).submit().remove()
            }
        },

        initialize: function(options) {
            this.queryState = options.queryState;

            this.queryModel = options.queryModel;
            this.pixelsPerBucket = options.pixelsPerBucket || 20;

            this.fieldName = 'autn_date';
            this.dataType = 'date';

            this.bucketModel = new BucketedParametricCollection.Model({id: this.fieldName});
            this.selectedParametricValues = options.queryState.selectedParametricValues;
            this.dateParametricFieldsCollection = options.dateParametricFieldsCollection;

            this.listenTo(this.queryModel, 'change', this.fetchBuckets);

            this.listenTo(this.bucketModel, 'change:values request sync error', this.updateGraph);

            this.listenTo(this.selectedParametricValues, 'graph', this.graphRequest)

            this.plots = []
        },

        graphRequest: function(field, value){
            if (!_.where(this.plots, { field: field, value: value }).length) {
                var model = new BucketedParametricCollection.Model({id: this.fieldName});
                var subPlot = { field: field, value: value, model: model };
                this.plots.push(subPlot);
                this.listenTo(model, 'change:values', this.updateGraph)
                this.lastOtherSelectedValues && this.fetchSubPlot(subPlot)
            }
        },

        update: function() {
        },

        updateGraph: function() {
            if (this.$tooltip) {
                this.$tooltip.hide()
            }

            var hadError = this.bucketModel.error;
            var fetching = this.bucketModel.fetching;
            var modelBuckets = this.bucketModel.get('values');
            var noValues = !modelBuckets || !modelBuckets.length;

            this.$('.dategraph-view-error-message').toggleClass('hide', !hadError);
            this.$('.dategraph-view-empty-text').toggleClass('hide', hadError || !noValues || fetching);

            var hideLoadingIndicator = hadError || !fetching;
            this.$('.dategraph-loading').toggleClass('hide', hideLoadingIndicator);


            var $contentEl = this.$('.dategraph-content');
            var width = $contentEl.width();

            function transform(values) {
                return values.map(function (a) {
                    return [0.5e3 * (a.min + a.max), a.count, a.min, a.max]
                })
            }

            if(!hadError && !noValues && hideLoadingIndicator && width > 0) {
                var multiAxes = !this.hideMainPlot && this.plots.length > 1;

                var data = (this.hideMainPlot ? [] : [{
                    color: '#00B388',
                    label: i18n['search.resultsView.dategraph.defaultSeriesLabel'],
                    data: transform(modelBuckets)
                }]).concat(_.map(this.plots, function(plot, idx, plots){
                    var label = plot.field.replace(/^.*\//, '').replace(/_/g, '\u00A0') + ': ' + plot.value;
                    return {
                        color: category10(label),
                        label: label,
                        data: transform(plot.model.get('values')),
                        yaxis: multiAxes ? 2 : 1
                    }
                }))

                var yaxes = [{ minTickSize: 1 }];

                if (multiAxes) {
                    yaxes.push({ minTickSize: 1, position: 'right' });
                }

                $.plot($contentEl[0], data, {
                    grid: { hoverable: true },
                    xaxis: {mode: 'time'},
                    yaxes: yaxes
                })

                this.$('.dategraph-view-pptx').removeClass('disabled');
            }
            else {
                $contentEl.empty();
                this.$('.dategraph-view-pptx').addClass('disabled');
            }
        },

        fetchBuckets: function() {
            var width = this.$('.dategraph-content').width();

            // If the SVG has no width or there are no values, there is no point fetching new data
            // if(width !== 0 && this.model.get('totalValues') !== 0) {
            if(width) {
                var rangeFilter = this.selectedParametricValues.find(rangeModelMatching(this.fieldName, this.dataType));

                var otherSelectedValues = this.selectedParametricValues
                    .map(function(model) {
                        return model.toJSON();
                    });

                var dateRange = rangeFilter && rangeFilter.get('range');

                var minDate = this.queryModel.getIsoDate('minDate');
                var maxDate = this.queryModel.getIsoDate('maxDate');

                var dateField = this.dateParametricFieldsCollection.find(dateModelMatching(this.fieldName, this.dataType));

                var baseParams = {
                    queryText: this.queryModel.get('queryText'),
                    fieldText: toFieldTextNode(otherSelectedValues),
                    minDate: minDate,
                    maxDate: maxDate,
                    minScore: this.queryModel.get('minScore'),
                    databases: this.queryModel.get('indexes'),
                    targetNumberOfBuckets: Math.floor(width / this.pixelsPerBucket),
                    bucketMin: dateRange ? dateRange[0] : dateField ? dateField.get('min') : Math.floor((new Date().getTime() - 86400e3*365)/1000),
                    bucketMax: dateRange ? dateRange[1] : dateField ? dateField.get('max') : Math.floor(new Date().getTime()/1000)
                };

                this.lastBaseParams = baseParams;
                this.lastOtherSelectedValues = otherSelectedValues;

                this.hideMainPlot = false;

                this.bucketModel.fetch({
                    data: baseParams
                });

                _.each(this.plots, this.fetchSubPlot, this)
            }
        },

        fetchSubPlot: function(plot){
            var newFieldText = toFieldTextNode([{field: plot.field, value: plot.value}])
            var plotSelectedValues = toFieldTextNode(this.lastOtherSelectedValues);

            plot.model.set([])
            plot.model.fetch({
                data: _.defaults({
                    fieldText: plotSelectedValues ? '(' + plotSelectedValues + ') AND (' + newFieldText + ')': newFieldText
                }, this.lastBaseParams)
            })
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                errorTemplate: this.errorTemplate,
                loadingHtml: loadingHtml,
                cid: this.cid
            }));

            this.$('.dategraph-content').on('click .legendColorBox', _.bind(function(evt){
                var idx = $(evt.target).closest('tr').index(), removed

                if (idx >= 0) {
                    if (this.hideMainPlot) {
                        removed = this.plots.splice(idx, 1)
                        this.stopListening(removed[0].model)

                        if (!this.plots.length) {
                            this.hideMainPlot = false;
                        }

                        this.updateGraph();
                    }
                    else if (idx) {
                        removed = this.plots.splice(idx - 1, 1)
                        this.stopListening(removed[0].model)

                        this.updateGraph();
                    }
                    else if (this.plots.length) {
                        this.hideMainPlot = true;
                        this.updateGraph();
                    }
                }
            }, this)).on('plothover', _.bind(function(evt, pos, item){
                function lPad2(num) {
                    return num < 10 ? '0' + num : num;
                }

                function formatDate(epochSeconds){
                    var date = new Date(1000 * epochSeconds);
                    return [
                        [date.getFullYear(), lPad2(date.getMonth() + 1), lPad2(date.getDate())].join('-'),
                        [lPad2(date.getHours()), lPad2(date.getMinutes())].join(':')
                    ]
                }

                if (item) {
                    if (!this.$tooltip) {
                        this.$tooltip = $('<div class="tooltip top" role="tooltip"><div class="tooltip-arrow"></div><div class="tooltip-inner" style="text-align: center; white-space: nowrap"></div></div>').appendTo(this.$el)
                    }
                    var origPt = item.series.data[item.dataIndex]
                    var minStr = formatDate(origPt[2]), maxStr = formatDate(origPt[3])

                    var timeStr = minStr[0] === maxStr[0] ? maxStr[0] + ' ' + minStr[1] + ' to ' + maxStr[1] : minStr[0] + ' to ' + maxStr[0];

                    this.$tooltip.find('.tooltip-inner').html(timeStr + '<br>' + _.escape(item.series.label) + ': ' + item.datapoint[1])
                    this.$tooltip.show()
                        .css({ top: item.pageY - 20 - this.$tooltip.height(), left: item.pageX - 0.5 * this.$tooltip.width(), opacity: 1 })
                }
                else if (this.$tooltip) {
                    this.$tooltip.hide()
                }
            }, this)).on('mouseout', _.bind(function(){
                if (this.$tooltip) {
                    this.$tooltip.hide()
                }
            }, this))

            this.fetchBuckets();
        }
    });
});
