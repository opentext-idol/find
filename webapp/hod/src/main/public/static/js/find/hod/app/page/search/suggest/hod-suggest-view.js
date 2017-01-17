/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/suggest/suggest-view',
    'find/hod/app/page/search/results/hod-results-view',
    'find/hod/app/page/search/results/hod-results-view-augmentation',
    'underscore'
], function(SuggestView, ResultsView, ResultsViewAugmentation, _) {
    'use strict';

    return SuggestView.extend({
        ResultsView: ResultsView,
        ResultsViewAugmentation: ResultsViewAugmentation,

        getIndexes: function(indexesCollection, documentModel) {
            var indexModels = indexesCollection.where({
                name: documentModel.get('index'),
                domain: documentModel.get('domain')
            });
            return _.pluck(indexModels, 'id');
        }
    });
});
