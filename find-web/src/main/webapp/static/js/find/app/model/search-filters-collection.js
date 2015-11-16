define([
    '../../../../bower_components/backbone/backbone',
    'underscore',
    'moment',
    'find/app/model/backbone-query-model',
    'find/app/model/dates-filter-model',
    'find/app/page/date/dates-filter-view',
    'i18n!find/nls/bundle'
], function(Backbone, _, moment, QueryModel, DatesFilterModel, datesFilterView, i18n) {

    var FilterTypes = {
        indexes: 'indexes',
        maxDate: 'maxDate',
        minDate: 'minDate',
        dateRange: 'dateRange',
        PARAMETRIC: 'PARAMETRIC'
    };

    var metaFilterType = {
        date: 'date'
    };

    function getDateFilterText(filterType, dateString) {
        var textPrefixKey = filterType === FilterTypes.maxDate ? 'app.until' : 'app.from';
        return i18n[textPrefixKey] + ': ' + dateString;
    }

    // Get the filter model id for a given parametric field name
    function parametricFilterId(fieldName) {
        return FilterTypes.PARAMETRIC + ':' + fieldName;
    }

    // Get the display text for the given parametric field name and array of selected parametric values
    function parametricFilterText(fieldName, values) {
        return fieldName + ': ' + values.join(', ');
    }

    // Get an array of filter model attributes from the selected parametric values collection
    function extractParametricFilters(selectedParametricValues) {
        return _.map(selectedParametricValues.toFieldsAndValues(), function(values, field) {
            return {
                id: parametricFilterId(field),
                field: field,
                text: parametricFilterText(field, values),
                type: FilterTypes.PARAMETRIC
            };
        });
    }

    // This collection backs the search filters display view. It monitors the query model and selected parmaetric values
    // collection and creates/removes it's own models when they change.
    // When a dates filter model is removed, it updates the appropriate request model attribute with a null value. However,
    // this currently can't be done for the selected databases because the databases view isn't backed by a collection.
    return Backbone.Collection.extend({
        initialize: function(models, options) {
            this.queryModel = options.queryModel;
            this.datesFilterModel = options.datesFilterModel;
            this.indexesCollection = options.indexesCollection;
            this.selectedIndexesCollection = options.selectedIndexesCollection;
            this.selectedParametricValues = options.selectedParametricValues;

            this.listenTo(this.selectedParametricValues, 'add remove', this.updateParametricSelection);
            this.listenTo(this.selectedParametricValues, 'reset', this.resetParametricSelection);
            this.listenTo(this.selectedIndexesCollection, 'reset update', this.updateDatabases);

            this.listenTo(this.queryModel, 'change', function() {
                if (this.queryModel.hasAnyChangedAttributes(['minDate', 'maxDate'])) {
                    var changed = this.queryModel.changedAttributes();
                    var dateFilterTypes = _.intersection(['minDate', 'maxDate'], _.keys(changed));

                    var dateRange = this.datesFilterModel.get('dateRange');

                    if(!_.isEmpty(dateFilterTypes)) {
                        if(dateRange === DatesFilterModel.dateRange.custom) {
                            this.intervalDate(dateFilterTypes);
                        } else if(dateRange) {
                            this.humanDate();
                        } else {
                            this.removeAllDateFilters();
                        }
                    }
                }
            });

            this.on('remove', function(model) {
                var type = model.get('type');

                if (type === FilterTypes.PARAMETRIC) {
                    var field = model.get('field');
                    this.selectedParametricValues.remove(this.selectedParametricValues.where({field: field}));
                } else if (type === FilterTypes.indexes) {
                    this.selectedIndexesCollection.set(this.indexesCollection.toResourceIdentifiers());
                }
            });

            if (this.queryModel.get('minDate')) {
                models.push({
                    id: FilterTypes.minDate,
                    type: FilterTypes.minDate,
                    metaType: metaFilterType.date,
                    text: getDateFilterText(FilterTypes.minDate, moment(this.queryModel.get('minDate')).format('LLL'))
                });
            }

            if (this.queryModel.get('maxDate')) {
                models.push({
                    id: FilterTypes.maxDate,
                    type: FilterTypes.maxDate,
                    metaType: metaFilterType.date,
                    text: getDateFilterText(FilterTypes.maxDate, moment(this.queryModel.get('maxDate')).format('LLL'))
                });
            }

            if (!this.allIndexesSelected()) {
                models.push({
                    id: FilterTypes.indexes,
                    type: FilterTypes.indexes,
                    text: this.getDatabasesFilterText()
                });
            }

            Array.prototype.push.apply(models, extractParametricFilters(this.selectedParametricValues));
        },

        getDatabasesFilterText: function() {
            var selectedIndexNames = this.selectedIndexesCollection.pluck('name');
            return i18n['search.indexes'] + ': ' + selectedIndexNames.join(', ');
        },

        allIndexesSelected: function() {
            return this.indexesCollection.length === this.selectedIndexesCollection.length;
        },

        updateDatabases: function() {
            var filterModel = this.get(FilterTypes.indexes);

            if (!this.allIndexesSelected()) {
                var filterText = this.getDatabasesFilterText();

                if (filterModel) {
                    filterModel.set('text', filterText);
                } else {
                    // The databases filter model has equal id and type since only one filter of this type can be present
                    this.add({id: FilterTypes.indexes, type: FilterTypes.indexes, text: filterText});
                }
            } else if (filterModel) {
                this.remove(filterModel);
            }
        },

        removeAllDateFilters: function() {
            this.remove(this.where({metaType: metaFilterType.date}));
        },

        humanDate: function() {
            this.removeAllDateFilters();

            var dateRange = this.datesFilterModel.get('dateRange');

            if (dateRange) {
                this.add({
                    id: dateRange,
                    type: FilterTypes.dateRange,
                    metaType: metaFilterType.date,
                    text: i18n['search.dates.timeInterval.' + dateRange]
                });
            }
        },

        intervalDate: function(filterTypes) {
            this.remove(this.where({type: FilterTypes.dateRange}));

            _.each(filterTypes, function(filterType) {
                var filterModel = this.get(filterType);

                var date = this.queryModel.get(filterType);

                if (date) {
                    var displayDate = date.format('LLL');
                    var filterText = getDateFilterText(filterType, displayDate);

                    if (filterModel) {
                        filterModel.set('text', filterText);
                    } else {
                        // Date filter models have equal id and type attributes since only one model of each type can be present
                        this.add({
                            id: filterType,
                            type: filterType,
                            metaType: metaFilterType.date,
                            text: filterText
                        });
                    }
                } else if(filterModel) {
                    this.remove(filterModel);
                }
            }, this);
        },

        // Handles add and remove events from the selected parametric values collection
        updateParametricSelection: function(selectionModel) {
            var field = selectionModel.get('field');
            var fieldDisplayName = selectionModel.get('fieldDisplayName');
            var id = parametricFilterId(field);
            var modelsForField = this.selectedParametricValues.where({field: field});

            if (modelsForField.length) {
                this.add({
                    id: id,
                    field: field,
                    text: parametricFilterText(fieldDisplayName, _.invoke(modelsForField, 'get', 'value')),
                    type: FilterTypes.PARAMETRIC
                }, {
                    // Merge true to overwrite the text for any existing model for this field name
                    merge: true
                });
            } else {
                // this.remove(id) doesn't work when this has been called in response to a different remove event
                this.remove(this.where({id: id}));
            }
        },

        resetParametricSelection: function() {
            this.remove(this.where({type: FilterTypes.PARAMETRIC}));
            this.add(extractParametricFilters(this.selectedParametricValues));
        }
    }, {
        FilterTypes: FilterTypes,
        metaFilterTypes: metaFilterType
    });

});
