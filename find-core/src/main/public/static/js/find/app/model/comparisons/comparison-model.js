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

    var convertSearchModelToComparisonModel = function(model, prefix) {
        var data = {};

        data[prefix + 'Text'] = searchDataUtil.makeQueryText(model.get('queryText'), model.get('relatedConcepts'));

        if(model.get('type') === SavedSearchModel.Type.SNAPSHOT) {
            data[prefix + 'QueryStateToken'] = model.get('stateTokens')[0];
        } else {
            data[prefix + 'Restrictions'] = searchDataUtil.buildQuery(model);
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
            var comparisonModelArguments = {};

            _.extend(comparisonModelArguments, convertSearchModelToComparisonModel(primaryModel, 'first'));
            _.extend(comparisonModelArguments, convertSearchModelToComparisonModel(secondaryModel, 'second'));

            return new ComparisonModel(comparisonModelArguments);
        }
    });

    return ComparisonModel;

});
