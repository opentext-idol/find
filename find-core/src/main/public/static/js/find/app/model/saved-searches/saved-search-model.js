define([
    'backbone',
    'moment',
    'underscore',
    'find/app/util/array-equality',
    'find/app/model/dates-filter-model'
], function(Backbone, moment, _, arraysEqual, DatesFilterModel) {

    /**
     * Models representing the state of a search.
     * @typedef {Object} QueryState
     * @property {DatesFilterModel} datesFilterModel Contains the date restrictions
     * @property {Backbone.Model} queryTextModel Contains the input text and related concepts
     * @property {Backbone.Collection} selectedIndexes
     * @property {Backbone.Collection} selectedParametricValues
     */

    /**
     * The attributes saved on a saved search model.
     * @typedef {Object} SavedSearchModelAttributes
     * @property {String} title
     * @property {String} queryText
     * @property {String[]} relatedConcepts
     * @property {{name: String, domain: String}[]} indexes
     * @property {{field: String, value: String}[]} parametricValues
     * @property {Moment} minDate
     * @property {Moment} maxDate
     * @property {Moment} dateModified
     * @property {Moment} dateCreated
     */

    var DATE_FIELDS = [
        'minDate',
        'maxDate',
        'dateCreated',
        'dateModified'
    ];

    function pickFieldAndValue(model) {
        return model.pick('field', 'value');
    }

    function nullOrUndefined(input) {
        return input === null || input === undefined;
    }

    // Treat moments are equal if they are both either null or undefined, or represent the same instant
    function optionalMomentsEqual(optionalMoment1, optionalMoment2) {
        if (nullOrUndefined(optionalMoment1)) {
            return nullOrUndefined(optionalMoment2);
        } else if (nullOrUndefined(optionalMoment2)) {
            return false;
        } else {
            return optionalMoment1.isSame(optionalMoment2);
        }
    }

    // TODO: Remove this when toResourceIdentifiers consistently returns null for domains against IDOL
    function selectedIndexToResourceIdentifier(selectedIndex) {
        // Selected indexes against IDOL are either null, undefined or the empty string; normalize to null here
        return {name: selectedIndex.name, domain: selectedIndex.domain || null};
    }

    return Backbone.Model.extend({
        defaults: {
            queryText: null,
            title: null,
            indexes: [],
            parametricValues: [],
            relatedConcepts: []
        },

        parse: function(response) {
            var dateAttributes = _.mapObject(_.pick(response, DATE_FIELDS), function(value) {
                return value && moment(value);
            });

            return _.defaults(dateAttributes, response);
        },

        destroy: function(options) {
            return Backbone.Model.prototype.destroy.call(this, _.extend(options || options, {
                // The server returns an empty body (ie: not JSON)
                dataType: 'text'
            }));
        },

        /**
         * Does this model represent the same search as the given query state?
         * @param {QueryState} queryState
         * @return {Boolean}
         */
        equalsQueryState: function(queryState) {
            var selectedIndexes = _.map(queryState.selectedIndexes.toResourceIdentifiers(), selectedIndexToResourceIdentifier);
            var datesAttributes = queryState.datesFilterModel.toQueryModelAttributes();

            return this.get('queryText') === queryState.queryTextModel.get('inputText')
                    && optionalMomentsEqual(this.get('minDate'), datesAttributes.minDate)
                    && optionalMomentsEqual(this.get('maxDate'), datesAttributes.maxDate)
                    && arraysEqual(this.get('relatedConcepts'), queryState.queryTextModel.get('relatedConcepts'))
                    && arraysEqual(this.get('indexes'), selectedIndexes, _.isEqual)
                    && arraysEqual(this.get('parametricValues'), queryState.selectedParametricValues.map(pickFieldAndValue), _.isEqual);
        },

        toDatesFilterModelAttributes: function() {
            var minDate = this.get('minDate');
            var maxDate = this.get('maxDate');

            return {
                dateRange: minDate || maxDate ? DatesFilterModel.DateRange.CUSTOM : null,
                customMinDate: minDate,
                customMaxDate: maxDate
            };
        },

        toQueryTextModelAttributes: function() {
            return {
                inputText: this.get('queryText'),
                relatedConcepts: this.get('relatedConcepts')
            };
        },

        toSelectedParametricValues: function() {
            return this.get('parametricValues');
        },

        toSelectedIndexes: function() {
            return this.get('indexes');
        }
    }, {
        /**
         * Build saved search model attributes from the given query state models.
         * @param {QueryState} queryState
         * @return {SavedSearchModelAttributes}
         */
        attributesFromQueryState: function(queryState) {
            var indexes = _.map(queryState.selectedIndexes.toResourceIdentifiers(), selectedIndexToResourceIdentifier);
            var parametricValues = queryState.selectedParametricValues.map(pickFieldAndValue);

            return _.extend({
                queryText: queryState.queryTextModel.get('inputText'),
                relatedConcepts: queryState.queryTextModel.get('relatedConcepts'),
                indexes: indexes,
                parametricValues: parametricValues
            }, queryState.datesFilterModel.toQueryModelAttributes());
        }
    });

});