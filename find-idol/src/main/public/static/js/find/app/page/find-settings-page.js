/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/abstract-find-settings-page',
    'find/app/page/settings/aci-widget',
    'find/app/page/settings/community-widget',
    'find/app/page/settings/map-widget',
    'find/app/page/settings/mmap-widget',
    'find/app/page/settings/query-manipulation-widget',
    'find/app/page/settings/view-widget',
    'i18n!find/nls/bundle'
], function(SettingsPage, AciWidget, CommunityWidget, MapWidget, MmapWidget, QueryManipulationWidget, ViewWidget, i18n) {

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
                ], [
                    new MmapWidget({
                        configItem: 'mmap',
                        description: i18n['settings.mmap.description'],
                        isOpened: true,
                        title: i18n['settings.mmap'],
                        strings: _.extend(this.serverStrings(), {
                            disable: i18n['settings.mmap.disable'],
                            disabled: i18n['settings.mmap.disabled'],
                            enable: i18n['settings.mmap.enable'],
                            enabled: i18n['settings.mmap.enabled'],
                            loading: i18n['settings.mmap.loading'],
                            url: i18n['settings.mmap.url']
                        })
                    }),
                    new MapWidget({
                        configItem: 'map',
                        description: i18n['settings.map.description'],
                        isOpened: true,
                        title: i18n['settings.map'],
                        strings: _.extend(this.serverStrings(), {
                            attribution: i18n['settings.map.attribution'],
                            disable: i18n['settings.map.disable'],
                            disabled: i18n['settings.map.disabled'],
                            enable: i18n['settings.map.enable'],
                            enabled: i18n['settings.map.enabled'],
                            loading: i18n['settings.map.loading'],
                            url: i18n['settings.map.url']
                        })
                    })
                ]
            ];
        }
    });

});
