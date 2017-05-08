/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/app',
    'find/app/page/default/default-page',
    'find/hod/app/model/hod-indexes-collection',
    'find/hod/app/page/hod-find-search'
], function(BaseApp, DefaultPage, IndexesCollection, FindSearch) {
    'use strict';

    return BaseApp.extend({
        defaultPage: 'default',
        IndexesCollection: IndexesCollection,

        getPageData: function() {
            return {
                search: {
                    Constructor: FindSearch,
                    models: ['savedQueryCollection', 'indexesCollection', 'windowScrollModel']
                },
                'default': {
                    Constructor: DefaultPage
                }
            };
        },

        ajaxErrorHandler: function(event, xhr) {
            if(xhr.status === 401) {
                location.assign('sso');
            } else if(xhr.status === 403) {
                window.location.reload();
            }
        }
    });
});
