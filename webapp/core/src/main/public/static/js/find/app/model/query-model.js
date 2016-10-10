/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/util/search-data-util',
    'parametric-refinement/to-fields-and-ranges'
], function(Backbone, searchDataUtil, toFieldsAndRanges) {
    
    'use strict';
    
    /**
     * @readonly
     * @enum {String}
     */
    var Sort = {
        date: 'date',
        relevance: 'relevance'
    };

    var DEBOUNCE_WAIT_MILLISECONDS = 500;

    var collectionBuildIndexes = function(collection) {
        return searchDataUtil.buildIndexes(collection.map(function (model) {
            return model.pick('domain', 'name');
        }));
    };

    return Backbone.Model.extend({
        defaults: {
            autoCorrect: true,
            queryText: '',
            indexes: [],
            field_matches: null,
            field_ranges: null,
            minDate: undefined,
            maxDate: undefined,
            minScore: 0,
            sort: Sort.relevance,
            stateMatchIds: [],
            promotionsStateMatchIds: []
        },

        /**
         * @param {Object} attributes
         * @param {{queryState: QueryState}} options
         */
        initialize: function(attributes, options) {
            this.queryState = options.queryState;

            this.listenTo(this.queryState.queryTextModel, 'change', function() {
                var queryText = this.queryState.queryTextModel.makeQueryText();
                if (queryText) {
                    this.set('queryText', queryText);
                }
            });

            this.listenTo(this.queryState.datesFilterModel, 'change', function() {
                this.set(this.queryState.datesFilterModel.toQueryModelAttributes());
            });

            this.listenTo(this.queryState.minScoreModel, 'change', function() {
                this.set('minScore', this.queryState.minScoreModel.get('minScore'));
            });

            this.listenTo(this.queryState.selectedIndexes, 'update reset', _.debounce(_.bind(function() {
                this.set('indexes', collectionBuildIndexes(this.queryState.selectedIndexes));
            }, this), DEBOUNCE_WAIT_MILLISECONDS));

            this.listenTo(this.queryState.selectedParametricValues, 'add remove reset change', _.debounce(_.bind(function() {
                var fieldTextNode = this.queryState.selectedParametricValues.toFieldTextNode();
                this.set('fieldText', fieldTextNode ? fieldTextNode : null);
            }, this), DEBOUNCE_WAIT_MILLISECONDS));

            var fieldTexts = toFieldsAndRanges(this.queryState.selectedParametricValues.models);

            this.set(_.extend({
                queryText: this.queryState.queryTextModel.makeQueryText(),
                minScore: this.queryState.minScoreModel.get('minScore'),
                indexes: collectionBuildIndexes(this.queryState.selectedIndexes),
                field_matches: fieldTexts.parametricValues || null,
                field_ranges: fieldTexts.parametricRanges || null
            }, this.queryState.datesFilterModel.toQueryModelAttributes()));
        },

        getFieldMatches: function() {
            return _.map(toFieldsAndRanges(this.queryState.selectedParametricValues.models).parametricValues, function(value) {
                return value.field + "::" + value.value;
            }).join(',');
        },

        getFieldRanges: function() {
            return _.map(toFieldsAndRanges(this.queryState.selectedParametricValues.models).parametricRanges, function(range) {
                return range.field + "::" + range.min + "::" + range.max + "::" + range.type;
            }).join(',');
        },

        getIsoDate: function(type) {
            var date = this.get(type);

            if (date) {
                return date.toISOString();
            } else {
                return null;
            }
        }
    }, {
        Sort: Sort
    });

});
