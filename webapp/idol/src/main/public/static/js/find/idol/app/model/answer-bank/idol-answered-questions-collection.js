define([
    'find/app/model/find-base-collection'
], function(FindBaseCollection) {
    'use strict';

    return FindBaseCollection.extend({
        url: 'api/public/answer/ask',

        parse: function(response) {
            return response.map(function(response) {
                return _.extend({
                    question: response.interpretation,
                    answer: response.text
                }, response);
            });
        }
    });
});