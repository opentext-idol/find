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
    'backbone',
    'find/app/app',
    'find/app/util/logout',
    'find/app/configuration',
    'find/idol/app/model/idol-indexes-collection',
    'find/idol/app/model/saved-searches/saved-snapshot-collection',
    'find/idol/app/model/saved-searches/shared-saved-snapshot-collection',
    'find/app/model/asset-collection',
    'find/idol/app/idol-navigation',
    'find/idol/app/page/idol-find-search',
    'find/idol/app/page/find-about-page',
    'find/idol/app/page/dashboard-page',
    'find/app/page/find-settings-page',
    'find/app/page/customizations-page',
    'i18n!find/nls/bundle'
], function(_, Backbone, BaseApp, logout, configuration, IndexesCollection, SavedSnapshotCollection,
            SharedSavedSnapshotCollection, AssetCollection, Navigation, FindSearch, AboutPage, DashboardPage,
            SettingsPage, CustomizationsPage, i18n) {
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
                    },
                    sharedSavedSnapshotCollection: {
                        Constructor: SharedSavedSnapshotCollection,
                        fetchOptions: {remove: false, reset: false}
                    }
                }, modelData);
            }

            modelData = _.extend({
                bigLogoCollection: {
                    Constructor: AssetCollection,
                    options: {
                        type: CustomizationsPage.AssetTypes.bigLogo.type
                    }
                },
                smallLogoCollection: {
                    Constructor: AssetCollection,
                    options: {
                        type: CustomizationsPage.AssetTypes.smallLogo.type
                    }
                }
            }, modelData);

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
                        models: ['sidebarModel', 'savedQueryCollection'],
                        title: i18n[dash.dashboardName] || dash.dashboardName,
                        order: index,
                        constructorArguments: dash,
                        navigation: 'dashboards'
                    };

                    return acc;
                }, {}));
            }

            const dashboardCount = dashboards
                ? dashboards.length
                : 0;

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
                            ? [
                                'savedSnapshotCollection',
                                'sharedSavedQueryCollection',
                                'sharedSavedSnapshotCollection',
                                'readOnlySearchCollection'
                            ]
                            : []
                    ),
                    title: i18n['app.search'],
                    order: dashboardCount,
                    navigation: config.enableSideBar
                        ? 'sidebar'
                        : 'dropdown'
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
                _.extend(pageData, {
                    settings: {
                        Constructor: SettingsPage,
                        icon: 'hp-icon hp-fw hp-settings',
                        navigation: 'dropdown',
                        title: i18n['app.settings'],
                        order: dashboardCount + 2
                    },
                    customizations: {
                        Constructor: CustomizationsPage,
                        icon: 'hp-icon hp-fw hp-view',
                        models: ['bigLogoCollection', 'smallLogoCollection'],
                        navigation: 'dropdown',
                        title: i18n['app.customizations'],
                        order: dashboardCount + 3
                    }
                });
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
