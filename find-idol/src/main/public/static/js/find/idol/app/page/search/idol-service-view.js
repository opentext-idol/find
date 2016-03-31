/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/service-view',
    'find/idol/app/page/search/results/idol-results-view-augmentation',
    'find/idol/app/page/search/results/idol-results-view',
    'find/idol/app/page/search/idol-query-left-side-view'
], function(ServiceView, ResultsViewAugmentation, ResultsView, IdolQueryLeftSideView) {

    'use strict';

    return ServiceView.extend({
        ResultsViewAugmentation: ResultsViewAugmentation,
        ResultsView: ResultsView,
        QueryLeftSideView: IdolQueryLeftSideView
    });

});
