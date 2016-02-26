/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'find/app/page/search/document/document-detail-tabs',
    'find/app/model/document-model'
], function(Backbone, $, configuration, i18n, documentDetailTabs, DocumentModel) {

    function filterTabs(model) {
        return _.filter(documentDetailTabs, function(tab) {
            return tab.shown(model);
        });
    }

    function byTitleKey(tabs, titleKey) {
        return _.findWhere(tabs, {title: i18n[titleKey]});
    }

    describe('Document detail tabs', function() {
        describe('with the location tab enabled', function() {
            beforeEach(function() {
                configuration.and.returnValue({
                    map: {
                        enabled: true
                    }
                });
            });

            it('displays every tab for a complete model with map enabled', function() {
                var model = new DocumentModel({
                    authors: ['Humbert', 'Gereon'],
                    media: true,
                    sourceType: 'news',
                    thumbnail: 'VGhlIGJhc2UgNjQgZW5jb2RlZCB0aHVtYm5haWw=',
                    transcript: 'test transcript',
                    longitude: 123.5,
                    latitude: -23,
                    url: true
                });

                expect(filterTabs(model).length).toBe(documentDetailTabs.length);
            });

            it('displays default tabs for an empty model', function() {
                var model = new DocumentModel({
                    authors: [],
                    latitude: undefined,
                    longitude: undefined
                });

                expect(filterTabs(model).length).toBe(3);
            });

            it('displays the author tab when an authors attribute is present', function() {
                var model = new DocumentModel({
                    authors: ['Humbert', 'Gereon']
                });

                var tabs = filterTabs(model);
                expect(tabs.length).toBe(4);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.authors')).toBeDefined();
            });

            it('displays similar sources tab when a source attribute is present', function() {
                var model = new DocumentModel({
                    sourceType: 'News'
                });

                var tabs = filterTabs(model);
                expect(tabs.length).toBe(4);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.similarSources')).toBeDefined();
            });

            it('displays the transcript tab when the model has transcript, media and url attributes', function() {
                var model = new DocumentModel({
                    media: true,
                    url: true,
                    transcript: 'test transcript'
                });

                var tabs = filterTabs(model);
                expect(tabs.length).toBe(4);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.transcript')).toBeDefined();
            });

            it('displays the location tab if the longitude and latitude attributes are present and within the required ranges', function() {
                var model = new DocumentModel({
                    longitude: 123.4,
                    latitude: -23
                });

                var tabs = filterTabs(model);
                expect(tabs.length).toBe(4);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.location')).toBeDefined();
            });

            it('does not display the location tab if only the longitude attribute is present', function() {
                var model = new DocumentModel({
                    longitude: 123.4,
                    latitude: undefined
                });

                var tabs = filterTabs(model);
                expect(tabs.length).toBe(3);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.location')).toBeUndefined();
            });

            it('does not display the location tab if the latitude is less than -90', function() {
                var model = new DocumentModel({
                    longitude: 123.4,
                    latitude: -123
                });

                var tabs = filterTabs(model);
                expect(tabs.length).toBe(3);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.location')).toBeUndefined();
            });
        });

        describe('with the location tab disabled', function() {
            beforeEach(function() {
                configuration.and.returnValue({
                    map: {
                        enabled: false
                    }
                });
            });

            it('does not display the location tab even if longitude and latitude attributes are present', function() {
                var model = new DocumentModel({
                    longitude: 123.4,
                    latitude: 73
                });

                var tabs = filterTabs(model);
                expect(tabs.length).toBe(3);
                expect(byTitleKey(tabs, 'search.document.detail.tabs.location')).toBeUndefined();
            });
        });
    });

});
