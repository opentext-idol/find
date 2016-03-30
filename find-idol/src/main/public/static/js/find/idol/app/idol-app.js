/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/app',
    'find/idol/app/idol-pages',
    'find/idol/app/idol-navigation'
], function(BaseApp, Pages, Navigation) {
    'use strict';

    return BaseApp.extend({

        Navigation: Navigation,

        constructPages: function() {
            return new Pages();
        }
    });
});
