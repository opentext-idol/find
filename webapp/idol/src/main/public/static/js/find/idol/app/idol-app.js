/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'find/app/app',
    'find/app/util/logout',
    'find/app/configuration',
    'find/idol/app/model/idol-indexes-collection',
    'find/idol/app/model/saved-searches/saved-snapshot-collection',
    'find/idol/app/idol-navigation',
    'find/idol/app/page/idol-find-search',
    'find/idol/app/page/find-about-page',
    'find/idol/app/page/dashboard-page',
    'find/app/page/find-settings-page',
    'i18n!find/nls/bundle'
], function(_, Backbone, BaseApp, logout, configuration, IndexesCollection, SavedSnapshotCollection,
            Navigation, FindSearch, AboutPage, DashboardPage, SettingsPage, i18n) {
    'use strict';

    return BaseApp.extend({
        Navigation: Navigation,
        IndexesCollection: IndexesCollection,

        getModelData: function() {
            let modelData = BaseApp.prototype.getModelData.call(this);

            if(configuration().hasBiRole) {
                modelData = _.extend({
                    savedSnapshotCollection: {
                        Constructor: SavedSnapshotCollection,
                        fetchOptions: {remove: false, reset: false}
                    },
                    readOnlySearchCollection: {
                        Constructor: Backbone.Collection,
                        fetchOptions: {},
                        fetch: false
                    }
                }, modelData);
            }

            return modelData;
        },

        getPageData: function() {
            const config = configuration();

            const pageData = {};
            let dashboards;
            if(config.enableDashboards) {
                dashboards = _.where(config.dashboards, {enabled: true});
                _.extend(pageData, _.reduce(dashboards, function(acc, dash, index) {
                    acc['dashboards/' + encodeURIComponent(dash.dashboardName)] = {
                        Constructor: DashboardPage,
                        icon: 'hp-icon hp-fw hp-dashboard',
                        models: ['sidebarModel'],
                        title: i18n[dash.dashboardName] || dash.dashboardName,
                        order: index,
                        constructorArguments: dash,
                        navigation: 'dashboards'
                    };

                    return acc;
                }, {}));
            }

            const dashboardCount = dashboards ? dashboards.length : 0;

            _.extend(pageData, {
                search: {
                    Constructor: FindSearch,
                    icon: 'hp-icon hp-fw hp-search',
                    models: [
                        'indexesCollection',
                        'savedQueryCollection',
                        'windowScrollModel'
                    ].concat(
                        config.hasBiRole
                            ? ['savedSnapshotCollection', 'readOnlySearchCollection']
                            : []
                    ),
                    title: i18n['app.search'],
                    order: dashboardCount,
                    navigation: config.enableSideBar ? 'sidebar' : 'dropdown'
                },
                about: {
                    Constructor: AboutPage,
                    icon: 'hp-icon hp-fw hp-info',
                    title: i18n['app.about'],
                    order: dashboardCount + 1,
                    navigation: 'dropdown'
                }
            });

            if(_.contains(config.roles, 'ROLE_ADMIN')) {
                pageData.settings = {
                    Constructor: SettingsPage,
                    icon: 'hp-icon hp-fw hp-settings',
                    title: i18n['app.settings'],
                    order: dashboardCount + 2,
                    navigation: 'dropdown'
                };
            }

            return pageData;
        },

        ajaxErrorHandler: function(event, xhr) {
            if(xhr.status === 401) {
                logout('../logout');
            } else if(xhr.status === 403) {
                // refresh the page - the filters should then redirect to the login screen
                window.location.reload();
            }
        }
    });
});
