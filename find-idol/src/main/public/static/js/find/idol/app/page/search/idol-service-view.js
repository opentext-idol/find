/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/service-view',
    'find/idol/app/page/search/filters/indexes/idol-indexes-view',
    'find/idol/app/page/search/results/idol-results-view-augmentation',
    'find/idol/app/page/search/results/idol-results-view'
], function(ServiceView, IndexesView, ResultsViewAugmentation, ResultsView) {

    'use strict';

    return ServiceView.extend({
        ResultsViewAugmentation: ResultsViewAugmentation,
        ResultsView: ResultsView,
        IndexesView: IndexesView
    });

});
