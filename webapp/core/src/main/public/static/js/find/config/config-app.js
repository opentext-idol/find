/*
 * Copyright 2014-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const $ = require('jquery');
const SettingsPage = require('find/app/page/find-settings-page');
const EmptyNavbar = require('js-whatever/js/empty-navbar');
const testBrowser = require('find/app/util/test-browser');
const i18n = require('find/nls/bundle');
const template = require('find/templates/config/config.html');
const emptyNavbar = require('find/templates/config/empty-navbar.html');

module.exports = function() {
        $.ajaxSetup({cache: false});

        const $page = $('.page');

        $page.html(_.template(template));

        // refreshOnSave interferes with the hasSavedSettings check below
        this.settingsPage = new SettingsPage({ refreshOnSave: false });
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

