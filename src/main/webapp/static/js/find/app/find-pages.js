define([
    'js-whatever/js/abstract-pages',
    'find/app/router',
    'find/app/vent'
], function(AbstractPages, router, vent) {

    return AbstractPages.extend({

        routePrefix: 'find/',

        eventName: 'route:find',

        vent: vent,

        router: router


    });
});