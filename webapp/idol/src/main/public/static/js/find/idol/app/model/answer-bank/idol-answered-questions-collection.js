/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/model/find-base-collection'
], function(_, FindBaseCollection) {
    'use strict';

    return FindBaseCollection.extend({
        url: 'api/public/answer/ask',

        parse: function(response) {
            return response.map(function(response) {
                return _.extend({
                    question: response.interpretation,
                    answer: response.text,
                    systemName: response.systemName,
                    source: response.source
                }, response);
            });
        }
    });
});
