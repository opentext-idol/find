/*
 * Copyright 2016-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const Backbone = require('backbone');
const i18n = require('find/nls/bundle');
const configuration = require('find/app/configuration');
const TabContentView = require('find/app/page/search/document/tab-content-view');
const AuthorsTab = require('find/app/page/search/document/authors-tab');
const FactsTab = require('find/app/page/search/document/facts-tab');
const LocationTab = require('find/app/page/search/document/location-tab');
const SimilarDocumentsTab = require('find/app/page/search/document/similar-documents-tab');
const SimilarDatesTab = require('find/app/page/search/document/similar-dates-tab');
const MetadataTab = require('find/app/page/search/document/metadata-tab');
const SimilarSourcesTab = require('find/app/page/search/document/similar-sources-tab');
const TranscriptTab = require('find/app/page/search/document/transcript-tab');

const always = _.constant(true);

// Function rather than constant so tests can mock configuration
const hasBiRole = function() {
    return configuration().hasBiRole;
};

module.exports = [
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

