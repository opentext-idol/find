/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/service-view',
    'find/idol/app/page/indexes/idol-indexes-view',
    'find/idol/app/page/results/idol-results-view'
], function(ServiceView, IndexesView, ResultsView) {
    'use strict';

    return ServiceView.extend({
        constructIndexesView: function (queryModel, indexesCollection, selectedIndexesCollection) {
            return new IndexesView({
                queryModel: queryModel,
                indexesCollection: indexesCollection,
                selectedDatabasesCollection: selectedIndexesCollection
            });
        },

        constructResultsView: function (entityCollection, indexesCollection, queryModel) {
            return new ResultsView({
                entityCollection: entityCollection,
                indexesCollection: indexesCollection,
                queryModel: queryModel
            });
        }
    });

});
