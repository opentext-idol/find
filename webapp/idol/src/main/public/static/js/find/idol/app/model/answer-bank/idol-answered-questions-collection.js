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
