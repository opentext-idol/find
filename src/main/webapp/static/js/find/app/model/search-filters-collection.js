define([
    'backbone',
    'underscore',
    'moment',
    'find/app/model/backbone-query-model',
    'find/app/page/date/dates-filter-view',
    'i18n!find/nls/bundle'
], function(Backbone, _, moment, QueryModel, datesFilterView, i18n) {

    var FilterTypes = {
        indexes: 'indexes',
        maxDate: 'maxDate',
        minDate: 'minDate',
        HUMANIZE_DATE: 'dateRange',
        PARAMETRIC: 'PARAMETRIC'
    };

    function getDateFilterText(filterType, date) {
        // Filters model date attributes are moments
        var dateString = date;
        var textPrefixKey = filterType === FilterTypes.maxDate ? 'app.until' : 'app.from';
        return i18n[textPrefixKey] + ': ' + dateString;
    }

    // Takes a field text node and deconstructs it into model attributes to add to the collection
    // TODO: This should probably be handled in the parametric view so we don't have to deconstruct the field text
    function deconstructParametricFieldText(node) {
        if(!node || !node.toString()) {
            return [];
        }

        if(_.contains(['AND', 'OR', 'XOR', 'BEFORE', 'AFTER'], node.operator)) {
            var left = deconstructParametricFieldText(node.left);
            var right = deconstructParametricFieldText(node.right);

            return _.flatten([left, right]);
        }

        if (node.operator === 'MATCH') {
            // There should only be one field per MATCH node for parametric field text
            var field = node.fields[0];

            var displayText = field + ': ' + node.values.join(', ');

            return [{
                field: field,
                text: displayText,
                type: FilterTypes.PARAMETRIC,
                id: FilterTypes.PARAMETRIC + ':' + field
            }];
        }
    }

    // This collection backs the search filters display view. It monitors the request model and selected indexes collection
    // and creates/removes it's own models when they change.
    // When a dates filter model is removed, it updates the appropriate request model attribute with a null value. However,
    // this currently can't be done for the selected databases or parametric filters because the databases and parametric
    // views aren't backed by a collection.
    return Backbone.Collection.extend({
        initialize: function(models, options) {
            this.queryModel = options.queryModel;

            this.listenTo(this.queryModel, 'change', function() {
                var changed = this.queryModel.changedAttributes();
                var dateFilterTypes = _.intersection(['minDate', 'maxDate'], _.keys(changed));

                var dateRange = this.queryModel.get('dateRange');

                if(!_.isEmpty(dateFilterTypes)) {
                    if(dateRange === QueryModel.DateRange.custom) {
                        this.intervalDate(dateFilterTypes);
                    } else if(dateRange !== QueryModel.DateRange.nothing) {
                        this.humanDate();
                    } else {
                        this.removeAllDateFilters();
                    }
                }

                if(changed.indexes) {
                    this.updateDatabases();
                }

                if(changed.fieldText) {
                    this.setParametricFieldText(this.queryModel.get('fieldText'))
                }
            });

            // Update the search request model when a dates filter is removed
            this.on('remove', function(model) {
                var type = model.get('type');

                if (type === FilterTypes.maxDate) {
                    this.queryModel.set('maxDate', null);
                } else if (type === FilterTypes.minDate) {
                    this.queryModel.set('minDate', null);
                }
            });

            if (this.queryModel.get('minDate')) {
                models.push({
                    id: FilterTypes.minDate,
                    type: FilterTypes.minDate,
                    text: getDateFilterText(FilterTypes.minDate, moment(this.queryModel.get('minDate')).format('LLL'))
                });
            }

            if (this.queryModel.get('maxDate')) {
                models.push({
                    id: FilterTypes.maxDate,
                    type: FilterTypes.maxDate,
                    text: getDateFilterText(FilterTypes.maxDate, moment(this.queryModel.get('maxDate')).format('LLL'))
                });
            }

            if (!this.queryModel.get('allIndexesSelected')) {
                models.push({
                    id: FilterTypes.indexes,
                    type: FilterTypes.indexes,
                    text: this.getDatabasesFilterText()
                });
            }
        },

        getDatabasesFilterText: function() {
            return i18n['search.indexes'] + ': ' + this.queryModel.get('indexes').join(', ');
        },

        setParametricFieldText: function(node) {
            var newParametricAttributes = deconstructParametricFieldText(node);

            var nonParametricModels = this.filter(function(model) {
                return model.get('type') !== FilterTypes.PARAMETRIC;
            });

            this.set(nonParametricModels.concat(newParametricAttributes));
        },

        updateDatabases: function() {
            var filterModel = this.get(FilterTypes.indexes);

            if (!this.queryModel.get('allIndexesSelected')) {
                var filterText = this.getDatabasesFilterText();

                if (filterModel) {
                    filterModel.set('text', filterText);
                } else {
                    // The databases filter model has equal id and type since only one filter of this type can be present
                    this.add({id: FilterTypes.indexes, type: FilterTypes.indexes, text: filterText});
                }
            } else if (this.contains(filterModel)) {
                this.remove(filterModel);
            }
        },

        removeAllDateFilters: function() {
            this.remove(
                _.union(
                    this.where({type: FilterTypes.HUMANIZE_DATE}),
                    this.where({type: FilterTypes.maxDate}),
                    this.where({type: FilterTypes.minDate})
                )
            );
        },

        humanDate: function() {
            this.removeAllDateFilters();

            var dateRange = this.queryModel.get('dateRange');
            this.add({id: dateRange, type: FilterTypes.HUMANIZE_DATE, text: i18n['search.dates.timeInterval.' + dateRange]});
        },

        intervalDate: function(filterTypes) {
            this.remove(this.where({type: FilterTypes.HUMANIZE_DATE}));

            _.each(filterTypes, function(filterType) {
                var filterModel = this.get(filterType);

                var date = this.queryModel.get(filterType);

                if (filterType && date) {
                    var displayDate = date.format('LLL');
                    var filterText = getDateFilterText(filterType, displayDate);

                    if (filterModel) {
                        filterModel.set('text', filterText);
                    } else {
                        // Date filter models have equal id and type attributes since only one model of each type can be present
                        this.add({id: filterType, type: filterType, text: filterText});
                    }
                } else if (this.contains(filterModel)) {
                    this.remove(filterModel);
                }
            }, this);
        }

    }, {
        FilterTypes: FilterTypes,
        getDateFilterText: getDateFilterText
    });

});
