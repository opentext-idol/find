/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'underscore',
    'find/app/model/bucketed-parametric-collection',
    'find/app/page/search/filters/parametric/abstract-parametric-view',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'js-whatever/js/list-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-view.html'
], function ($, _, BucketedParametricCollection, AbstractView, FieldView, ListView, i18n, template) {
    "use strict";

    const DEFAULT_TARGET_NUMBER_OF_PIXELS_PER_BUCKET = 10;

    var PreInitialisedListView = ListView.extend({
        createItemView: function (model) {
            //noinspection AssignmentResultUsedJS,JSUnresolvedFunction
            var view = this.views[model.cid] = new this.ItemView(_.extend({
                model: model,
                viewWidth: this.$el.width()
            }, this.itemOptions));

            //noinspection JSUnresolvedFunction
            _.each(this.proxyEvents, function (event) {
                this.listenTo(view, event, function () {
                    this.trigger.apply(this, ['item:' + event].concat(Array.prototype.slice.call(arguments, 0)));
                });
            }, this);

            view.render();
            return view;
        }
    });

    return AbstractView.extend({
        getBucketingRequestData: null,
        
        template: _.template(template)({i18n: i18n}),

        initialize: function (options) {
            this.fieldsCollection = options.fieldsCollection;
            this.queryModel = options.queryModel;
            
            this.collection = new BucketedParametricCollection();
            this.monitorCollection(this.collection);

            this.fieldNamesListView = new PreInitialisedListView({
                collection: this.collection,
                ItemView: FieldView,
                itemOptions: {
                    template: options.fieldTemplate,
                    queryModel: options.queryModel,
                    selectedParametricValues: options.queryState.selectedParametricValues,
                    pixelsPerBucket: DEFAULT_TARGET_NUMBER_OF_PIXELS_PER_BUCKET,
                    numericRestriction: options.numericRestriction,
                    stringFormatting: options.stringFormatting,
                    selectionEnabled: options.selectionEnabled,
                    zoomEnabled: options.zoomEnabled,
                    buttonsEnabled: options.buttonsEnabled
                }
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(options.fieldsCollection, 'update reset',  this.refreshFields);
        },

        refreshFields: function () {
            const fieldNames = this.fieldsCollection.pluck('id');
            if (fieldNames.length > 0) {
                //noinspection JSUnresolvedVariable,JSUnresolvedFunction
                const targetNumberOfBuckets = _.times(this.fieldsCollection.length, _.constant(Math.floor(this.$el.width() / DEFAULT_TARGET_NUMBER_OF_PIXELS_PER_BUCKET)));

                this.collection.fetch({
                    data: this.getBucketingRequestData(fieldNames, targetNumberOfBuckets)
                });
            }
        },

        remove: function () {
            this.fieldNamesListView.remove();
            AbstractView.prototype.remove.call(this);
        },

        updateEmpty: $.noop
    });
});