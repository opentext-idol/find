/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

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
