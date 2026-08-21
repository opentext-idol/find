'use strict';

// Temporary Phase-4 stand-in for core/src/main/public/static/js/login.js, which still uses
// the two-phase require(['require-config'], ...) bootstrap - see build/entry/public.js for
// why. This mirrors login.js's inner require() body exactly; Phase 5 deletes this file and
// rewrites login.js to this same shape.
const _ = require('underscore');
const Login = require('login-page/js/login');
const template = require('text!find/templates/app/page/login/login.html');
const i18n = require('find/nls/bundle');

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
