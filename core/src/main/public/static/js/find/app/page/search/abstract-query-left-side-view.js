/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/page/search/filters/date/dates-filter-view',
    'find/app/page/search/filters/parametric/parametric-view',
    'find/app/page/search/filters/parametric/numeric-parametric-view',
    'find/app/page/search/filters/parametric/numeric-parametric-field-view',
    'find/app/model/bucketed-parametric-collection',
    'find/app/util/text-input',
    'find/app/util/collapsible',
    'find/app/util/filtering-collection',
    'parametric-refinement/prettify-field-name',
    'find/app/vent',
    'parametric-refinement/display-collection',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'moment',
    'text!find/templates/app/page/search/filters/parametric/numeric-parametric-field-view.html',
    'text!find/templates/app/page/search/filters/parametric/numeric-date-parametric-field-view.html'
], function(Backbone, $, _, DateView, ParametricView, NumericParametricView, NumericParametricFieldView, BucketedParametricCollection, TextInput, Collapsible, FilteringCollection, prettifyFieldName,
            vent, ParametricDisplayCollection, configuration, i18n, i18nIndexes, moment, numericParametricFieldTemplate, numericParametricDateFieldTemplate) {
    "use strict";

    var datesTitle = i18n['search.dates'];

    var DEFAULT_TARGET_NUMBER_OF_PIXELS_PER_BUCKET = 10;

    function filterPredicate(filterModel, model) {
        var searchText = filterModel.get('text');
        return searchText ? searchMatches(prettifyFieldName(model.id), filterModel.get('text')) : true;
    }

    function searchMatches(text, search) {
        return text.toLowerCase().indexOf(search.toLowerCase()) > -1;
    }

    return Backbone.View.extend({
        // Abstract
        IndexesView: null,
        getBucketingRequestData: null,

        initialize: function(options) {
            this.filterModel = new Backbone.Model();

            this.filterInput = new TextInput({
                model: this.filterModel,
                modelAttribute: 'text',
                templateOptions: {
                    placeholder: i18n['search.filters.filter']
                }
            });

            this.indexesEmpty = false;

            //noinspection JSUnresolvedFunction
            var indexesView = new this.IndexesView({
                queryModel: options.queryModel,
                indexesCollection: options.indexesCollection,
                selectedDatabasesCollection: options.queryState.selectedIndexes,
                filterModel: this.filterModel,
                visibleIndexesCallback: _.bind(function(indexes) {
                    this.indexesEmpty = indexes.length === 0;
                    this.updateIndexesVisibility();
                    this.updateEmptyMessage();
                }, this)
            });

            var dateView = new DateView({
                datesFilterModel: options.queryState.datesFilterModel,
                savedSearchModel: options.savedSearchModel
            });

            this.numericParametricFieldsCollection = options.numericParametricFieldsCollection;
            this.dateParametricFieldsCollection = options.dateParametricFieldsCollection;
            
            this.numericBucketedCollection = new BucketedParametricCollection();
            this.dateBucketedCollection = new BucketedParametricCollection();

            this.filteredNumericCollection = new FilteringCollection([], {
                filterModel: this.filterModel,
                collection: this.numericBucketedCollection,
                predicate: filterPredicate,
                resetOnFilter: false
            });

            this.filteredDateCollection = new FilteringCollection([], {
                filterModel: this.filterModel,
                collection: this.dateBucketedCollection,
                predicate: filterPredicate,
                resetOnFilter: false
            });

            this.parametricDisplayCollection = new ParametricDisplayCollection([], {
                parametricCollection: options.parametricCollection,
                selectedParametricValues: options.queryState.selectedParametricValues,
                filterModel: this.filterModel
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.parametricDisplayCollection, 'update reset', function() {
                this.updateParametricVisibility();
                this.updateEmptyMessage();
            });

            this.numericParametricView = new NumericParametricView({
                queryModel: options.queryModel,
                queryState: options.queryState,
                collection: this.filteredNumericCollection,
                fieldTemplate: numericParametricFieldTemplate,
                defaultTargetNumberOfPixelsPerBucket: DEFAULT_TARGET_NUMBER_OF_PIXELS_PER_BUCKET,
                numericRestriction: true,
                selectionEnabled: true,
                zoomEnabled: true,
                buttonsEnabled: true
            });

            this.dateParametricView = new NumericParametricView({
                queryModel: options.queryModel,
                queryState: options.queryState,
                collection: this.filteredDateCollection,
                fieldTemplate: numericParametricDateFieldTemplate,
                defaultTargetNumberOfPixelsPerBucket: DEFAULT_TARGET_NUMBER_OF_PIXELS_PER_BUCKET,
                formatting: NumericParametricFieldView.dateFormatting
            });

            this.refreshNumericFields = _.bind(this.refreshFields, this, this.numericParametricFieldsCollection, this.numericBucketedCollection, this.numericParametricView);
            this.refreshDateFields = _.bind(this.refreshFields, this, this.dateParametricFieldsCollection, this.dateBucketedCollection, this.dateParametricView);
            
            //noinspection JSUnresolvedFunction
            this.listenTo(vent, 'vent:resize', function () {
                this.refreshNumericFields();
                this.refreshDateFields();
            });

            this.parametricView = new ParametricView({
                queryModel: options.queryModel,
                queryState: options.queryState,
                filterModel: this.filterModel,
                indexesCollection: options.indexesCollection,
                parametricCollection: options.parametricCollection,
                displayCollection: this.parametricDisplayCollection
            });

            this.indexesViewWrapper = new Collapsible({
                view: indexesView,
                collapsed: false,
                title: i18nIndexes['search.indexes']
            });

            this.dateViewWrapper = new Collapsible({
                view: dateView,
                collapsed: false,
                title: datesTitle
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.filterModel, 'change', function() {
                this.updateDatesVisibility();
                this.updateParametricVisibility();
                this.updateEmptyMessage();
            });

            this.listenTo(this.numericParametricFieldsCollection, 'update reset',  this.refreshNumericFields);
            this.listenTo(this.dateParametricFieldsCollection, 'update reset',  this.refreshDateFields);

            this.$emptyMessage = $('<p class="hide">' + i18n['search.filters.empty'] + '</p>');
        },

        render: function() {
            //noinspection JSUnresolvedVariable
            this.$el.empty()
                .append(this.filterInput.$el)
                .append(this.$emptyMessage)
                .append(this.indexesViewWrapper.$el)
                .append(this.dateViewWrapper.$el)
                .append(this.numericParametricView.$el)
                .append(this.dateParametricView.$el)
                .append(this.parametricView.$el);

            this.filterInput.render();
            this.indexesViewWrapper.render();
            this.numericParametricView.render();
            this.dateParametricView.render();
            this.parametricView.render();
            this.dateViewWrapper.render();

            this.updateParametricVisibility();
            this.updateDatesVisibility();
            this.updateIndexesVisibility();
            this.updateEmptyMessage();

            return this;
        },

        remove: function() {
            //noinspection JSUnresolvedFunction
            _.invoke([
                this.numericParametricView,
                this.dateParametricView,
                this.parametricView,
                this.indexesViewWrapper,
                this.dateViewWrapper
            ], 'remove');

            Backbone.View.prototype.remove.call(this);
        },

        updateEmptyMessage: function() {
            var noFiltersMatched = !(this.indexesEmpty && this.hideDates && this.parametricDisplayCollection.length === 0 && this.filteredNumericCollection.length === 0 && this.filteredDateCollection.length === 0);

            this.$emptyMessage.toggleClass('hide', noFiltersMatched);
        },

        updateParametricVisibility: function() {
            this.numericParametricView.$el.toggleClass('hide', this.numericParametricFieldsCollection.length === 0 && Boolean(this.filterModel.get('text')));
            this.dateParametricView.$el.toggleClass('hide', this.dateParametricFieldsCollection.length === 0 && Boolean(this.filterModel.get('text')));
            this.parametricView.$el.toggleClass('hide', this.parametricDisplayCollection.length === 0 && Boolean(this.filterModel.get('text')));
        },

        updateDatesVisibility: function() {
            var search = this.filterModel.get('text');
            this.hideDates = !(!search || searchMatches(datesTitle, search));

            this.dateViewWrapper.$el.toggleClass('hide', this.hideDates);
        },

        updateIndexesVisibility: function() {
            this.indexesViewWrapper.$el.toggleClass('hide', this.indexesEmpty);
        },

        refreshFields: function (fieldsCollection, bucketedCollection, view) {
            var fieldNames = fieldsCollection.pluck('id');
            if (fieldNames.length > 0) {
                //noinspection JSUnresolvedVariable,JSUnresolvedFunction
                var targetNumberOfBuckets = _.times(fieldsCollection.length, _.constant(Math.floor(view.$el.width() / DEFAULT_TARGET_NUMBER_OF_PIXELS_PER_BUCKET)));

                bucketedCollection.fetch({
                    data: this.getBucketingRequestData(fieldNames, targetNumberOfBuckets),
                    reset: false
                });
            } else {
                bucketedCollection.reset();
            }
        }
    });

});
