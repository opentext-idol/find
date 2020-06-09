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
    'backbone',
    'moment',
    'find/app/model/dates-filter-model',
    'find/app/model/geography-model',
    'find/app/page/search/filters/parametric/numeric-range-rounder',
    'find/app/util/database-name-resolver',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes'
], function(_, Backbone, moment, DatesFilterModel, GeographyModel, rounder, databaseNameResolver,
            i18n, i18nIndexes) {
    'use strict';

    const DATE_FORMAT = 'YYYY-MM-DD HH:mm';
    const SHORT_DATE_FORMAT = 'YYYY-MM-DD';
    const DATE_SHORTEN_CUTOFF = 7 * 24 * 3600 * 1000; // interval in millseconds at which date format changes to short

    const FilterType = {
        INDEXES: 'INDEXES',
        MAX_DATE: 'MAX_DATE',
        MIN_DATE: 'MIN_DATE',
        DATE_RANGE: 'DATE_RANGE',
        PARAMETRIC: 'PARAMETRIC',
        GEOGRAPHY: 'GEOGRAPHY',
        DOCUMENT_SELECTION: 'DOCUMENT_SELECTION'
    };

    const customDatesFilters = [
        {attribute: 'customMinDate', type: FilterType.MIN_DATE},
        {attribute: 'customMaxDate', type: FilterType.MAX_DATE}
    ];

    function getDateFilterText(filterType, dateString) {
        const textPrefixKey = filterType === FilterType.MAX_DATE
            ? 'app.until'
            : 'app.from';
        return i18n[textPrefixKey] + ': ' + dateString;
    }

    // Get the filter model id for a given parametric field name
    function parametricFilterId(fieldName) {
        return 'param-' + fieldName;
    }

    // Get the filter model id for a given location field name
    function locationFilterId(locationId) {
        return 'loc-' + locationId;
    }

    function formatDate(autnDate, format) {
        return moment(autnDate).format(format);
    }

    // Get the display text for the given parametric field name and array of selected parametric values
    function parametricFilterText(displayValues, ranges, type) {
        let values;

        if(type === 'Parametric') {
            values = displayValues;
        } else if(type === 'Numeric') {
            values = ranges.map(function(range) {
                const round = rounder().round;
                return round(range[0], range[0], range[1]) + ' \u2013 ' + round(range[1], range[0], range[1]);
            });
        } else if(type === 'NumericDate') {
            values = ranges.map(function(range) {
                //Discard time of day if range greater than 1 week
                return range[1] - range[0] > DATE_SHORTEN_CUTOFF
                    ? formatDate(range[0], SHORT_DATE_FORMAT) + ' \u2013 ' + formatDate(range[1], SHORT_DATE_FORMAT)
                    : formatDate(range[0], DATE_FORMAT) + ' \u2013 ' + formatDate(range[1], DATE_FORMAT);
            });
        }

        return values.join(', ');
    }

    // Get an array of filter model attributes from the selected parametric values collection
    function extractParametricFilters(selectedParametricValues) {
        return _.map(selectedParametricValues.toFieldsAndValues(), function(data, field) {
            return {
                id: parametricFilterId(field),
                field: field,
                heading: data.displayName,
                text: parametricFilterText(
                    data.displayValues,
                    data.range
                        ? [data.range]
                        : [],
                    data.type),
                type: FilterType.PARAMETRIC
            };
        });
    }

    // This collection backs the search filters display view. It monitors the query state models and collections and
    // creates/removes it's own models when they change.
    // When a dates filter model is removed, it updates the appropriate request model attribute with a null value. However,
    // this currently can't be done for the selected databases because the databases view isn't backed by a collection.
    return Backbone.Collection.extend({
        initialize: function(models, options) {
            this.indexesCollection = options.indexesCollection;

            this.datesFilterModel = options.queryState.datesFilterModel;
            this.geographyModel = options.queryState.geographyModel;
            this.documentSelectionModel = options.queryState.documentSelectionModel;
            this.selectedIndexesCollection = options.queryState.selectedIndexes;
            this.selectedParametricValues = options.queryState.selectedParametricValues;

            this.listenTo(this.selectedParametricValues, 'add remove change', this.updateParametricSelection);
            this.listenTo(this.selectedParametricValues, 'reset', this.resetParametricSelection);
            this.listenTo(this.selectedIndexesCollection, 'reset update', this.updateDatabases);
            this.listenTo(this.datesFilterModel, 'change', this.updateDateFilters);
            this.listenTo(this.geographyModel, 'change', this.updateGeographyFilters);
            this.listenTo(this.documentSelectionModel, 'change', this.updateDocumentSelection);

            this.on('remove', function(model) {
                const type = model.get('type');

                if(type === FilterType.PARAMETRIC) {
                    const field = model.get('field');
                    this.selectedParametricValues.remove(this.selectedParametricValues.where({field: field}));
                } else if(type === FilterType.INDEXES) {
                    this.selectedIndexesCollection.set(databaseNameResolver.getDatabaseInfoFromCollection(this.indexesCollection));
                } else if(type === FilterType.DATE_RANGE) {
                    if(this.datesFilterModel.get('dateRange') !== DatesFilterModel.DateRange.CUSTOM) {
                        this.datesFilterModel.set('dateRange', null);
                    }
                } else if(type === FilterType.MAX_DATE) {
                    this.datesFilterModel.set('customMaxDate', null);
                } else if(type === FilterType.MIN_DATE) {
                    this.datesFilterModel.set('customMinDate', null);
                } else if(type === FilterType.GEOGRAPHY) {
                    this.geographyModel.set(model.get('locationId'), null);
                } else if(type === FilterType.DOCUMENT_SELECTION) {
                    this.documentSelectionModel.reset();
                }
            });

            const dateRange = this.datesFilterModel.get('dateRange');

            if(dateRange) {
                if(dateRange === DatesFilterModel.DateRange.CUSTOM) {
                    _.each(customDatesFilters, function(filterData) {
                        const currentValue = this.datesFilterModel.get(filterData.attribute);

                        if(currentValue) {
                            models.push({
                                id: filterData.type,
                                type: filterData.type,
                                text: getDateFilterText(filterData.type, currentValue.format('LLL'))
                            });
                        }
                    }, this);
                } else {
                    models.push({
                        id: FilterType.DATE_RANGE,
                        type: FilterType.DATE_RANGE,
                        text: i18n['search.dates.timeInterval.' + dateRange]
                    });
                }
            }

            if(!this.allIndexesSelected()) {
                models.push({
                    id: FilterType.INDEXES,
                    type: FilterType.INDEXES,
                    text: this.getDatabasesFilterText()
                });
            }

            _.each(this.geographyModel.attributes, function(shapes, id){
                if (shapes && shapes.length) {
                    const locationField = GeographyModel.LocationFieldsById[id];
                    models.push({
                        id: locationFilterId(id),
                        locationId: id,
                        type: FilterType.GEOGRAPHY,
                        text: locationField.displayName,
                        heading: i18n['search.geography']
                    });
                }
            }, this);

            if (!this.documentSelectionModel.isDefault()) {
                models.push({
                    id: FilterType.DOCUMENT_SELECTION,
                    type: FilterType.DOCUMENT_SELECTION,
                    text: this.documentSelectionModel.describe(),
                    heading: i18n['search.documentSelection.title']
                });
            }

            Array.prototype.push.apply(models, extractParametricFilters(this.selectedParametricValues));
        },

        getDatabasesFilterText: function() {
            const selectedIndexNames = this.selectedIndexesCollection.map(function(model) {
                //noinspection JSUnresolvedFunction
                return databaseNameResolver.getDatabaseDisplayNameFromDatabaseModel(this.indexesCollection, model);
            }.bind(this));
            return selectedIndexNames.join(', ');
        },

        allIndexesSelected: function() {
            return this.indexesCollection.length === this.selectedIndexesCollection.length;
        },

        updateDatabases: function() {
            const filterModel = this.get(FilterType.INDEXES);

            if(this.allIndexesSelected()) {
                if(this.contains(filterModel)) {
                    this.remove(filterModel);
                }
            } else {
                const filterText = this.getDatabasesFilterText();

                if(filterModel) {
                    filterModel.set('text', filterText);
                } else {
                    // The databases filter model has equal id and type since only one filter of this type can be present
                    this.add({
                        id: FilterType.INDEXES,
                        type: FilterType.INDEXES,
                        text: filterText,
                        heading: i18nIndexes['search.indexes']
                    });
                }
            }
        },

        // Handles add and remove events from the selected parametric values collection
        updateParametricSelection: function(selectionModel) {
            const field = selectionModel.get('field');
            const id = parametricFilterId(field);
            const modelsForField = this.selectedParametricValues.where({field: field});

            if(modelsForField.length) {
                const displayValues = _.chain(modelsForField).invoke('get', 'displayValue').compact().value();
                const ranges = _.chain(modelsForField).invoke('get', 'range').compact().value();

                this.add({
                    id: id,
                    field: field,
                    text: parametricFilterText(displayValues, ranges, selectionModel.get('type')),
                    type: FilterType.PARAMETRIC,
                    heading: selectionModel.get('displayName')
                }, {
                    // Merge true to overwrite the text for any existing model for this field name
                    merge: true
                });
            } else {
                // this.remove(id) doesn't work when this has been called in response to a different remove event
                this.remove(this.where({id: id}));
            }
        },

        updateDateFilters: function() {
            const dateRange = this.datesFilterModel.get('dateRange');

            if(dateRange) {
                if(dateRange === DatesFilterModel.DateRange.CUSTOM) {
                    // Remove any last <period> date filter
                    this.remove(this.where({id: FilterType.DATE_RANGE}));

                    _.each(customDatesFilters, function(filterData) {
                        const currentValue = this.datesFilterModel.get(filterData.attribute);

                        if(currentValue) {
                            const existingModel = this.get(filterData.type);
                            const filterText = getDateFilterText(filterData.type, currentValue.format('LLL'));

                            if(existingModel) {
                                existingModel.set('text', filterText);
                            } else {
                                this.add({
                                    id: filterData.type,
                                    type: filterData.type,
                                    text: filterText,
                                    heading: null
                                });
                            }
                        } else {
                            this.remove(this.where({id: filterData.type}));
                        }
                    }, this);
                } else {
                    // Remove any custom filters
                    this.remove(this.filter(function(model) {
                        return _.contains([FilterType.MAX_DATE, FilterType.MIN_DATE], model.id);
                    }));

                    const existingDateRangeModel = this.get(FilterType.DATE_RANGE);
                    const filterText = i18n['search.dates.timeInterval.' + dateRange];

                    if(existingDateRangeModel) {
                        existingDateRangeModel.set('text', filterText);
                    } else {
                        this.add({
                            id: FilterType.DATE_RANGE,
                            type: FilterType.DATE_RANGE,
                            text: filterText,
                            heading: i18n['search.dates']
                        });
                    }
                }
            } else {
                // No date range selected so remove all date filter models
                this.remove(this.filter(function(model) {
                    return _.contains([FilterType.DATE_RANGE, FilterType.MAX_DATE, FilterType.MIN_DATE], model.id);
                }));
            }
        },

        updatethaoeunsthaoetnshutnsaoehueographyFilters: function() {
            _.each(GeographyModel.LocationFields, function(locationField){
                const id = locationField.id;

                const existing = this.findWhere({ type: FilterType.GEOGRAPHY, locationId: id });
                const shapes = this.geographyModel.get(id);
                const shouldShow = shapes && shapes.length;

                if (shouldShow) {
                    const text = locationField.displayName;
                    if (existing) {
                        existing.set('text', text);
                    }
                    else {
                        this.add({
                            id: locationFilterId(id),
                            locationId: id,
                            type: FilterType.GEOGRAPHY,
                            text: text,
                            heading: i18n['search.geography']
                        });
                    }
                }
                else if(existing) {
                    this.remove(existing);
                }
            }, this);
        },

        updateDocumentSelection: function () {
            const currentModel = this.findWhere({ type: FilterType.DOCUMENT_SELECTION });
            const isDefault = this.documentSelectionModel.isDefault();

            if (!currentModel && !isDefault) {
                this.add({
                    id: FilterType.DOCUMENT_SELECTION,
                    type: FilterType.DOCUMENT_SELECTION,
                    text: this.documentSelectionModel.describe(),
                    heading: i18n['search.documentSelection.title']
                });
            } else if (currentModel && isDefault) {
                this.remove(currentModel);
            } else if (currentModel && !isDefault) {
                currentModel.set('text', this.documentSelectionModel.describe());
            }
        },

        resetParametricSelection: function() {
            this.remove(this.where({type: FilterType.PARAMETRIC}));
            this.add(extractParametricFilters(this.selectedParametricValues));
        }
    }, {
        FilterType: FilterType
    });
});
