/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'i18n!find/nls/bundle',
    'find/app/page/abstract-find-settings-page',
    'find/app/page/settings/iod-widget',
    'find/app/page/settings/powerpoint-widget',
    'settings/js/widgets/single-user-widget',
    'underscore'
], function(i18n, SettingsPage, IodWidget, PowerPointWidget, SingleUserWidget, _) {
    'use strict';

    return SettingsPage.extend({
        initializeWidgets: function() {
            this.widgetGroups = [
                [
                    new IodWidget({
                        configItem: 'iod',
                        isOpened: true,
                        title: 'IoD settings',
                        strings: _.extend(this.serverStrings(), {
                            application: i18n['settings.iod.application'],
                            apiKey: i18n['settings.iod.apiKey'],
                            domain: i18n['settings.iod.domain'],
                            iconClass: 'key',
                            validateFailed: 'Invalid API Key',
                            validateSuccess: 'API Key OK',
                            validateButton: 'Test Key'
                        })
                    })
                ], [
                    new SingleUserWidget({
                        configItem: 'login',
                        description: i18n['settings.adminUser.description'],
                        isOpened: true,
                        strings: {
                            confirmPassword: 'Confirm Password',
                            currentPassword: 'Current Password',
                            iconClass: 'user',
                            newPassword: 'New Password',
                            passwordMismatch: 'Passwords do not match',
                            passwordRedacted: '(redacted)',
                            username: 'Username',
                            validateConfirmPasswordBlank: 'Confirm password must not be blank!',
                            validateCurrentPasswordBlank: 'Current password must not be blank!',
                            validateNewPasswordBlank: 'New password must not be blank!',
                            validateUsernameBlank: 'Username must not be blank!',
                            validateFailed: i18n['settings.test.failed']
                        },
                        title: i18n['settings.adminUser']
                    })
                ], [
                    new PowerPointWidget({
                        configItem: 'powerPoint',
                        description: i18n['settings.powerpoint.description'],
                        isOpened: true,
                        title: i18n['settings.powerpoint'],
                        strings: this.serverStrings(),
                        pptxTemplateUrl: this.pptxTemplateUrl
                    })
                ]
            ];
        }
    });
});
