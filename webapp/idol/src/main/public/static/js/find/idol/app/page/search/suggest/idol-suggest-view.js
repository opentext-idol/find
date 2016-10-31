/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/suggest/suggest-view',
    'find/idol/app/page/search/results/idol-results-view',
    'find/idol/app/page/search/results/idol-results-view-augmentation'

], function(SuggestView, ResultsView, ResultsViewAugmentation) {

    return SuggestView.extend({
        ResultsView: ResultsView,
        ResultsViewAugmentation: ResultsViewAugmentation,

        getIndexes: function(indexesCollection) {
            return indexesCollection.pluck('id')
        }
    });

});
