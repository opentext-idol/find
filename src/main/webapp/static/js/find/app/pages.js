define([
    'js-utils/js/abstract-pages',
    'find/app/router',
    'find/app/vent',
    'find/app/page/first-page',
    'find/app/page/second-page',
    'find/app/page/find-settings-page',
    'find/app/page/find-users-page',
    'find/app/page/about-page',
    'i18n!find/nls/bundle'
], function(AbstractPages, router, vent, FirstPage, SecondPage, SettingsPage, UsersPage, AboutPage, i18n) {

    return AbstractPages.extend({

        routePrefix: 'find/',

        eventName: 'route:find',

        vent: vent,

        router: router,

        initializePages: function() {
            this.pages = [
                {
                    constructor: FirstPage
                    , pageName: 'first-page'
                    , label: 'First Page'
                    , icon: 'icon-thumbs-down'
                    , group: false
                    , classes: ''
                }, {
                    constructor: SecondPage
                    , pageName: 'second-page'
                    , label: 'Second Page'
                    , icon: false
                    , group: 'group'
                    , classes: ''
                }, {
                    constructor: SettingsPage
                    , pageName: 'settings'
                    , label: i18n['app.settings']
                    , icon: false
                    , group: 'settings'
                    , classes: 'hide-from-non-useradmin'
                }, {
                    constructor: UsersPage
                    , pageName: 'users'
                    , label: i18n['app.users']
                    , icon: false
                    , group: 'settings'
                    , classes: 'hide-from-non-useradmin'
                }, {
                    constructor: AboutPage
                    , pageName: 'about'
                    , label: i18n['app.about']
                    , icon: false
                    , group: 'settings'
                    , classes: ''
                }
            ];
        }
    });
});