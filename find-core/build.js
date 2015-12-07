/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

({
    appDir: "${basedir}/src/main/public",
    baseUrl: "static/js",
    dir: "${basedir}/target/${build.finalName}",
    keepBuildDir: "true",
    mainConfigFile: '${basedir}/src/main/public/static/js/require-config.js',
    modules: [
        {
            name: "main",
            include: [
                'require-config',
                'find/app/app'
            ]
        }, {
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
                'find/public/app'
            ]
        }
    ]
})
