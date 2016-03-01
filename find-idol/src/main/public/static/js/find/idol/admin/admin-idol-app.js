/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/idol/public/idol-app',
    'find/idol/admin/admin-idol-pages'
], function(IdolApp, Pages) {
    'use strict';

    return IdolApp.extend({

        constructPages: function() {
            return new Pages();
        }

    });
});
