/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/suggest/suggest-view',
    'find/hod/app/page/search/results/hod-results-view'
], function(SuggestView, ResultsView) {

    return SuggestView.extend({
        ResultsView: ResultsView
    });

});
