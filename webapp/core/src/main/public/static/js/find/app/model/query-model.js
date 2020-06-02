/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'find/app/util/search-data-util'
], function(_, Backbone, searchDataUtil) {
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

    const collectionBuildIndexes = function(collection) {
        return searchDataUtil.buildIndexes(collection.map(function(model) {
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
            promotionsStateMatchIds: [],
            editingDocumentSelection: false,
            fieldTextWithoutDocumentSelection: null
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

                    if(options.enableAutoCorrect) {
                        // Reset auto-correct whenever the search text changes
                        newAttributes.autoCorrect = true;
                    }

                    this.set(newAttributes);
                }
            });

            this.listenTo(this.queryState.datesFilterModel, 'change', function() {
                this.set(this.queryState.datesFilterModel.toQueryModelAttributes());
            });

            this.listenTo(this.queryState.geographyModel, 'change', function() {
                this.updateFieldText();
            });

            this.listenTo(this.queryState.documentSelectionModel, 'change', function() {
                this.updateFieldText();
            });

            this.listenTo(this.queryState.minScoreModel, 'change', function() {
                this.set('minScore', this.queryState.minScoreModel.get('minScore'));
            });

            this.listenTo(this.queryState.selectedIndexes, 'update reset', _.bind(function() {
                this.set('indexes', collectionBuildIndexes(this.queryState.selectedIndexes));
            }, this));

            this.listenTo(this.queryState.selectedParametricValues,
                'add remove reset change',
                _.debounce(_.bind(function() {
                    this.updateFieldText();
                }, this), DEBOUNCE_WAIT_MILLISECONDS));

            this.set(_.extend({
                queryText: makeQueryText(this.queryState),
                minScore: this.queryState.minScoreModel.get('minScore'),
                indexes: collectionBuildIndexes(this.queryState.selectedIndexes)
            }, this.queryState.datesFilterModel.toQueryModelAttributes()));
            this.updateFieldText();
        },

        /**
         * Update fieldText and fieldTextWithotDocumentSelection attributes from current queryState.
         */
        updateFieldText: function () {
            this.set({
                fieldText: searchDataUtil.buildMergedFieldText(
                    this.queryState.selectedParametricValues,
                    this.queryState.geographyModel,
                    this.queryState.documentSelectionModel),
                fieldTextWithoutDocumentSelection:
                    searchDataUtil.buildMergedFieldTextWithoutDocumentSelection(
                        this.queryState.selectedParametricValues,
                        this.queryState.geographyModel)
            });
        },

        getIsoDate: function(type) {
            const date = this.get(type);

            return date
                ? date.toISOString()
                : null;
        }
    }, {
        Sort: Sort
    });
});
