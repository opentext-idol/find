/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'jquery'
], function (_, Backbone, $) {
    'use strict';

    const ParametricPaginator = function (options) {
        this.fetchData = options.fetchData;
        this.fetchFunction = options.fetchFunction;
        this.fieldName = options.fieldName;
        this.selectedValues = options.selectedValues;
        this.pageSize = options.pageSize || 20;

        this.stateModel = new Backbone.Model({
            loading: false,
            error: null,
            empty: false
        });

        this.valuesCollection = new Backbone.Collection();

        // Internal state
        this.fetching = false;
        this.nextPage = 1;
        this.error = null;
        this.totalValues = null;
    };

    _.extend(ParametricPaginator.prototype, {
        fetchNext: function () {
            const start = 1 + this.pageSize * (this.nextPage - 1);

            if (!this.fetching && !this.error && (this.totalValues === null || start <= this.totalValues)) {
                this.fetchFunction(_.extend({
                        fieldNames: [this.fieldName],
                        start: start,
                        maxValues: this.pageSize * this.nextPage
                    }, this.fetchData))
                    .done(function(output) {
                        this.totalValues = output.totalValues;

                        this.valuesCollection.add(output.values.map(function(data) {
                            return {
                                count: data.count,
                                value: data.value,
                                selected: Boolean(this.selectedValues.findWhere({field: this.fieldName, value: data.value}))
                            };
                        }.bind(this)));
                    }.bind(this))
                    .fail(function(error) {
                        this.error = error;
                    }.bind(this))
                    .always(function() {
                        this.fetching = false;
                        this.stateModel.set(determineStateAttributes.call(this));
                    }.bind(this));

                this.nextPage++;
                this.fetching = true;
                this.stateModel.set(determineStateAttributes.call(this));
            }
        },

        toggleSelection: function(value) {
            const model = this.valuesCollection.findWhere({value: value});
            const isSelected = !model.get('selected');
            model.set('selected', isSelected);

            if (isSelected) {
                this.selectedValues.add({field: this.fieldName, value: value});
            } else {
                this.selectedValues.remove(this.selectedValues.where({field: this.fieldName, value: value}));
            }
        }
    });

    function determineStateAttributes() {
        return {
            empty: this.totalValues !== null && !this.fetching && this.valuesCollection.isEmpty(),
            error: this.error,
            loading: this.fetching
        };
    }

    return ParametricPaginator;

});
