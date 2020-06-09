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
    'underscore',
    'find/app/router-constructor',
    'find/app/configuration'
], function (_, RouterConstructor, configuration) {

    const Router = RouterConstructor.extend({
        routes: function () {
            const routes = _.extend({
                'search/document/:database/:reference': 'documentDetail',
                'search/suggest/:database/:reference(/databases/:suggestDatabase)': 'suggest'
            }, RouterConstructor.prototype.routes);

            if (configuration().enableSavedSearch) {
                routes['search/tab/:id(/view/:view(/*splat))'] = 'savedSearch';
            }

            if (configuration().enableDashboards) {
                routes['dashboards/:dashboardName'] = 'dashboards';
            }

            return routes;
        },

        // Implements abstract method in superclass.
        parseEncodedDatabases: function(optionalEncodedDatabases){
            try {
                return optionalEncodedDatabases.split(',').map(function(str){
                    const parsed = decodeURIComponent(str);

                    return {
                        name: parsed
                    }
                });
            }
            catch(e) {
                // we'll ignore the databases
            }
        }
    });

    return new Router();
});
