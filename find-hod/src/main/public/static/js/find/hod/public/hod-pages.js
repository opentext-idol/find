/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/find-pages',
    'find/hod/app/page/hod-find-search',
    'find/app/page/default/default-page'
], function(FindPages, FindSearch, DefaultPage) {
    'use strict';

    return FindPages.extend({

        initializePages: function() {
            this.pages = [
                {
                    constructor: FindSearch
                    , pageName: 'search'
                },
                {
                    constructor: DefaultPage
                    , defaultPage: true
                    , pageName: 'default'
                }
            ];
        }
    });
});
