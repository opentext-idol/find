/*
 * Copyright 2014-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/abstract-find-settings-page',
    'find/app/page/settings/aci-widget',
    'find/app/page/settings/community-widget',
    'i18n!find/nls/bundle'
], function(SettingsPage, AciWidget, CommunityWidget, i18n) {

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

                ]
            ];
        }
    });

});
