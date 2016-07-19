define([
    'backbone',
    'moment',
    'underscore',
    'find/app/util/array-equality',
    'find/app/model/dates-filter-model'
], function(Backbone, moment, _, arraysEqual, DatesFilterModel) {
    "use strict";

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
     * @property {String[][]} relatedConcepts
     * @property {{name: String, domain: String}[]} indexes
     * @property {{field: String, value: String}[]} parametricValues
     * @property {{field: String, min: Number, max: Number, type: String}[]} parametricRanges
     * @property {Integer} minScore
     * @property {Moment} minDate
     * @property {Moment} maxDate
     * @property {Moment} dateModified
     * @property {Moment} dateCreated
     * @property {DateRange} dateRange
     */

    var DATE_FIELDS = [
        'minDate',
        'maxDate',
        'dateCreated',
        'dateModified',
        'dateNewDocsLastFetched'
    ];

    /**
     * @readonly
     * @enum {String}
     */
    var Type = {
        QUERY: 'QUERY',
        SNAPSHOT: 'SNAPSHOT'
    };

    function parseParametricRestrictions(models) {
        var parametricValues = [];
        var parametricRanges = [];

        models.forEach(function (model) {
            if (model.has('value')) {
                parametricValues.push({
                    field: model.get('field'),
                    value: model.get('value')
                });
            } else if (model.has('range')) {
                parametricRanges.push({
                    field: model.get('field'),
                    min: model.get('range')[0],
                    max: model.get('range')[1],
                    type: model.get('dataType') === 'numeric' ? 'Numeric' : 'Date'
                })
            }
        });

        return {
            parametricValues: parametricValues,
            parametricRanges: parametricRanges
        };
    }

    function nullOrUndefined(input) {
        return input === null || input === undefined;
    }

    var optionalMomentsEqual = optionalEqual(function(optionalMoment1, optionalMoment2) {
        return optionalMoment1.isSame(optionalMoment2);
    });

    var optionalExactlyEqual = optionalEqual(function(optionalItem1, optionalItem2) {
        return optionalItem1 === optionalItem2;
    });

    // Treat as equal if they are both either null or undefined, or pass a regular equality test
    function optionalEqual(equalityTest) {
        return function(optionalItem1, optionalItem2) {
            if (nullOrUndefined(optionalItem1)) {
                return nullOrUndefined(optionalItem2);
            } else if (nullOrUndefined(optionalItem2)) {
                return false;
            } else {
                return equalityTest(optionalItem1, optionalItem2);
            }
        }
    }

    var arrayEqualityPredicate = _.partial(arraysEqual, _, _, _.isEqual);

    // TODO: Remove this when toResourceIdentifiers consistently returns null for domains against IDOL
    function selectedIndexToResourceIdentifier(selectedIndex) {
        // Selected indexes against IDOL are either null, undefined or the empty string; normalize to null here
        return {name: selectedIndex.name, domain: selectedIndex.domain || null};
    }

    function relatedConceptsToClusterModel(relatedConcepts, clusterId) {
        if(!relatedConcepts.length) {
            return null;
        }

        return _.map(relatedConcepts, function(concept, index) {
            return {
                clusterId: clusterId,
                phrase: concept,
                primary: index === 0
            };
        });
    }

    return Backbone.Model.extend({
        defaults: {
            queryText: null,
            title: null,
            indexes: [],
            parametricValues: [],
            parametricRanges: [],
            relatedConcepts: [],
            minDate: null,
            maxDate: null,
            newDocuments: 0,
            dateRange: null,
            dateNewDocsLastFetched: null,
            minScore: 0
        },

        parse: function(response) {
            var dateAttributes = _.mapObject(_.pick(response, DATE_FIELDS), function(value) {
                return value && moment(value);
            });

            var relatedConcepts = _.chain(response.conceptClusterPhrases)
                .groupBy('clusterId')
                .map(function(clusterPhrases) {
                    return _.chain(clusterPhrases)
                        .sortBy('primary')
                        .reverse()
                        .pluck('phrase')
                        .value();
                })
                .value();
            
            // group token strings by type
            var tokensByType = _.chain(response.stateTokens)
                .groupBy('type')
                .mapObject(function(arr) {
                    return _.pluck(arr, 'stateToken')
                })
                .value();
            
            return _.defaults(dateAttributes, {queryStateTokens: tokensByType.QUERY}, {promotionsStateTokens: tokensByType.PROMOTIONS}, {relatedConcepts: relatedConcepts}, response);
        },

        toJSON: function() {
            return _.defaults({
                conceptClusterPhrases: _.flatten(this.get('relatedConcepts').map(relatedConceptsToClusterModel))
            }, Backbone.Model.prototype.toJSON.call(this));
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

            var parametricRestrictions = parseParametricRestrictions(queryState.selectedParametricValues);
            return this.get('queryText') === queryState.queryTextModel.get('inputText')
                    && this.equalsQueryStateDateFilters(queryState)
                    && arraysEqual(this.get('relatedConcepts'), queryState.queryTextModel.get('relatedConcepts'), arrayEqualityPredicate)
                    && arraysEqual(this.get('indexes'), selectedIndexes, _.isEqual)
                    && this.get('minScore') === queryState.minScoreModel.get('minScore')
                    && arraysEqual(this.get('parametricValues'), parametricRestrictions.parametricValues, _.isEqual)
                    && arraysEqual(this.get('parametricRanges'), parametricRestrictions.parametricRanges, _.isEqual);
        },

        equalsQueryStateDateFilters: function(queryState) {
            var datesAttributes = queryState.datesFilterModel.toQueryModelAttributes();

            if(this.get('dateRange') === DatesFilterModel.DateRange.CUSTOM) {
                return this.get('dateRange') === datesAttributes.dateRange
                    && optionalMomentsEqual(this.get('minDate'), datesAttributes.minDate)
                    && optionalMomentsEqual(this.get('maxDate'), datesAttributes.maxDate);
            } else {
                return optionalExactlyEqual(this.get('dateRange'), datesAttributes.dateRange);
            }
        },

        toDatesFilterModelAttributes: function() {
            var minDate = this.get('minDate');
            var maxDate = this.get('maxDate');

            return {
                dateRange: this.get('dateRange'),
                customMinDate: minDate,
                customMaxDate: maxDate,
                dateNewDocsLastFetched: this.get('dateNewDocsLastFetched')
            };
        },

        toQueryTextModelAttributes: function() {
            return {
                inputText: this.get('queryText'),
                relatedConcepts: this.get('relatedConcepts')
            };
        },

        toMinScoreModelAttributes: function() {
            return this.pick('minScore')
        },

        toSelectedParametricValues: function() {
            return this.get('parametricValues').concat(this.get('parametricRanges').map(function (range) {
                return {
                    field: range.field,
                    range: [range.min, range.max],
                    numeric: range.type === 'Numeric'
                }
            }));
        },

        toSelectedIndexes: function() {
            return this.get('indexes');
        }
    }, {
        Type: Type,

        /**
         * Build saved search model attributes from the given query state models.
         * @param {QueryState} queryState
         * @return {SavedSearchModelAttributes}
         */
        attributesFromQueryState: function(queryState) {
            var indexes = _.map(queryState.selectedIndexes.toResourceIdentifiers(), selectedIndexToResourceIdentifier);
            var parametricRestrictions = parseParametricRestrictions(queryState.selectedParametricValues);

            return _.extend(
                {
                    queryText: queryState.queryTextModel.get('inputText'),
                    relatedConcepts: queryState.queryTextModel.get('relatedConcepts'),
                    indexes: indexes,
                    parametricValues: parametricRestrictions.parametricValues,
                    parametricRanges: parametricRestrictions.parametricRanges,
                    minScore: queryState.minScoreModel.get('minScore')
                },
                queryState.datesFilterModel.toQueryModelAttributes(),
                {
                    dateNewDocsLastFetched: moment(),
                    dateDocsLastFetched: moment()
                }
            );
        }
    });

});