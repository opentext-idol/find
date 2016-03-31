/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/service-view',
    'find/hod/app/page/search/results/hod-results-view-augmentation',
    'find/hod/app/page/search/results/hod-results-view',
    'find/hod/app/page/search/hod-query-left-side-view',
    'find/hod/app/page/search/hod-query-middle-column-header-view'
], function(ServiceView, ResultsViewAugmentation, ResultsView, HodQueryLeftSideView, HodQueryMiddleColumnHeaderView) {

    'use strict';

    return ServiceView.extend({
        QueryMiddleColumnHeaderView: HodQueryMiddleColumnHeaderView,
        QueryLeftSideView: HodQueryLeftSideView,
        ResultsView: ResultsView,
        ResultsViewAugmentation: ResultsViewAugmentation
    });

});
