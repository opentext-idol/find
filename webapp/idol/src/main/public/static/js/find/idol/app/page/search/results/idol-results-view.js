/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/results/results-view',
    'find/app/configuration',
    'find/idol/app/page/search/results/idol-questions-view'
], function(ResultsView, configuration, QuestionsView) {
    'use strict';

    return ResultsView.extend({
        getQuestionsViewConstructor: function() {
            if(configuration().answerServerEnabled === true) {
                return QuestionsView;
            }
        },

        generateSuggestRoute: function(resultNode) {
            let database = encodeURIComponent(resultNode.attr('data-index'));
            let reference = encodeURIComponent(resultNode.attr('data-reference'));
            return 'search/suggest/' + database + '/' + reference;
        }
    });
});
