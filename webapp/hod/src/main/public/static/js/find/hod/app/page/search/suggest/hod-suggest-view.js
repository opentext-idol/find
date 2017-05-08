/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/page/search/suggest/suggest-view',
    'find/hod/app/page/search/results/hod-results-view',
    'find/hod/app/page/search/results/hod-results-view-augmentation'
], function(_, SuggestView, ResultsView, ResultsViewAugmentation) {
    'use strict';

    return SuggestView.extend({
        ResultsView: ResultsView,
        ResultsViewAugmentation: ResultsViewAugmentation,

        getIndexes: function(indexesCollection, documentModel) {
            const indexModels = indexesCollection.where({
                name: documentModel.get('index'),
                domain: documentModel.get('domain')
            });
            return _.pluck(indexModels, 'id');
        }
    });
});
