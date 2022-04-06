/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'backbone'
], function (_, Backbone) {
    'use strict';

    /**
     * @typedef {Object} ParametricPaginatorOptions
     * @property {Object} fetchRestrictions Parameters for the search, used to determine counts for matching values
     * @property {Function} fetchFunction Fetch parametric values for some parameters, returning a promise resolved with
     * values and totalValues
     * @property {String} fieldName The parametric field name
     * @property {Backbone.Collection} selectedValues The selected values collection, kept up to date by this object, but
     * changes made in the collection will not be reflected here
     * @property {Array<*>} allIndexes All indexes which should be queried for unrestricted parametric values
     * @property {number} [pageSize=20] How many values to fetch in one request
     */
    /**
     * Pages over parametric values for the given field. A new page is loaded when fetchNext is called, unless a request
     * is already in flight or there are no more values to fetch.
     *
     * Values are ordered by count of documents who match the given fetch restrictions. When there are no more values
     * matching the restrictions, zero-count values are fetched.
     *
     * @param {ParametricPaginatorOptions} options
     * @constructor
     */
    const ParametricPaginator = function (options) {
        this.fieldDisplayName = options.fieldDisplayName;
        this.selectedValues = options.selectedValues;
        this.fetchFunction = options.fetchFunction;

        this.fetchOptions = {
            fetchRestrictions: options.fetchRestrictions,
            fieldName: options.fieldName,
            allIndexes: options.allIndexes,
            pageSize: options.pageSize || 20
        };

        this.stateModel = new Backbone.Model({
            loading: true,
            error: null,
            empty: false
        });

        this.valuesCollection = new Backbone.Collection();

        this.fetching = false;
        this.error = null;

        this.paginationState = {
            duplicatesFound: 0,
            nextRestrictedPage: 1,
            nextPage: 1,
            totalRestrictedValues: null,
            totalValues: null
        };
    };

    _.extend(ParametricPaginator.prototype, {
        /**
         * Fetch the next page of values if there are more values and we are not already fetching.
         *
         * If no more values match the fetch restrictions, zero-count values are loaded. As many requests are made as
         * are required to fetch the page size. If more than the page size are found, they all are added (so page size
         * is a minimum). If no more zero-count values are available in the field, fetching stops.
         */
        fetchNext: function () {
            if (!this.fetching && !this.error) {
                loadPage.call(this, this.fetchOptions.pageSize);
            }
        },

        /**
         * Set the selected property of the given value, updating the selected parametric values
         * collection and the values collection.
         */
        setSelected: function (value, isSelected) {
            const model = this.valuesCollection.findWhere({value: value});
            model.set('selected', isSelected);

            const currentValue = this.selectedValues.findWhere({
                field: this.fetchOptions.fieldName,
                value: value
            });
            if (isSelected && !currentValue) {
                this.selectedValues.add({
                    field: this.fetchOptions.fieldName,
                    displayName: this.fieldDisplayName,
                    value: value,
                    displayValue: model.get('displayValue'),
                    type: 'Parametric'
                });
            } else if (!isSelected && currentValue) {
                this.selectedValues.remove(currentValue);
            }
        },

        /**
         * Toggle the selected property of the given value, updating the selected parametric values collection and the
         * values collection.
         * @param value
         */
        toggleSelection: function (value) {
            const model = this.valuesCollection.findWhere({value: value});
            this.setSelected(value, !model.get('selected'));
        }
    });

    /*
     * Loads at least the given number of values, one page of size pageSize at a time, but only if we know there are
     * values to fetch. Called recursively if there are not enough questions after the first page.
     */
    function loadPage(valuesRequired) {
        const restrictedStart = 1 + this.fetchOptions.pageSize * (this.paginationState.nextRestrictedPage - 1);

        let nextFetch;

        if (this.paginationState.totalRestrictedValues === null || restrictedStart <= this.paginationState.totalRestrictedValues) {
            // Fetch restricted values because we either have not fetched them before or we know there are more to fetch
            nextFetch = {
                totalKey: 'totalRestrictedValues',
                useCount: true,
                parameters: _.extend({
                    fieldNames: [this.fetchOptions.fieldName],
                    start: restrictedStart,
                    maxValues: this.fetchOptions.pageSize * this.paginationState.nextRestrictedPage
                }, this.fetchOptions.fetchRestrictions)
            };

            this.paginationState = _.defaults({nextRestrictedPage: this.paginationState.nextRestrictedPage + 1}, this.paginationState);
        } else {
            const totalValues = this.paginationState.totalValues;
            const unrestrictedStart = 1 + this.fetchOptions.pageSize * (this.paginationState.nextPage - 1);

            // Used to check if all remaining values were already fetched in the restricted phase
            const duplicatesNotFound = this.paginationState.totalRestrictedValues - this.paginationState.duplicatesFound;

            if (
                totalValues === null ||
                (unrestrictedStart <= totalValues && duplicatesNotFound < totalValues - unrestrictedStart + 1)
            ) {
                // Fetch unrestricted values because we either have not fetched them before or we know there are more to fetch
                nextFetch = {
                    totalKey: 'totalValues',
                    useCount: false,
                    parameters: {
                        databases: this.fetchOptions.allIndexes,
                        fieldNames: [this.fetchOptions.fieldName],
                        start: unrestrictedStart,
                        maxValues: this.fetchOptions.pageSize * this.paginationState.nextPage
                    }
                };

                this.paginationState = _.defaults({nextPage: this.paginationState.nextPage + 1}, this.paginationState);
            } else {
                // We have exhausted restricted and unrestricted values
                nextFetch = null;
            }
        }

        if (nextFetch) {
            this.fetchFunction(nextFetch.parameters)
                .done(function(output) {
                    const newValueData = output.values
                        .filter(function(data) {
                            return !this.valuesCollection.some(function(model) {
                                return model.get('value') === data.value;
                            });
                        }.bind(this))
                        .map(function(data) {
                            return {
                                count: nextFetch.useCount ? data.count : 0,
                                value: data.value,
                                displayValue: data.displayValue,
                                selected: Boolean(this.selectedValues.findWhere({
                                    field: this.fetchOptions.fieldName,
                                    value: data.value
                                }))
                            };
                        }.bind(this));

                    this.valuesCollection.add(newValueData);

                    this.paginationState[nextFetch.totalKey] = output.totalValues;
                    this.paginationState.duplicatesFound += output.values.length - newValueData.length;

                    if (newValueData.length >= valuesRequired) {
                        this.fetching = false;
                        this.stateModel.set(determineStateAttributes.call(this));
                    } else {
                        this.fetching = true;
                        loadPage.call(this, valuesRequired - output.values.length);
                    }
                }.bind(this))
                .fail(function (error) {
                    this.error = error;
                    this.fetching = false;
                    this.stateModel.set(determineStateAttributes.call(this));
                }.bind(this));

            this.fetching = true;
        } else {
            this.fetching = false;
        }

        this.stateModel.set(determineStateAttributes.call(this));
    }

    function determineStateAttributes() {
        return {
            empty: !this.error && this.totalValues !== null && !this.fetching && this.valuesCollection.isEmpty(),
            error: this.error,
            loading: this.fetching
        };
    }

    return ParametricPaginator;

});
