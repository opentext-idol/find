/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/query-middle-column-header-view',
    'find/hod/app/model/hod-search-filters-collection'
], function(QueryMiddleColumnHeaderView, HodSearchFiltersCollection) {

    return QueryMiddleColumnHeaderView.extend({
        SearchFiltersCollection: HodSearchFiltersCollection
    });

});
