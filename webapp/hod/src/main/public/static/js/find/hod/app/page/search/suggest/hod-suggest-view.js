/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
