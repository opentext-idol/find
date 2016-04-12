/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/app',
    'find/app/page/default/default-page',
    'find/hod/app/page/hod-find-search'
], function(BaseApp, DefaultPage, FindSearch) {

    'use strict';

    return BaseApp.extend({
        defaultPage: 'default',

        getPageData: function() {
            return {
                search: {
                    Constructor: FindSearch,
                    models: ['savedQueryCollection', 'indexesCollection']
                },
                'default': {
                    Constructor: DefaultPage
                }
            };
        }
    });

});
