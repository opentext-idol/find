/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(['/src/js/require-config.js'], function() {
    require.config({
        baseUrl: '/src/static/js',
        paths: {
            /*  Directories  */
            mock: '/test/mock',
            real: '/src/static/js',
            resources: '/test/resources'

            /* Mocks */
            // replace this comment with your mocks
        }
    });
});
