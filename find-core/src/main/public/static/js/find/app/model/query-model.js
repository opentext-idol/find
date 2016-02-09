/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    /**
     * @readonly
     * @enum {String}
     */
    var Sort = {
        date: 'date',
        relevance: 'relevance'
    };

    var DEBOUNCE_WAIT_MILLISECONDS = 500;

    function buildIndexes(selectedIndexesCollection) {
        return selectedIndexesCollection.map(function(model) {
            return model.get('domain') ? encodeURIComponent(model.get('domain')) + ':' + encodeURIComponent(model.get('name')) : encodeURIComponent(model.get('name'));
        });
    }

    return Backbone.Model.extend({
        defaults: {
            autoCorrect: true,
            queryText: '',
            indexes: [],
            fieldText: null,
            minDate: undefined,
            maxDate: undefined,
            sort: Sort.relevance
        },

        /**
         * @param {Object} attributes
         * @param {{queryState: QueryState}} options
         */
        initialize: function(attributes, options) {
            this.queryState = options.queryState;

            this.listenTo(this.queryState.queryTextModel, 'change', function() {
                this.set('queryText', this.queryState.queryTextModel.makeQueryText());
            });

            this.listenTo(this.queryState.datesFilterModel, 'change', function() {
                this.set(this.queryState.datesFilterModel.toQueryModelAttributes());
            });

            this.listenTo(this.queryState.selectedIndexes, 'update reset', _.debounce(_.bind(function() {
                this.set('indexes', buildIndexes(this.queryState.selectedIndexes));
            }, this), DEBOUNCE_WAIT_MILLISECONDS));

            this.listenTo(this.queryState.selectedParametricValues, 'add remove reset', _.debounce(_.bind(function() {
                var fieldTextNode = this.queryState.selectedParametricValues.toFieldTextNode();
                this.set('fieldText', fieldTextNode ? fieldTextNode : null);
            }, this)));

            var fieldTextNode = this.queryState.selectedParametricValues.toFieldTextNode();

            this.set(_.extend({
                queryText: this.queryState.queryTextModel.makeQueryText(),
                indexes: buildIndexes(this.queryState.selectedIndexes),
                fieldText: fieldTextNode ? fieldTextNode : null
            }, this.queryState.datesFilterModel.toQueryModelAttributes()));
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
