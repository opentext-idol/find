define([
    'i18n!find/nls/bundle',
    'find/app/vent',
    'find/app/router',
    'settings/js/settings-page',
    'find/app/model/config',
    'find/app/page/settings/iod-widget',
    'settings/js/widgets/single-user-widget'
], function(i18n, vent, router, SettingsPage, configModel, IodWidget, SingleUserWidget) {

    var serverStrings = function() {
        return {
            databaseLabel: i18n['settings.database'],
            hostPlaceholder: i18n['placeholder.hostname'],
            portPlaceholder: i18n['placeholder.port'],
            usernameLabel: i18n['settings.username'],
            validateButton: i18n['settings.test'],
            validateFailed: i18n['settings.test.failed'],
            validateHostBlank: i18n['settings.test.hostBlank'],
            validatePasswordBlank: i18n['settings.test.passwordBlank'],
            validateUsernameBlank: i18n['settings.test.usernameBlank'],
            validateSuccess: i18n['settings.test.ok']
        };
    };

    var passwordStrings = function(){
        return {
            passwordDescription: i18n['settings.password.description'],
            passwordLabel: i18n['settings.password'],
            passwordRedacted: i18n['settings.password.redacted']
        };
    };

    var urlRoot = /\bconfig\/[\/]*$/.test(window.location.pathname) ? '../api/config/config/' : '../api/useradmin/config/';

    return SettingsPage.extend({
        configModel: configModel,
        router: router,
        routeRoot: 'find/settings',
        scrollSelector: '.body',
        vent: vent,
        validateUrl: urlRoot + 'config-validation',
        strings: {
            cancelButton: i18n['settings.cancel'],
            cancelCancel: i18n['app.cancel'],
            cancelMessage: i18n['settings.cancel.message'],
            cancelOk: i18n['app.ok'],
            cancelTitle: i18n['settings.cancel.title'],
            confirmUnload: i18n['settings.unload.confirm'],
            requiredFields: i18n['settings.requiredFields'],
            restoreButton: i18n['settings.restoreChanges'],
            saveButton: i18n['settings.save'],
            title: i18n['app.settings'],
            description: function(configEnv, configPath) {
                return i18n['settings.description'](configEnv, configPath);
            },
            saveModal: {
                cancel: i18n['settings.cancel'],
                close: i18n['settings.close'],
                confirm: i18n['settings.save.confirm'],
                errorThrown: i18n['settings.save.errorThrown'],
                failure: i18n['settings.save.failure'],
                failureAnd: i18n['settings.save.failure.and'],
                failureValidationMessage: i18n['settings.save.failure.validation.message'],
                failureText: i18n['settings.save.failure.text'],
                retry: i18n['settings.retry'],
                retypePass: i18n['settings.save.retypePassword'],
                save: i18n['settings.save'],
                saving: i18n['settings.save.saving'],
                success: i18n['settings.save.success'],
                successMessage: i18n['settings.save.success.message'],
                title: i18n['settings.save.confirm.title'],
                unknown: i18n['settings.save.unknown']
            }
        },

        initializeWidgets: function() {
            this.leftWidgets = [
                new IodWidget({
                    configItem: 'iod',
                    isOpened: true,
                    title: 'IoD settings',
                    strings: _.extend(serverStrings(), {
                        iconClass: 'key',
                        validateFailed: 'Invalid API Key',
                        validateSuccess: 'API Key OK',
                        validateButton: 'Test Key'
                    })
                })
            ];

            this.middleWidgets = [
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
            ];
        }
    });

});
