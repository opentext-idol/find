/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/results/results-view'
], function(ResultsView) {
    'use strict';

    return ResultsView.extend({
        getQuestionsViewConstructor: function() {
        },

        render: function(){
            ResultsView.prototype.render.apply(this, arguments);
            this.sortView.$el.addClass('hide');
            this.resultsNumberView.$el.addClass('hide');
            this.intentBasedRankingView && this.intentBasedRankingView.$el.addClass('hide');
        },

        generateSuggestRoute: function(resultNode) {
            let database = encodeURIComponent(resultNode.attr('data-index'));
            let reference = encodeURIComponent(resultNode.attr('data-reference'));
            return 'search/suggest/' + database + '/' + reference;
        }
    });
});
