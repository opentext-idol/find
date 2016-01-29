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

    function momentsEqual(maybeMoment1, maybeMoment2) {
        if (!maybeMoment1) {
            return !maybeMoment2;
        } else if (!maybeMoment2) {
            return false;
        } else {
            return maybeMoment1.isSame(maybeMoment2);
        }
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

        /**
         * Does this model represent the same search as the given query state?
         * @param {QueryState} queryState
         * @return {Boolean}
         */
        equalsQueryState: function(queryState) {
            return this.get('queryText') === queryState.queryTextModel.get('inputText')
                    && momentsEqual(this.get('minDate'), queryState.queryModel.get('minDate'))
                    && momentsEqual(this.get('maxDate'), queryState.queryModel.get('maxDate'))
                    && arraysEqual(this.get('relatedConcepts'), queryState.queryTextModel.get('relatedConcepts'))
                    && arraysEqual(this.get('indexes'), queryState.selectedIndexes.toResourceIdentifiers(), _.isEqual)
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