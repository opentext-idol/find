define([
    'config-wizard/js/config-wizard',
    'config-wizard/js/welcome',
    'find/app/page/find-settings-page',
    'find/app/page/find-users-page',
    'find/app/util/test-browser',
    'text!find/templates/config/config.html',
    'i18n!find/nls/bundle',
    'underscore'
], function(ConfigWizard, WelcomePage, SettingsPage, UsersPage, testBrowser, template, i18n, _) {

    return function () {
        jQuery.ajaxSetup({ cache: false });

        new ConfigWizard({
            el: '.page',
            logoutUri: '../login/login.html',
            navigationEl: '.header',
            template: _.template(template),
            wizardEl: '.content',
            steps: [
                {
                    constructor: WelcomePage,
                    class: 'welcome-panel',
                    label: i18n['wizard.step.welcome'],
                    active: true,
                    options: {
                        finish: i18n['wizard.welcome.finish'],
                        tagLine: i18n['wizard.welcome.helper'],
                        title: i18n['wizard.welcome'],
                        steps: [
                            i18n['wizard.welcome.step1'],
                            i18n['wizard.welcome.step2']
                        ]
                    }
                },
                {
                    constructor: SettingsPage,
                    class: 'settings-panel',
                    label: i18n['wizard.step.settings']
                },
                {
                    constructor: UsersPage,
                    class: 'users-panel',
                    label: i18n['wizard.step.users']
                }
            ],
            strings: {
                appName: i18n['app.name'],
                last: i18n['wizard.last'],
                next: i18n['wizard.next'],
                prev: i18n['wizard.prev']
            }
        });

        testBrowser();
    }
});