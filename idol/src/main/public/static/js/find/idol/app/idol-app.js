/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/app',
    'underscore',
    'find/app/configuration',
    'find/idol/app/model/idol-indexes-collection',
    'find/idol/app/model/saved-searches/saved-snapshot-collection',
    'find/idol/app/idol-navigation',
    'find/idol/app/page/idol-find-search',
    'find/idol/app/page/find-about-page',
    'find/app/page/find-settings-page',
    'i18n!find/nls/bundle'
], function(BaseApp, _, configuration, IndexesCollection, SavedSnapshotCollection, Navigation, FindSearch, AboutPage, SettingsPage, i18n) {

    'use strict';

    return BaseApp.extend({
        Navigation: Navigation,
        IndexesCollection: IndexesCollection,

        getModelData: function() {
            var modelData = BaseApp.prototype.getModelData.call(this);

            if (configuration().hasBiRole) {
                modelData = _.extend({
                    savedSnapshotCollection: {
                        Constructor: SavedSnapshotCollection,
                        fetchOptions: {remove: false}
                    }
                }, modelData);
            }

            return modelData;
        },

        getPageData: function() {
            var pageData = {
                search: {
                    Constructor: FindSearch,
                    icon: 'hp-icon hp-fw hp-search',
                    models: ['indexesCollection', 'savedQueryCollection', 'windowScrollModel'].concat(configuration().hasBiRole ? ['savedSnapshotCollection'] : []),
                    title: i18n['app.search'],
                    order: 0
                },
                about: {
                    Constructor: AboutPage,
                    icon: 'hp-icon hp-fw hp-info',
                    title: i18n['app.about'],
                    order: 1
                }
            };

            if (_.contains(configuration().roles, 'ROLE_ADMIN')) {
                pageData.settings = {
                    Constructor: SettingsPage,
                    icon: 'hp-icon hp-fw hp-settings',
                    title: i18n['app.settings'],
                    order: 2
                };
            }

            return pageData;
        }
    });

});
