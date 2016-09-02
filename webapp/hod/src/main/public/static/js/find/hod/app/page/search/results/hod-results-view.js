/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'find/app/page/search/results/results-view'
], function(ResultsView) {
    'use strict';

    return ResultsView.extend({
        generateSuggestRoute: function(resultNode) {
            var domain = encodeURIComponent(resultNode.attr('data-domain'));
            var index = encodeURIComponent(resultNode.attr('data-index'));
            var reference = encodeURIComponent(resultNode.attr('data-reference'));
            return 'find/search/suggest/' + domain + '/' + index + '/' + reference;
        }
    });
});
