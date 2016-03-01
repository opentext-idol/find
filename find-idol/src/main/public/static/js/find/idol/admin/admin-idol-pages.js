/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/idol/public/idol-pages',
    'find/app/page/find-settings-page',
    'i18n!find/nls/bundle'
], function(IdolPages, SettingsPage, i18n) {

    'use strict';

    return IdolPages.extend({
        initializePages: function() {
            IdolPages.prototype.initializePages.apply(this, arguments);

            this.pages.push({
                constructor: SettingsPage,
                icon: 'hp-icon hp-fw hp-settings',
                pageName: 'settings',
                title: i18n['app.settings']
            });
        }
    });

});
