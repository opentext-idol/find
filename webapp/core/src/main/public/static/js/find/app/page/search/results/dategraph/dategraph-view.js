/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'backbone',
    'underscore',
    'find/app/util/topic-map-view',
    'find/app/model/entity-collection',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/page/search/filters/parametric/calibrate-buckets',
    'find/app/model/bucketed-parametric-collection',
    'parametric-refinement/to-field-text-node',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/dategraph/dategraph-view.html',
    'text!find/templates/app/page/loading-spinner.html',
    'iCheck',
    'slider/bootstrap-slider',
    'flot.time'
], function(Backbone, _, TopicMapView, EntityCollection, i18n, configuration, calibrateBuckets, BucketedParametricCollection, toFieldTextNode, generateErrorHtml, template,
            loadingTemplate) {
    'use strict';

    var loadingHtml = _.template(loadingTemplate)({i18n: i18n, large: true});

    function rangeModelMatching(fieldName, dataType) {
        return function(model) {
            return model.get('field') === fieldName && model.get('range') && model.get('dataType') === dataType;
        };
    }

    return Backbone.View.extend({
        template: _.template(template),

        events: {
        },

        initialize: function(options) {
            this.queryState = options.queryState;

            this.queryModel = options.queryModel;
            this.pixelsPerBucket = options.pixelsPerBucket || 20;

            this.fieldName = 'autn_date';
            this.dataType = 'date';

            this.bucketModel = new BucketedParametricCollection.Model({id: this.fieldName});
            this.selectedParametricValues = options.queryState.selectedParametricValues;

            this.listenTo(this.queryModel, 'change', this.fetchBuckets);

            this.listenTo(this.bucketModel, 'change:values request sync error', this.updateGraph);
        },

        update: function() {
        },

        updateGraph: function() {
            var hadError = this.bucketModel.error;
            var fetching = this.bucketModel.fetching;
            var modelBuckets = this.bucketModel.get('values');
            var noValues = !modelBuckets || !modelBuckets.length;

            this.$('.dategraph-view-error-message').toggleClass('hide', !hadError);
            this.$('.dategraph-view-empty-text').toggleClass('hide', hadError || !noValues);

            var showLoadingIndicator = !hadError && !noValues && (fetching && modelBuckets.length === 0);
            this.$('.dategraph-loading').toggleClass('hide', !showLoadingIndicator);


            var $contentEl = this.$('.dategraph-content');
            var width = $contentEl.width();

            if(!hadError && !noValues && !showLoadingIndicator && width > 0) {

                $.plot($contentEl[0], [_.map(modelBuckets, function(a){
                    return [0.5e3 * (a.min + a.max), a.count]
                })], {
                    xaxis: {mode: 'time'}
                })
            }
        },

        fetchBuckets: function() {
            var width = this.$('.dategraph-content').width();

            // If the SVG has no width or there are no values, there is no point fetching new data
            // if(width !== 0 && this.model.get('totalValues') !== 0) {
            if(width) {
                // Exclude any restrictions for this field from the field text
                var otherSelectedValues = this.selectedParametricValues
                    .reject(rangeModelMatching(this.fieldName, this.dataType))
                    .map(function(model) {
                        return model.toJSON();
                    });

                var minDate = this.queryModel.getIsoDate('minDate');
                var maxDate = this.queryModel.getIsoDate('maxDate');

                this.bucketModel.fetch({
                    data: {
                        queryText: this.queryModel.get('queryText'),
                        fieldText: toFieldTextNode(otherSelectedValues),
                        minDate: minDate,
                        maxDate: maxDate,
                        minScore: this.queryModel.get('minScore'),
                        databases: this.queryModel.get('indexes'),
                        targetNumberOfBuckets: Math.floor(width / this.pixelsPerBucket),
                        // TODO: do this properly
                        bucketMin: Math.floor((new Date().getTime() - 86400e3*4*365)/1000),
                        bucketMax: Math.floor(new Date().getTime()/1000)
                    }
                });
            }
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                errorTemplate: this.errorTemplate,
                loadingHtml: loadingHtml,
                cid: this.cid
            }));

            this.fetchBuckets();
        }
    });
});
