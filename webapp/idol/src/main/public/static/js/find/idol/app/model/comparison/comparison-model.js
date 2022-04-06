/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'backbone',
    'find/app/util/search-data-util',
    'find/app/model/saved-searches/saved-search-model',
    'underscore'
], function(Backbone, searchDataUtil, SavedSearchModel, _) {
    'use strict';

    var getComparisonAttributesFromSavedSearch = function(savedSearchModel, prefix) {
        var data = {};

        data[prefix + 'Text'] = searchDataUtil.makeQueryText(savedSearchModel.get('relatedConcepts'));

        if(savedSearchModel.get('type') === SavedSearchModel.Type.SNAPSHOT) {
            data[prefix + 'QueryStateToken'] = _.first(savedSearchModel.get('queryStateTokens'));
        } else {
            data[prefix + 'Restrictions'] = searchDataUtil.buildQuery(savedSearchModel);
        }

        return data;
    };

    var ComparisonModel = Backbone.Model.extend({
        url: 'api/bi/comparison/compare',

        parse: function(response) {
            return {
                bothText: '(' + this.get('firstText') + ') OR (' + this.get('secondText') + ')',

                inBoth: {
                    stateMatchIds: _.compact([response.firstQueryStateToken, response.secondQueryStateToken]),
                    stateDontMatchIds: _.compact([response.documentsOnlyInFirstStateToken, response.documentsOnlyInSecondStateToken])
                },

                onlyInFirst: {
                    stateMatchIds: _.compact([response.firstQueryStateToken]),
                    stateDontMatchIds: _.compact([response.secondQueryStateToken])
                },

                onlyInSecond: {
                    stateMatchIds: _.compact([response.secondQueryStateToken]),
                    stateDontMatchIds: _.compact([response.firstQueryStateToken])
                }
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
