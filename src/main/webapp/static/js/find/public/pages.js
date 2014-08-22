define([
    'find/app/find-pages',
    'find/app/page/find-search',
    'i18n!find/nls/bundle'
], function(FindPages, FindSearch) {

    return FindPages.extend({

        initializePages: function() {
            this.pages = [
                {
                    constructor: FindSearch
                    , pageName: 'search'
                }
            ];
        }

    });

});