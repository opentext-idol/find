/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/service-view',
    'find/idol/app/page/search/filters/indexes/idol-indexes-view',
    'find/idol/app/page/search/results/idol-results-view',
    'find/idol/app/page/search/results/idol-results-view-augmentation'
], function(ServiceView, IndexesView, ResultsView, ResultsViewAugmentation) {
    'use strict';

    return ServiceView.extend({
        ResultsView: ResultsView,
        ResultsViewAugmentation: ResultsViewAugmentation,
        IndexesView: IndexesView
    });
});
