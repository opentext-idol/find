/*
 * (c) Copyright 2014-2016 Micro Focus or one of its affiliates.
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
    'underscore',
    'find/app/page/abstract-find-settings-page',
    'find/app/page/settings/aci-widget',
    'find/app/page/settings/answer-server-widget',
    'find/app/page/settings/community-widget',
    'find/app/page/settings/optional-aci-widget',
    'find/app/page/settings/map-widget',
    'find/app/page/settings/motd-widget',
    'find/app/page/settings/mmap-widget',
    'find/app/page/settings/query-manipulation-widget',
    'find/app/page/settings/saved-search-widget',
    'find/app/page/settings/stats-server-widget',
    'find/app/page/settings/view-widget',
    'find/app/page/settings/control-point-widget',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/settings/community-widget.html'
], function (_, SettingsPage, AciWidget, AnswerServerWidget, CommunityWidget, OptionalAciWidget,
             MapWidget, MotdWidget, MmapWidget, QueryManipulationWidget, SavedSearchWidget,
             StatsServerWidget, ViewWidget, ControlPointWidget, i18n, dropdownTemplate) {

    return SettingsPage.extend({
        initializeWidgets: function () {
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
                        template: dropdownTemplate,
                        title: i18n['settings.community.title'],
                        strings: _.extend(this.serverStrings(), {
                            fetchSecurityTypes: i18n['settings.community.login.fetchTypes'],
                            iconClass: '',
                            invalidSecurityType: i18n['settings.community.login.invalidType'],
                            loginTypeLabel: i18n['settings.community.login.type'],
                            validateFailed: i18n['settings.test.failed']
                        })
                    }),
                    new OptionalAciWidget({
                        configItem: 'communityAgentStore',
                        description: i18n['settings.communityAgentStore.description'],
                        isOpened: true,
                        title: i18n['settings.communityAgentStore.title'],
                        strings: _.extend(this.serverStrings(), {
                            disable: i18n['settings.communityAgentStore.disable'],
                            disabled: i18n['settings.communityAgentStore.disabled'],
                            enable: i18n['settings.communityAgentStore.enable'],
                            enabled: i18n['settings.communityAgentStore.enabled'],
                            loading: i18n['settings.communityAgentStore.loading']
                        })
                    }),
                    new MotdWidget({
                        configItem: 'messageOfTheDay',
                        isOpened: true,
                        title: i18n['settings.messageOfTheDay'],
                        strings: _.extend(this.serverStrings(), {
                            description: i18n['settings.messageOfTheDay.description'],
                            message: i18n['settings.messageOfTheDay.message'],
                            status: i18n['settings.messageOfTheDay.status'],
                            statusInfo: i18n['settings.messageOfTheDay.status.info'],
                            statusWarning: i18n['settings.messageOfTheDay.status.warning']
                        })
                    })
                ], [
                    new QueryManipulationWidget({
                        configItem: 'queryManipulation',
                        description: i18n['settings.queryManipulation.description'],
                        isOpened: true,
                        title: i18n['settings.queryManipulation'],
                        strings: _.extend(this.serverStrings(), {
                            blacklist: i18n['settings.queryManipulation.blacklist'],
                            disable: i18n['settings.queryManipulation.disable'],
                            disabled: i18n['settings.queryManipulation.disabled'],
                            dictionary: i18n['settings.queryManipulation.dictionary'],
                            expandQuery: i18n['settings.queryManipulation.expandQuery'],
                            synonymDatabaseMatch: i18n['settings.queryManipulation.synonymDatabaseMatch'],
                            explicitProfiling: i18n['settings.queryManipulation.explicitProfiling'],
                            enable: i18n['settings.queryManipulation.enable'],
                            enabled: i18n['settings.queryManipulation.enabled'],
                            index: i18n['settings.queryManipulation.index'],
                            loading: i18n['settings.queryManipulation.loading'],
                            typeaheadMode: i18n['settings.queryManipulation.typeaheadMode']
                        })
                    }),
                    new ViewWidget({
                        configItem: 'view',
                        description: i18n['settings.view.description'],
                        isOpened: true,
                        title: i18n['settings.view'],
                        strings: _.extend(this.serverStrings(), {
                            connector: i18n['settings.view.connector'],
                            universal: i18n['settings.view.universal'],
                            referenceFieldLabel: i18n['settings.view.referenceFieldLabel'],
                            referenceFieldBlank: i18n['settings.view.referenceFieldBlank'],
                            referenceFieldPlaceholder: i18n['settings.view.referenceFieldPlaceholder'],
                            viewingMode: i18n['settings.view.viewingMode']
                        })
                    }),
                    new AnswerServerWidget({
                        configItem: 'answerServer',
                        title: i18n['settings.answerServer.title'],
                        isOpened: true,
                        description: i18n['settings.answerServer.description'],
                        strings: _.extend(this.serverStrings(), {
                            enable: i18n['settings.answerServer.enable'],
                            enabled: i18n['settings.answerServer.enabled'],
                            disable: i18n['settings.answerServer.disable'],
                            disabled: i18n['settings.answerServer.disabled'],
                            loading: i18n['settings.answerServer.loading'],
                            conversationSystem: i18n['settings.answerServer.conversationSystem'],
                            conversationSystemDisabled: i18n['settings.answerServer.conversationSystemDisabled'],
                            systemNames: i18n['settings.answerServer.systemNames'],
                            systemNamesDisabled: i18n['settings.answerServer.systemNamesDisabled']
                        })
                    }),
                    new ControlPointWidget({
                        configItem: 'controlPoint',
                        description: i18n['settings.controlPoint.description'],
                        isOpened: true,
                        title: i18n['settings.controlPoint.title'],
                        strings: _.extend(this.serverStrings(), {
                            enable: i18n['settings.controlPoint.enable'],
                            enabled: i18n['settings.controlPoint.enabled'],
                            disable: i18n['settings.controlPoint.disable'],
                            disabled: i18n['settings.controlPoint.disabled'],
                            CONNECTION_ERROR: i18n['settings.controlPoint.validation.CONNECTION_ERROR']
                        })
                    })
                ], [
                    new StatsServerWidget({
                        configItem: 'statsServer',
                        description: i18n['settings.statsserver.description'],
                        isOpened: true,
                        title: i18n['settings.statsserver.title'],
                        strings: _.extend(this.serverStrings(), {
                            disable: i18n['settings.statsserver.disable'],
                            disabled: i18n['settings.statsserver.disabled'],
                            enable: i18n['settings.statsserver.enable'],
                            enabled: i18n['settings.statsserver.enabled'],
                            loading: i18n['settings.statsserver.loading']
                        })
                    }),
                    new SavedSearchWidget({
                        configItem: 'savedSearches',
                        description: i18n['settings.savedSearches.description'],
                        isOpened: true,
                        title: i18n['settings.savedSearches'],
                        strings: _.extend(this.serverStrings(), {
                            loading: i18n['settings.mmap.loading'],
                            disablePolling: i18n['settings.savedSearches.polling.disable'],
                            enablePolling: i18n['settings.savedSearches.polling.enable'],
                            pollingDisabled: i18n['settings.savedSearches.polling.disabled'],
                            pollingEnabled: i18n['settings.savedSearches.polling.enabled'],
                            pollingInterval: i18n['settings.savedSearches.polling.interval']
                        })
                    }),
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
                            url: i18n['settings.map.url'],
                            resultsstep: i18n['settings.map.results.step']
                        })
                    })
                ]
            ];
        }
    });

});
