define([
    'backbone',
    'moment',
    'underscore',
    'find/app/util/array-equality'
], function(Backbone, moment, _, arraysEqual) {

    /**
     * Models representing the state of a search.
     * @typedef {Object} QueryState
     * @property {Backbone.Model} queryModel Contains the date restrictions
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

    var QUERY_MODEL_ATTRIBUTES = ['minDate', 'maxDate'];

    function pickFieldAndValue(model) {
        return model.pick('field', 'value');
    }

    function nullOrUndefined(input) {
        return input === null || input === undefined;
    }

    function strictEqual(input1, input2) {
        return input1 === input2;
    }

    // Compare two optional values
    function optionalEqual(compareValues) {
        return function(optional1, optional2) {
            if (nullOrUndefined(optional1)) {
                return nullOrUndefined(optional2);
            } else if (nullOrUndefined(optional2)) {
                return false;
            } else {
                return compareValues(optional1, optional2);
            }
        };
    }

    // Treat domains as equal if they are both either null or undefined, or are strictly equal
    var optionalDomainsEqual = optionalEqual(strictEqual);

    function indexesEqual(index1, index2) {
        return strictEqual(index1.name, index2.name) && optionalDomainsEqual(index1.domain, index2.domain);
    }

    // Treat moments are equal if they are both either null or undefined, or represent the same instant
    var optionalMomentsEqual = optionalEqual(function(moment1, moment2) {
        return moment1.isSame(moment2);
    });

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
            return this.get('queryText') === queryState.queryTextModel.get('inputText')
                    && optionalMomentsEqual(this.get('minDate'), queryState.queryModel.get('minDate'))
                    && optionalMomentsEqual(this.get('maxDate'), queryState.queryModel.get('maxDate'))
                    && arraysEqual(this.get('relatedConcepts'), queryState.queryTextModel.get('relatedConcepts'))
                    && arraysEqual(this.get('indexes'), queryState.selectedIndexes.toResourceIdentifiers(), indexesEqual)
                    && arraysEqual(this.get('parametricValues'), queryState.selectedParametricValues.map(pickFieldAndValue), _.isEqual);
        },

        toQueryModelAttributes: function() {
            return this.pick(QUERY_MODEL_ATTRIBUTES);
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
            var indexes = queryState.selectedIndexes.toResourceIdentifiers();
            var parametricValues = queryState.selectedParametricValues.map(pickFieldAndValue);

            return _.extend({
                queryText: queryState.queryTextModel.get('inputText'),
                relatedConcepts: queryState.queryTextModel.get('relatedConcepts'),
                indexes: indexes,
                parametricValues: parametricValues
            }, queryState.queryModel.pick(QUERY_MODEL_ATTRIBUTES));
        }
    });

});