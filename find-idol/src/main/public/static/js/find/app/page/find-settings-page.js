/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/abstract-find-settings-page',
    'find/app/page/settings/aci-widget',
    'find/app/page/settings/community-widget',
    'find/app/page/settings/query-manipulation-widget',
    'find/app/page/settings/view-widget',
    'i18n!find/nls/bundle'
], function(SettingsPage, AciWidget, CommunityWidget, QueryManipulationWidget, ViewWidget, i18n) {

    return SettingsPage.extend({
        initializeWidgets: function() {
            this.widgetGroups = [
                [
                    new AciWidget({
                        configItem: 'content',
                        description: i18n['settings.content.description'],
                        isOpened: true,
                        title: i18n['settings.content.title'],
                        strings: this.serverStrings()
                    }),
                    new CommunityWidget({
                        configItem: 'login',
                        description: i18n['settings.community.description'],
                        isOpened: true,
                        securityTypesUrl: this.urlRoot + 'securitytypes',
                        serverName: i18n['settings.community.serverName'],
                        title: i18n['settings.community.title'],
                        strings: _.extend(this.serverStrings(), {
                            fetchSecurityTypes: i18n['settings.community.login.fetchTypes'],
                            iconClass: '',
                            invalidSecurityType: i18n['settings.community.login.invalidType'],
                            loginTypeLabel: i18n['settings.community.login.type'],
                            validateFailed: i18n['settings.test.failed']
                        })
                    })
                ], [
                    new QueryManipulationWidget({
                        configItem: 'queryManipulation',
                        description: 'Enable query manipulation with QMS',
                        isOpened: true,
                        serverName: 'qms',
                        title: 'Query Manipulation',
                        strings: _.extend(this.serverStrings(), {
                            disable: 'Disable Query Manipulation',
                            disabled: 'Query Manipulation is disabled',
                            enable: 'Enable Query Manipulation',
                            enabled: 'Query Manipulation is enabled',
                            loading: 'Loading...'
                        })
                    })
                ], [
                    new ViewWidget({
                        configItem: 'view',
                        description: 'View documents by either a custom field, or using a connector',
                        isOpened: true,
                        serverName: 'view',
                        title: 'View',
                        strings: _.extend(this.serverStrings(), {
                            referenceFieldLabel: i18n['settings.view.referenceFieldLabel'],
                            referenceFieldBlank: i18n['settings.view.referenceFieldBlank'],
                            referenceFieldPlaceholder: i18n['settings.view.referenceFieldPlaceholder']
                        })
                    })
                ]
            ];
        }
    });

});
