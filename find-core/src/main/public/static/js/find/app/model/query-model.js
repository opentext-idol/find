/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    var Sort = {
        date: 'date',
        relevance: 'relevance'
    };

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
