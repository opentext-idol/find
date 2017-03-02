/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/router-constructor'
], function(_, RouterConstructor) {

    const Router = RouterConstructor.extend({
        routes: _.extend({
            'dashboards/:dashboardName': 'dashboards',
            'search/tab/:id(/view/:view)': 'savedSearch',
            'search/document/:database/:reference': 'documentDetail',
            'search/suggest/:database/:reference': 'suggest'
        }, RouterConstructor.prototype.routes)
    });

    return new Router();
});
