/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/util/search-data-util',
    'find/app/model/comparisons/comparison-documents-collection'
], function(Backbone, searchDataUtil, ComparisonDocumentsCollection) {


    return Backbone.Model.extend({
        url: '../api/public/comparison/compare',

        parse: function(response) {
            return {
                documentsInBoth: new ComparisonDocumentsCollection([], {
                    stateMatchIds: _.compact([response.firstQueryStateToken, response.secondQueryStateToken]),
                    stateDontMatchIds: _.compact([response.documentsOnlyInFirstStateToken, response.documentsOnlyInSecondStateToken])
                }),

                documentsOnlyInFirst: new ComparisonDocumentsCollection([], {
                    stateMatchIds: _.compact([response.firstQueryStateToken]),
                    stateDontMatchIds: _.compact([response.secondQueryStateToken])
                }),

                documentsOnlyInSecond: new ComparisonDocumentsCollection([], {
                    stateMatchIds: _.compact([response.secondQueryStateToken]),
                    stateDontMatchIds: _.compact([response.firstQueryStateToken])
                })
            };
        }
    }, {
        fromModels: function(primaryModel, secondaryModel) {
            var primaryStateMatchId = primaryModel.get('stateMatchId');
            var secondaryStateMatchId = secondaryModel.get('stateMatchId');

            var comparisonModelArguments = {};

            primaryStateMatchId ? comparisonModelArguments.firstQueryStateToken = primaryStateMatchId : comparisonModelArguments.firstRestrictions = searchDataUtil.buildQuery(primaryModel);
            secondaryStateMatchId ? comparisonModelArguments.secondQueryStateToken = secondaryStateMatchId : comparisonModelArguments.secondRestrictions = searchDataUtil.buildQuery(secondaryModel);

            return new this(comparisonModelArguments);
        }
    });

});
