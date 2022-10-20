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
    'find/app/util/array-equality',
    'find/app/model/query-model',
    'parametric-refinement/selected-values-collection',
    'find/app/util/database-name-resolver',
    'find/app/model/dates-filter-model',
    'find/app/model/geography-model',
    'find/app/model/document-selection-model'
], function(_, Backbone, moment, arraysEqual, QueryModel, SelectedParametricValuesCollection,
            databaseNameResolver, DatesFilterModel, GeographyModel, DocumentSelectionModel) {
    'use strict';

    /**
     * Models representing the state of a search.
     * @typedef {Object} QueryState
     * @property {DatesFilterModel} datesFilterModel Contains the date restrictions
     * @property {Backbone.Collection} conceptGroups
     * @property {Backbone.Collection} selectedIndexes
     * @property {Backbone.Collection} selectedParametricValues
     */

    /**
     * The attributes saved on a saved search model.
     * @typedef {Object} SavedSearchModelAttributes
     * @property {String} title
     * @property {String[][]} relatedConcepts
     * @property {{name: String, domain: String}[]} indexes
     * @property {boolean} documentSelectionIsWhitelist
     * @property {{reference: String}[]} documentSelection
     * @property {{field: String, value: String}[]} parametricValues
     * @property {{field: String, min: Number, max: Number, type: String}[]} parametricRanges
     * @property {Integer} minScore
     * @property {Moment} minDate
     * @property {Moment} maxDate
     * @property {Moment} dateModified
     * @property {Moment} dateCreated
     * @property {DateRange} dateRange
     * @property
     */
    const DATE_FIELDS = [
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
    const Type = {
        QUERY: 'QUERY',
        READ_ONLY_QUERY: 'READ_ONLY_QUERY',
        READ_ONLY_SNAPSHOT: 'READ_ONLY_SNAPSHOT',
        SHARED_QUERY: 'SHARED_QUERY',
        SHARED_READ_ONLY_QUERY: 'SHARED_READ_ONLY_QUERY',
        SHARED_READ_ONLY_SNAPSHOT: 'SHARED_READ_ONLY_SNAPSHOT',
        SHARED_SNAPSHOT: 'SHARED_SNAPSHOT',
        SNAPSHOT: 'SNAPSHOT'
    };

    function parseParametricRestrictions(models) {
        const parametricValues = [];
        const parametricRanges = [];

        models.forEach(function(model) {
            if(model.has('value')) {
                parametricValues.push({
                    field: model.get('field'),
                    displayName: model.get('displayName'),
                    value: model.get('value'),
                    displayValue: model.get('displayValue')
                });
            } else if(model.has('range')) {
                parametricRanges.push({
                    field: model.get('field'),
                    displayName: model.get('displayName'),
                    min: model.get('range')[0],
                    max: model.get('range')[1],
                    type: model.get('type') === 'Numeric' ? 'Numeric' : 'Date'
                });
            }
        });

        return {
            parametricValues: parametricValues,
            parametricRanges: parametricRanges
        };
    }

    function parseGeographyModel(model) {
        const parsed = [];

        if (model) {
            _.each(model.toJSON(), function(filters, locationField){
                _.each(filters, function(filter){
                    parsed.push({ field: locationField, json: JSON.stringify(filter) });
                })
            })
        }

        return parsed;
    }

    /**
     * Determine SavedSearchModel attributes corresponding to a DocumentSelectionModel.
     */
    function parseDocumentSelectionModel(model) {
        return {
            documentSelectionIsWhitelist: model.get('isWhitelist'),
            documentSelection: _.map(model.getReferences(), function (ref) {
                return { reference: ref };
            })
        };
    }

    function compareWithoutDisplayNames(x, y) {
        return _.isEqual(_.omit(x, ['displayName', 'displayValue']), _.omit(y, 'displayName', 'displayValue'));
    }

    function nullOrUndefined(input) {
        return input === null || input === undefined;
    }

    const optionalMomentsEqual = optionalEqual(function(optionalMoment1, optionalMoment2) {
        return optionalMoment1.isSame(optionalMoment2);
    });

    const optionalExactlyEqual = optionalEqual(function(optionalItem1, optionalItem2) {
        return optionalItem1 === optionalItem2;
    });

    // Treat as equal if they are both either null or undefined, or pass a regular equality test
    function optionalEqual(equalityTest) {
        return function(optionalItem1, optionalItem2) {
            if(nullOrUndefined(optionalItem1)) {
                return nullOrUndefined(optionalItem2);
            } else if(nullOrUndefined(optionalItem2)) {
                return false;
            } else {
                return equalityTest(optionalItem1, optionalItem2);
            }
        };
    }

    const arrayEqualityPredicate = _.partial(arraysEqual, _, _, _.isEqual);

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
            title: null,
            indexes: [],
            geographyFilters: [],
            documentSelectionIsWhitelist: false,
            documentSelection: [],
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
            const dateAttributes = _.mapObject(_.pick(response, DATE_FIELDS), function(value) {
                return value && moment(value);
            });

            const relatedConcepts = _.chain(response.conceptClusterPhrases)
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
            const tokensByType = _.chain(response.stateTokens)
                .groupBy('type')
                .mapObject(function(arr) {
                    return _.pluck(arr, 'stateToken');
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
            return Backbone.Model.prototype.destroy.call(this, _.extend(options || {}, {
                // The server returns an empty body (ie: not JSON)
                dataType: 'text'
            }));
        },

        urlRoot: function() {
            const modelType = this.get('type');
            const isShared = modelType.indexOf('SHARED') !== -1;
            const isQuery = modelType.indexOf('QUERY') !== -1;

            return 'api/bi/' +
                (isQuery ? 'saved-query' : 'saved-snapshot') +
                (isShared ? '/shared' : '');
        },

        /**
         * Does this model represent the same search as the given query state?
         * @param {QueryState} queryState
         * @return {Boolean}
         */
        equalsQueryState: function(queryState) {
            const selectedIndexes = databaseNameResolver.getDatabaseInfoFromCollection(queryState.selectedIndexes);

            const parametricRestrictions = parseParametricRestrictions(queryState.selectedParametricValues);
            const geographyFilters = parseGeographyModel(queryState.geographyModel);
            const documentSelection = parseDocumentSelectionModel(queryState.documentSelectionModel);

            return this.equalsQueryStateDateFilters(queryState)
                && arraysEqual(this.get('relatedConcepts'), queryState.conceptGroups.pluck('concepts'), arrayEqualityPredicate)
                && arraysEqual(this.get('indexes'), selectedIndexes, _.isEqual)
                && this.get('minScore') === queryState.minScoreModel.get('minScore')
                && arraysEqual(this.get('parametricValues'), parametricRestrictions.parametricValues, compareWithoutDisplayNames)
                && arraysEqual(this.get('parametricRanges'), parametricRestrictions.parametricRanges, compareWithoutDisplayNames)
                && arraysEqual(this.get('geographyFilters'), geographyFilters, _.isEqual)
                && this.get('documentSelectionIsWhitelist') === documentSelection.documentSelectionIsWhitelist
                && arraysEqual(this.get('documentSelection'), documentSelection.documentSelection, _.isEqual);
        },

        equalsQueryStateDateFilters: function(queryState) {
            const datesAttributes = queryState.datesFilterModel.toQueryModelAttributes();

            if(this.get('dateRange') === DatesFilterModel.DateRange.CUSTOM) {
                return this.get('dateRange') === datesAttributes.dateRange
                    && optionalMomentsEqual(this.get('minDate'), datesAttributes.minDate)
                    && optionalMomentsEqual(this.get('maxDate'), datesAttributes.maxDate);
            } else {
                return optionalExactlyEqual(this.get('dateRange'), datesAttributes.dateRange);
            }
        },

        toDatesFilterModelAttributes: function() {
            const minDate = this.get('minDate');
            const maxDate = this.get('maxDate');

            return {
                dateRange: this.get('dateRange'),
                customMinDate: minDate,
                customMaxDate: maxDate,
                dateNewDocsLastFetched: this.get('dateNewDocsLastFetched')
            };
        },

        toGeographyModelAttributes: function() {
            const geographyFilters = this.get('geographyFilters') || [];
            const map = {};

            _.each(_.groupBy(geographyFilters, 'field'), function(filterList, locationField){
                if (GeographyModel.LocationFieldsById.hasOwnProperty(locationField)) {
                    map[locationField] = _.map(filterList, function(filter) { return JSON.parse(filter.json) });
                }
            });

            return map
        },

        /**
         * Get DocumentSelectionModel attributes for this saved search.
         */
        toDocumentSelectionModelAttributes: function() {
            return {
                isWhitelist: this.get('documentSelectionIsWhitelist'),
                references: _.pluck(this.get('documentSelection'), 'reference')
            };
        },

        toConceptGroups: function() {
            return this.get('relatedConcepts').map(function(concepts) {
                return {concepts: concepts};
            });
        },

        toMinScoreModelAttributes: function() {
            return this.pick('minScore');
        },

        toSelectedParametricValues: function() {
            const selectedParametricValues = this.get('parametricValues').map(function(fieldAndValues) {
                return _.defaults(fieldAndValues, {type: 'Parametric'});
            });
            const selectedParametricRanges = this.get('parametricRanges').map(function(range) {
                return {
                    field: range.field,
                    displayName: range.displayName,
                    range: [range.min, range.max],
                    type: range.type === 'Numeric' ? 'Numeric' : 'NumericDate'
                };
            });
            return selectedParametricValues.concat(selectedParametricRanges);
        },

        toSelectedIndexes: function() {
            return this.get('indexes');
        },

        toQueryModel: function(IndexesCollection, autoCorrect) {
            // also constructed in find-search:selectContentView
            const queryState = {
                conceptGroups: new Backbone.Collection(this.toConceptGroups()),
                minScoreModel: new Backbone.Model({minScore: 0}),
                datesFilterModel: new DatesFilterModel(this.toDatesFilterModelAttributes()),
                geographyModel: new GeographyModel(this.toGeographyModelAttributes()),
                documentSelectionModel:
                    new DocumentSelectionModel(this.toDocumentSelectionModelAttributes()),
                selectedIndexes: new IndexesCollection(this.get('indexes')),
                selectedParametricValues: new SelectedParametricValuesCollection(this.toSelectedParametricValues())
            };

            return new QueryModel({
                autoCorrect: autoCorrect,
                stateMatchIds: this.get('queryStateTokens'),
                promotionsStateMatchIds: this.get('promotionsStateTokens')
            }, {queryState: queryState});

        }
    }, {
        Type: Type,

        /**
         * Build saved search model attributes from the given query state models.
         * @param {QueryState} queryState
         * @return {SavedSearchModelAttributes}
         */
        attributesFromQueryState: function(queryState) {
            const indexes = databaseNameResolver.getDatabaseInfoFromCollection(queryState.selectedIndexes);
            const parametricRestrictions = parseParametricRestrictions(queryState.selectedParametricValues);
            const geographyFilters = parseGeographyModel(queryState.geographyModel);

            return _.extend(
                {
                    relatedConcepts: queryState.conceptGroups.pluck('concepts'),
                    indexes: indexes,
                    geographyFilters: geographyFilters,
                    parametricValues: parametricRestrictions.parametricValues,
                    parametricRanges: parametricRestrictions.parametricRanges,
                    minScore: queryState.minScoreModel.get('minScore')
                },
                queryState.datesFilterModel.toQueryModelAttributes(),
                parseDocumentSelectionModel(queryState.documentSelectionModel),
                {
                    dateNewDocsLastFetched: moment(),
                    dateDocsLastFetched: moment()
                }
            );
        }
    });
});
