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
                        description: i18n['settings.queryManipulation.description'],
                        isOpened: true,
                        title: i18n['settings.queryManipulation.blacklist'],
                        strings: _.extend(this.serverStrings(), {
                            blacklist: i18n['settings.queryManipulation.blacklist'],
                            disable: i18n['settings.queryManipulation.disable'],
                            disabled: i18n['settings.queryManipulation.disabled'],
                            dictionary: i18n['settings.queryManipulation.dictionary'],
                            expandQuery: i18n['settings.queryManipulation.expandQuery'],
                            enable: i18n['settings.queryManipulation.enable'],
                            enabled: i18n['settings.queryManipulation.enabled'],
                            index: i18n['settings.queryManipulation.index'],
                            loading: i18n['settings.queryManipulation.loading'],
                            typeaheadMode: i18n['settings.queryManipulation.typeaheadMode']
                        })
                    })
                ], [
                    new ViewWidget({
                        configItem: 'view',
                        description: i18n['settings.view.description'],
                        isOpened: true,
                        title: i18n['settings.view'],
                        strings: _.extend(this.serverStrings(), {
                            connector: i18n['settings.view.connector'],
                            referenceFieldLabel: i18n['settings.view.referenceFieldLabel'],
                            referenceFieldBlank: i18n['settings.view.referenceFieldBlank'],
                            referenceFieldPlaceholder: i18n['settings.view.referenceFieldPlaceholder'],
                            viewingMode: i18n['settings.view.viewingMode']
                        })
                    })
                ]
            ];
        }
    });

});
