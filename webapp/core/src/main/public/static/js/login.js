/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

require(['require-config'], function() {
    'use strict';

    require([
        'underscore',
        'login-page/js/login',
        'text!find/templates/app/page/login/login.html',
        'i18n!find/nls/bundle'
    ], function(_, Login, template, i18n) {
        const FindLogin = Login.extend({
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
