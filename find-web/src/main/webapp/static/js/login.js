/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

require(['require-config'], function() {
    require([
        'login-page/js/login',
        'text!find/templates/app/page/login/login.html',
        'i18n!find/nls/bundle'
    ], function(Login, template, i18n) {
        var FindLogin = Login.extend({
            template: _.template(template),
            controlGroupClass: 'form-group',
            errorClass: 'has-error'
        });

        new FindLogin({
            configURL: '/config/',
            url: 'authenticate',
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
