/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/model/comparisons/comparison-documents-collection'
], function(Backbone, ComparisonDocumentsCollection) {


    return Backbone.Model.extend({
        url: '../api/public/comparison/compare',

        parse: function(response) {
            return {
                documentsInBoth: new ComparisonDocumentsCollection(response.documentsInBoth, {
                    stateMatchIds: [response.firstQueryStateToken, response.secondQueryStateToken],
                    stateDontMatchIds: [response.documentsOnlyInFirstStateToken, response.documentsOnlyInSecondStateToken]
                }),

                documentsOnlyInFirst: new ComparisonDocumentsCollection(response.documentsOnlyInFirst, {
                    stateMatchIds: [response.firstQueryStateToken],
                    stateDontMatchIds: [response.secondQueryStateToken]
                }),

                documentsOnlyInSecond: new ComparisonDocumentsCollection(response.documentsOnlyInSecond, {
                    stateMatchIds: [response.secondQueryStateToken],
                    stateDontMatchIds: [response.firstQueryStateToken]
                })
            };
        }
    });

});
