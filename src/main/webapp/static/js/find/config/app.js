define([
    'find/app/page/find-settings-page',
    'find/app/util/test-browser',
    'i18n!find/nls/bundle',
    'underscore'
], function(SettingsPage, testBrowser, i18n, _) {

    return function () {
        jQuery.ajaxSetup({ cache: false });

        this.settingsPage = new SettingsPage({
            el: '.page'
        });

        this.settingsPage.render();

        testBrowser();
    }
});