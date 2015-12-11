/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/public/pages',
    'find/hod/app/page/hod-find-search'
], function(Pages, FindSearch) {
    'use strict';

    return Pages.extend({
        initializePages: function() {
            this.pages = [
                {
                    constructor: FindSearch
                    , pageName: 'search'
                }
            ];
        }
    });
});
