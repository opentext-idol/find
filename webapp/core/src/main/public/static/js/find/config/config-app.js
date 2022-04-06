/*
 * (c) Copyright 2014-2017 Micro Focus or one of its affiliates.
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
    'jquery',
    'find/app/page/find-settings-page',
    'js-whatever/js/empty-navbar',
    'find/app/util/test-browser',
    'i18n!find/nls/bundle',
    'text!find/templates/config/config.html',
    'text!find/templates/config/empty-navbar.html'
], function(_, $, SettingsPage, EmptyNavbar, testBrowser, i18n, template, emptyNavbar) {
    'use strict';

    return function() {
        $.ajaxSetup({cache: false});

        const $page = $('.page');

        $page.html(_.template(template));

        this.settingsPage = new SettingsPage({});
        this.settingsPage.render();

        this.navigation = new (EmptyNavbar.extend({
            template: _.template(emptyNavbar),

            events: {
                'click a': function(e) {
                    if(!this.options.settingsPage.hasSavedSettings) {
                        e.preventDefault();

                        alert('You should save your settings before you can log out.');
                    }
                }
            }
        }))({
            strings: {
                appName: i18n['app.name'],
                logout: i18n['settings.logoutFromSettings']
            },
            logoutUri: 'logout',
            settingsPage: this.settingsPage,
            showLogout: true
        });

        this.navigation.render();
        $('.header').append(this.navigation.el);
        this.settingsPage.show();
        $('.content').append(this.settingsPage.el);

        testBrowser();
    };
});
