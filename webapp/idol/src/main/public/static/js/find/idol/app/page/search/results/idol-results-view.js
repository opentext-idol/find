/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
