/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

({
    appDir: "${static-resources-dir}",
    baseUrl: "static/js",
    dir: "${project.build.outputDirectory}",
    keepBuildDir: true,
    mainConfigFile: '${static-resources-dir}/static/js/require-config.js',
    modules: [
        {
            name: "login",
            include: [
                'require-config',
                'login-page/js/login',
                'i18n!find/nls/bundle'
            ]
        }, {
            name: "config",
            include: [
                'require-config',
                'find/config/app'
            ]
        }, {
            name: "public",
            include: [
                'require-config',
                'find/hod/public/hod-app'
            ]
        }
    ]
})
