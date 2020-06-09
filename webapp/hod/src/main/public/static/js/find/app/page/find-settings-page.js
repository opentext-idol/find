/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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

define([
    'find/app/page/abstract-find-settings-page',
    'find/app/page/settings/iod-widget',
    'settings/js/widgets/single-user-widget',
    'underscore'
], function(SettingsPage, IodWidget, SingleUserWidget, _) {
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
                ]
            ];
        }
    });
});
