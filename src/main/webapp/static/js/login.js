require(['require-config'], function() {
    require([
        'login-page/js/login',
        'i18n!find/nls/bundle'
    ], function(Login, i18n) {
        new Login({
            configURL: '/config/',
            url: '../authenticate',
            strings: {
                defaultLogin: i18n['login.defaultLogin'],
                important: i18n['login.important'],
                infoDefaultLogin: i18n['login.infoDefaultLogin'],
                infoPasswordCopyPaste: i18n['login.infoPasswordCopyPaste'],
                infoSearchConfig: i18n['login.infoSearchConfig'],
                login: i18n['login.login'],
                moreInfoLink: i18n['login.moreInfo'],
                newCreds: i18n['login.newCredentials'],
                password: i18n['settings.password'],
                title: i18n['login.title'](i18n['app.name']),
                username: i18n['settings.username'],
                error: {
                    auth: i18n['login.error.auth'],
                    connection: i18n['login.error.connection'],
                    nonadmin: i18n['login.error.nonadmin']
                }
            }
        });
    });
});