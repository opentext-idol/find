/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/page/search/document/tab-content-view',
    'find/app/page/search/document/authors-tab',
    'find/app/page/search/document/facts-tab',
    'find/app/page/search/document/location-tab',
    'find/app/page/search/document/similar-documents-tab',
    'find/app/page/search/document/similar-dates-tab',
    'find/app/page/search/document/metadata-tab',
    'find/app/page/search/document/similar-sources-tab',
    'find/app/page/search/document/transcript-tab'
], function(_, Backbone, i18n, configuration, TabContentView, AuthorsTab, FactsTab, LocationTab,
            SimilarDocumentsTab, SimilarDatesTab, MetadataTab, SimilarSourcesTab,
            TranscriptTab) {
    'use strict';

    const always = _.constant(true);

    // Function rather than constant so tests can mock configuration
    const hasBiRole = function() {
        return configuration().hasBiRole;
    };

    return [
        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: MetadataTab}),
            title: i18n['search.document.detail.tabs.metadata'],
            shown: always
        },
        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: AuthorsTab}),
            title: i18n['search.document.detail.tabs.authors'],
            shown: function(documentModel) {
                return documentModel.get('authors').length > 0;
            }
        },
        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: LocationTab}),
            title: i18n['search.document.detail.tabs.location'],
            shown: function(documentModel) {
                const locations = documentModel.get('locations');
                return configuration().map.enabled && !_.isEmpty(locations);
            }
        },
        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: SimilarDocumentsTab}),
            title: i18n['search.document.detail.tabs.similarDocuments'],
            shown: always
        },
        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: SimilarDatesTab}),
            title: i18n['search.document.detail.tabs.similarDates'],
            shown: hasBiRole
        },
        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: SimilarSourcesTab}),
            title: i18n['search.document.detail.tabs.similarSources'],
            shown: function(documentModel) {
                return documentModel.has('sourceType');
            }
        },
        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: TranscriptTab}),
            title: i18n['search.document.detail.tabs.transcript'],
            shown: function(documentModel) {
                return documentModel.isMedia() && documentModel.has('transcript');
            }
        },
        {
            TabContentConstructor: TabContentView.extend({TabSubContentConstructor: FactsTab}),
            title: i18n['search.document.detail.tabs.facts'],
            shown: function(documentModel) {
                // when enabled, the field always exists, possibly with an empty-object value
                const facts = _.findWhere(documentModel.get('fields'), { id: 'facts' });
                return facts && facts.values[0].fact_extract_;
            }
        }
    ];
});
