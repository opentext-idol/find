define([
    'js-utils/js/abstract-pages',
    'find/app/router',
    'find/app/vent',
    'find/app/page/find-search',
    'i18n!find/nls/bundle'
], function(AbstractPages, router, vent, FindSearch, i18n) {

    return AbstractPages.extend({

        routePrefix: 'find/',

        eventName: 'route:find',

        vent: vent,

        router: router,

        initializePages: function() {
            this.pages = [
                {
                    constructor: FindSearch
                    , pageName: 'find-search'
                    , label: 'First Page'
                    , icon: 'icon-thumbs-down'
                    , group: false
                    , classes: ''
                }
            ];
        }
    });
});