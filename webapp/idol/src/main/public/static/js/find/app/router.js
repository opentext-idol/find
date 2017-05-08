/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/app/router-constructor',
    'find/app/configuration'
], function (_, RouterConstructor, configuration) {

    const Router = RouterConstructor.extend({
        routes: function () {
            const routes = _.extend({
                'search/document/:database/:reference': 'documentDetail',
                'search/suggest/:database/:reference': 'suggest'
            }, RouterConstructor.prototype.routes);

            if (configuration().enableSavedSearch) {
                routes['search/tab/:id(/view/:view)'] = 'savedSearch';
            }

            if (configuration().enableDashboards) {
                routes['dashboards/:dashboardName'] = 'dashboards';
            }

            return routes;
        }
    });

    return new Router();
});
