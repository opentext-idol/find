define([
    'find/app/find-pages',
    'find/app/page/find-search',
    'i18n!find/nls/bundle'
], function(FindPages, FindSearch, i18n) {

    return FindPages.extend({

        initializePages: function() {
            this.pages = [
                {
                    constructor: FindSearch
                    , pageName: 'search'
                    , label: 'Search'
                    , group: false
                    , classes: ''
                }
            ];
        }

    });

});