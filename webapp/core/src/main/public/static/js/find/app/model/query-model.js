/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/util/search-data-util',
    'underscore'
], function(Backbone, searchDataUtil, _) {
    'use strict';

    /**
     * @readonly
     * @enum {String}
     */
    const Sort = {
        date: 'date',
        relevance: 'relevance'
    };

    const DEBOUNCE_WAIT_MILLISECONDS = 500;

    const collectionBuildIndexes = function (collection) {
        return searchDataUtil.buildIndexes(collection.map(function (model) {
            return model.pick('domain', 'name');
        }));
    };

    function makeQueryText(queryState) {
        return searchDataUtil.makeQueryText(queryState.conceptGroups.pluck('concepts'));
    }

    return Backbone.Model.extend({
        defaults: {
            autoCorrect: true,
            queryText: '',
            indexes: [],
            fieldText: null,
            minDate: undefined,
            maxDate: undefined,
            minScore: 0,
            sort: Sort.relevance,
            stateMatchIds: [],
            promotionsStateMatchIds: []
        },

        /**
         * @param {Object} attributes
         * @param {{queryState: QueryState, enableAutoCorrect: boolean}} options
         */
        initialize: function(attributes, options) {
            this.queryState = options.queryState;

            this.listenTo(this.queryState.conceptGroups, 'change:concepts update reset', function() {
                const queryText = makeQueryText(this.queryState);

                if(queryText) {
                    const newAttributes = {correctedQuery: '', queryText: queryText};

                    if (options.enableAutoCorrect) {
                        // Reset auto-correct whenever the search text changes
                        newAttributes.autoCorrect = true;
                    }

                    this.set(newAttributes);
                }
            });

            this.listenTo(this.queryState.datesFilterModel, 'change', function() {
                this.set(this.queryState.datesFilterModel.toQueryModelAttributes());
            });

            this.listenTo(this.queryState.minScoreModel, 'change', function() {
                this.set('minScore', this.queryState.minScoreModel.get('minScore'));
            });

            this.listenTo(this.queryState.selectedIndexes, 'update reset', _.bind(function() {
                this.set('indexes', collectionBuildIndexes(this.queryState.selectedIndexes));
            }, this));

            this.listenTo(this.queryState.selectedParametricValues, 'add remove reset change', _.debounce(_.bind(function() {
                const fieldTextNode = this.queryState.selectedParametricValues.toFieldTextNode();
                this.set('fieldText', fieldTextNode ? fieldTextNode : null);
            }, this), DEBOUNCE_WAIT_MILLISECONDS));

            const fieldTextNode = this.queryState.selectedParametricValues.toFieldTextNode();

            this.set(_.extend({
                queryText: makeQueryText(this.queryState),
                minScore: this.queryState.minScoreModel.get('minScore'),
                indexes: collectionBuildIndexes(this.queryState.selectedIndexes),
                fieldText: fieldTextNode ? fieldTextNode : null
            }, this.queryState.datesFilterModel.toQueryModelAttributes()));
        },

        getIsoDate: function(type) {
            const date = this.get(type);

            if(date) {
                return date.toISOString();
            } else {
                return null;
            }
        }
    }, {
        Sort: Sort
    });
});
