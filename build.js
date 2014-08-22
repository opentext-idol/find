({
    appDir: "${basedir}/src/main/webapp",
    baseUrl: "static/js",
    dir: "${basedir}/target/${build.finalName}",
    keepBuildDir: "true",
    mainConfigFile: '${basedir}/src/main/webapp/static/js/require-config.js',
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
