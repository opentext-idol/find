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

        this.navigation = new EmptyNavbar({
            strings: {
                appName: 'Find'
            },
            showLogout: false
        });

        this.navigation.render();
        $('.header').append(this.navigation.el);

        this.settingsPage = new SettingsPage({});

        this.settingsPage.render();
        this.settingsPage.show();

        $('.content').append(this.settingsPage.el);

        testBrowser();
    }
});