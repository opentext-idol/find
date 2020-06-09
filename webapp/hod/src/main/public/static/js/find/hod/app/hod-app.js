/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
