/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/util/search-data-util',
    'find/app/model/comparisons/comparison-documents-collection',
    'find/app/model/saved-searches/saved-search-model'
], function(Backbone, searchDataUtil, ComparisonDocumentsCollection, SavedSearchModel) {

    var getComparisonAttributesFromSavedSearch = function(savedSearchModel, prefix) {
        var data = {};

        data[prefix + 'Text'] = searchDataUtil.makeQueryText(savedSearchModel.get('queryText'), savedSearchModel.get('relatedConcepts'));

        if(savedSearchModel.get('type') === SavedSearchModel.Type.SNAPSHOT) {
            data[prefix + 'QueryStateToken'] = _.first(savedSearchModel.get('stateTokens'));
        } else {
            data[prefix + 'Restrictions'] = searchDataUtil.buildQuery(savedSearchModel);
        }

        return data;
    };

    var ComparisonModel = Backbone.Model.extend({
        url: '../api/public/comparison/compare',

        parse: function(response) {
            return {
                documentsInBoth: new ComparisonDocumentsCollection([], {
                    text: '(' + this.get('firstText') + ') OR (' + this.get('secondText') + ')',
                    stateMatchIds: _.compact([response.firstQueryStateToken, response.secondQueryStateToken]),
                    stateDontMatchIds: _.compact([response.documentsOnlyInFirstStateToken, response.documentsOnlyInSecondStateToken])
                }),

                documentsOnlyInFirst: new ComparisonDocumentsCollection([], {
                    text: this.get('firstText'),
                    stateMatchIds: _.compact([response.firstQueryStateToken]),
                    stateDontMatchIds: _.compact([response.secondQueryStateToken])
                }),

                documentsOnlyInSecond: new ComparisonDocumentsCollection([], {
                    text: this.get('secondText'),
                    stateMatchIds: _.compact([response.secondQueryStateToken]),
                    stateDontMatchIds: _.compact([response.firstQueryStateToken])
                })
            };
        }
    }, {
        fromModels: function(primaryModel, secondaryModel) {
            var comparisonModelAttributes = {};

            _.extend(comparisonModelAttributes, getComparisonAttributesFromSavedSearch(primaryModel, 'first'));
            _.extend(comparisonModelAttributes, getComparisonAttributesFromSavedSearch(secondaryModel, 'second'));

            return new ComparisonModel(comparisonModelAttributes);
        }
    });

    return ComparisonModel;

});
