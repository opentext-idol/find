define([
    'js-utils/js/abstract-pages',
    'find/app/router',
    'find/app/vent',
    'find/app/page/find-search',
    'find/app/page/find-settings-page',
    'i18n!find/nls/bundle'
], function(AbstractPages, router, vent, FindSearch, SettingsPage, i18n) {

    return AbstractPages.extend({

        routePrefix: 'find/',

        eventName: 'route:find',

        vent: vent,

        router: router,

        initializePages: function() {
            this.pages = [
                {
                    constructor: FindSearch
                    , pageName: 'search'
                    , label: 'First Page'
                    , group: false
                    , classes: ''
                }, {
                    constructor: SettingsPage
                    , pageName: 'settings'
                    , label: i18n['app.settings']
                    , group: 'settings'
                    , classes: 'hide-from-non-useradmin'
                }
            ];
        }
    });
});