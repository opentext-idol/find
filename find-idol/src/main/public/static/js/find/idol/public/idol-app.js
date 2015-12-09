/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/public/app',
    'find/idol/public/idol-pages',
    'find/idol/public/idol-navigation'
], function(BaseApp, Pages, Navigation) {
    'use strict';

    return BaseApp.extend({

        Navigation: Navigation,

        constructPages: function() {
            return new Pages();
        }
    });
});
