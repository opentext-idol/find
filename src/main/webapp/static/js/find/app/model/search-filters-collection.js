define([
    'backbone',
    'underscore',
    'i18n!find/nls/bundle'
], function(Backbone, _, i18n) {

    var FilterTypes = {
        DATABASES: 'DATABASES',
        MAX_DATE: 'MAX_DATE',
        MIN_DATE: 'MIN_DATE',
        PARAMETRIC: 'PARAMETRIC'
    };

    function getDateFilterText(filterType, date) {
        // Filters model date attributes are moments
        var dateString = date;//.format('LL');
        var textPrefixKey = filterType === FilterTypes.MAX_DATE ? 'app.until' : 'app.from';
        return i18n[textPrefixKey] + ': ' + dateString;
    }

    function updateDateFilter(filtersCollection, filterType, date) {
        var filterModel = filtersCollection.get(filterType);

        if (date) {
            var filterText = getDateFilterText(filterType, date);

            if (filterModel) {
                filterModel.set('text', filterText);
           } else {
                // Date filter models have equal id and type attributes since only one model of each type can be present
                filtersCollection.add({id: filterType, type: filterType, text: filterText});
            }
        } else if (filtersCollection.contains(filterModel)) {
            filtersCollection.remove(filterModel);
        }
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

            this.listenTo(this.queryModel, 'change:minDate', this.updateMinDate);
            this.listenTo(this.queryModel, 'change:maxDate', this.updateMaxDate);
            this.listenTo(this.queryModel, 'change:indexes', this.updateDatabases);
            this.listenTo(this.queryModel, 'change:fieldText', this.setParametricFieldText(this.queryModel.get('fieldText')));

            this.listenTo(this.queryModel, 'change', console.log(_.clone(this.queryModel)));

            // Update the search request model when a dates filter is removed
            this.on('remove', function(model) {
                var type = model.get('type');

                if (type === FilterTypes.MAX_DATE) {
                    this.queryModel.set('maxDate', null);
                } else if (type === FilterTypes.MIN_DATE) {
                    this.queryModel.set('minDate', null);
                }
            });

            if (this.queryModel.getIsoDate('minDate')) {
                models.push({
                    id: FilterTypes.MIN_DATE,
                    type: FilterTypes.MIN_DATE,
                    text: getDateFilterText(FilterTypes.MIN_DATE, this.queryModel.getIsoDate('minDate'))
                });
            }

            if (this.queryModel.getIsoDate('maxDate')) {
                models.push({
                    id: FilterTypes.MAX_DATE,
                    type: FilterTypes.MAX_DATE,
                    text: getDateFilterText(FilterTypes.MAX_DATE, this.queryModel.getIsoDate('maxDate'))
                });
            }

            if (this.queryModel.get('allIndexesSelected')) {
                models.push({
                    id: FilterTypes.DATABASES,
                    type: FilterTypes.DATABASES,
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
            var filterModel = this.get(FilterTypes.DATABASES);

            if (!this.queryModel.get('allIndexesSelected')) {
                var filterText = this.getDatabasesFilterText();

                if (filterModel) {
                    filterModel.set('text', filterText);
                } else {
                    // The databases filter model has equal id and type since only one filter of this type can be present
                    this.add({id: FilterTypes.DATABASES, type: FilterTypes.DATABASES, text: filterText});
                }
            } else if (this.contains(filterModel)) {
                this.remove(filterModel);
            }
        },

        updateMaxDate: function() {
            updateDateFilter(this, FilterTypes.MAX_DATE, this.queryModel.getIsoDate('maxDate'));
        },

        updateMinDate: function() {
            updateDateFilter(this, FilterTypes.MIN_DATE, this.queryModel.getIsoDate('minDate'));
        }
    }, {
        FilterTypes: FilterTypes
    });

});
