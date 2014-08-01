define([
    'find/app/find-pages',
    'find/app/page/find-search',
    'find/app/page/find-settings-page',
    'i18n!find/nls/bundle'
], function(FindPages, FindSearch, SettingsPage, i18n) {

    return FindPages.extend({

        initializePages: function() {
            this.pages = [
                {
                    constructor: FindSearch
                    , pageName: 'search'
                    , label: 'Search'
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