/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/find-pages',
    'find/idol/app/page/idol-find-search',
    'find/idol/app/page/find-about-page',
    'find/app/page/find-settings-page',
    'i18n!find/nls/bundle'
], function(FindPages, FindSearch, AboutPage, SettingsPage, i18n) {

    'use strict';

    return FindPages.extend({
        initializePages: function() {
            this.pages = [
                {
                    constructor: FindSearch,
                    icon: 'hp-icon hp-fw hp-search',
                    pageName: 'search',
                    title: i18n['app.search']
                }, {
                    constructor: AboutPage,
                    icon: 'hp-icon hp-fw hp-info',
                    pageName: 'about',
                    title: i18n['app.about']
                }
            ];
        }
    });

});
