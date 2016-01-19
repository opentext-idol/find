/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/results/results-view-container',
    'find/idol/app/page/search/results/idol-results-view'
], function(ResultsViewContainer, ResultsView) {
    'use strict';

    return ResultsViewContainer.extend({

        ResultsView: ResultsView
    });

});
