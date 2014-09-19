define([
    'find/app/page/find-settings-page',
    'js-utils/js/empty-navbar',
    'find/app/util/test-browser',
    'i18n!find/nls/bundle',
    'text!find/templates/config/config.html',
    'underscore'
], function(SettingsPage, EmptyNavbar, testBrowser, i18n, template, _) {

    return function () {
        jQuery.ajaxSetup({ cache: false });

        var $page = $('.page');

        $page.html(_.template(template));

        this.settingsPage = new SettingsPage({});
        this.settingsPage.render();

        this.navigation = new (EmptyNavbar.extend({
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
                appName: 'Find',
                logout: 'Logout from Settings'
            },
            logoutUri: '../logout',
            settingsPage: this.settingsPage,
            showLogout: true
        });

        this.navigation.render();
        $('.header').append(this.navigation.el);
        this.settingsPage.show();
        $('.content').append(this.settingsPage.el);

        testBrowser();
    }
});