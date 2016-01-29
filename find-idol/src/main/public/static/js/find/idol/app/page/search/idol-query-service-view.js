/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/query-service-view',
    'find/app/model/search-filters-collection',
    'find/idol/app/page/search/filters/indexes/idol-indexes-view',
    'find/idol/app/page/search/results/idol-results-view'
], function (ServiceView, SearchFiltersCollection, IndexesView, ResultsView) {
    'use strict';

    return ServiceView.extend({
        IndexesView: IndexesView,
        ResultsView: ResultsView,
        SearchFiltersCollection: SearchFiltersCollection
    });

});
