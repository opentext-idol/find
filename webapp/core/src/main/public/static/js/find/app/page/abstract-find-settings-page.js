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
    'settings/js/settings-page',
    'find/app/page/settings/validate-on-save-modal',
    'find/app/util/confirm-view',
    'find/app/model/config',
    'find/app/vent',
    'find/app/router',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/settings-page.html',
    'underscore'
], function(SettingsPage, ValidateOnSaveModal, Confirm, configModel, vent, router, i18n, template, _) {
    'use strict';

    var urlRoot = /\bconfig$/.test(window.location.pathname) ? 'api/config/config/' : 'api/admin/config/';

    return SettingsPage.extend({
        className: 'container-fluid',
        SaveModalConstructor: ValidateOnSaveModal,
        configModel: configModel,
        groupClass: 'col-md-4',
        router: router,
        routeRoot: 'settings',
        scrollSelector: '.body',
        template: _.template(template),
        urlRoot: urlRoot,
        vent: vent,
        validateUrl: urlRoot + 'config-validation',
        widgetGroupParent: 'form .row',

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
                alertErrorClass: 'danger',
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
                unknown: i18n['settings.save.unknown'],
                CONNECTION_ERROR: i18n['settings.CONNECTION_ERROR'],
                DEFAULT_LOGIN: i18n['settings.DEFAULT_LOGIN'],
                FETCH_PORT_ERROR: i18n['settings.FETCH_PORT_ERROR'],
                FETCH_SERVICE_PORT_ERROR: i18n['settings.FETCH_SERVICE_PORT_ERROR'],
                INCORRECT_SERVER_TYPE: i18n['settings.INCORRECT_SERVER_TYPE'],
                INVALID_CONVERSATION_SYSTEM_NAME: i18n['settings.INVALID_CONVERSATION_SYSTEM_NAME'],
                INVALID_SYSTEM_NAME: i18n['settings.INVALID_SYSTEM_NAME'],
                INDEX_PORT_ERROR: i18n['settings.INDEX_PORT_ERROR'],
                REQUIRED_FIELD_MISSING: i18n['settings.REQUIRED_FIELD_MISSING'],
                REGULAR_EXPRESSION_MATCH_ERROR: i18n['settings.REGULAR_EXPRESSION_MATCH_ERROR'],
                SERVICE_AND_INDEX_PORT_ERROR: i18n['settings.SERVICE_AND_INDEX_PORT_ERROR'],
                SERVICE_PORT_ERROR: i18n['settings.SERVICE_PORT_ERROR'],
                INVALID_CREDENTIALS: i18n['settings.INVALID_CREDENTIALS'],
                UNKNOWN_ERROR: i18n['error.unknown']
            }
        },

        serverStrings: function() {
            return {
                loading: i18n['app.loading'],
                databaseLabel: i18n['settings.database'],
                hostPlaceholder: i18n['placeholder.hostname'],
                portPlaceholder: i18n['placeholder.port'],
                usernameLabel: i18n['settings.username'],
                validateButton: i18n['settings.test'],
                validateFailed: i18n['settings.test.failed'],
                validateHostBlank: i18n['settings.test.hostBlank'],
                validatePasswordBlank: i18n['settings.test.passwordBlank'],
                validatePortInvalid: i18n['settings.test.portInvalid'],
                validateUsernameBlank: i18n['settings.test.usernameBlank'],
                validateSuccess: i18n['settings.test.ok'],
                passwordLabel: i18n['settings.password'],
                passwordRedacted: i18n['settings.password.redacted'],
                CONNECTION_ERROR: i18n['settings.CONNECTION_ERROR'],
                DEFAULT_LOGIN: i18n['settings.DEFAULT_LOGIN'],
                FETCH_PORT_ERROR: i18n['settings.FETCH_PORT_ERROR'],
                FETCH_SERVICE_PORT_ERROR: i18n['settings.FETCH_SERVICE_PORT_ERROR'],
                INCORRECT_SERVER_TYPE: i18n['settings.INCORRECT_SERVER_TYPE'],
                INVALID_CONVERSATION_SYSTEM_NAME: i18n['settings.INVALID_CONVERSATION_SYSTEM_NAME'],
                INVALID_SYSTEM_NAME: i18n['settings.INVALID_SYSTEM_NAME'],
                INDEX_PORT_ERROR: i18n['settings.INDEX_PORT_ERROR'],
                REQUIRED_FIELD_MISSING: i18n['settings.REQUIRED_FIELD_MISSING'],
                REGULAR_EXPRESSION_MATCH_ERROR: i18n['settings.REGULAR_EXPRESSION_MATCH_ERROR'],
                SERVICE_AND_INDEX_PORT_ERROR: i18n['settings.SERVICE_AND_INDEX_PORT_ERROR'],
                SERVICE_PORT_ERROR: i18n['settings.SERVICE_PORT_ERROR'],
                INVALID_CREDENTIALS: i18n['settings.INVALID_CREDENTIALS'],
                UNKNOWN_ERROR: i18n['error.unknown']
            };
        },

        handleCancelButton: function(e) {
            e.preventDefault();

            new Confirm({
                cancelClass: 'button-secondary',
                cancelIcon: 'hp-icon hp-fw hp-close',
                cancelText: this.strings.cancelCancel,
                okText: this.strings.cancelOk,
                okClass: 'button-primary',
                okIcon: 'hp-icon hp-fw hp-reset',
                message: this.strings.cancelMessage,
                title: this.strings.cancelTitle,
                hiddenEvent: 'hidden.bs.modal',
                okHandler: _.bind(function() {
                    this.loadFromConfig();
                }, this)
            });
        },

        onConfigSaved: function () {
            // it should be possible to have config changes take effect without a reload, but there
            // are lots of places that would need changes to re-render on change, so this makes it
            // work for now
            setTimeout(function () { location.reload(); }, 1000);
        }
    });
});
