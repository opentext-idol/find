/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/configuration',
    'find/app/page/search/document/tab-content-view',
    'find/app/page/search/document/authors-tab',
    'find/app/page/search/document/location-tab',
    'find/app/page/search/document/similar-documents-tab',
    'find/app/page/search/document/similar-dates-tab',
    'find/app/page/search/document/metadata-tab',
    'find/app/page/search/document/similar-sources-tab',
    'find/app/page/search/document/transcript-tab'
], function(Backbone, _, i18n, configuration, TabContentView, AuthorsTab, LocationTab, SimilarDocumentsTab, SimilarDatesTab, MetadataTab, SimilarSourcesTab, TranscriptTab) {

    'use strict';

    var always = _.constant(true);

    function inRange(min, max, value) {
        return value >= min && value <= max;
    }

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
                var longitude = documentModel.get('longitude');
                var latitude = documentModel.get('latitude');
                return configuration().map.enabled && inRange(-180, 180, longitude) && inRange(-90, 90, latitude);
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

            shown: always
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
        }
    ];

});
