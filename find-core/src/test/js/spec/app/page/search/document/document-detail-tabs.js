/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/page/search/document/document-detail-tabs',
    'find/app/model/document-model'
], function (Backbone, $, i18n, DocumentDetailTabs, DocumentModel) {

    describe('Tabs should filter based on the document model', function () {

        describe('Should show all tabs', function () {
            var tabs;
            beforeEach(function () {
                var basicModel = new DocumentModel({
                    authors: ['Humbert', 'Gereon'],
                    media: true,
                    sourceType: 'news',
                    thumbnail: 'VGhlIGJhc2UgNjQgZW5jb2RlZCB0aHVtYm5haWw=',
                    transcript: 'test transcript',
                    url: true
                });

                tabs = _.filter(DocumentDetailTabs, function (tab) {
                    return tab.shown(basicModel);
                })
            });


            it('should display every tab', function () {
                expect(tabs.length).toBe(6);
            });
        });

        describe('Should show specific tabs', function () {

            beforeEach(function () {

            });


            it('should display default tabs', function () {
                var basicModel = new DocumentModel();

                var tabs = _.filter(DocumentDetailTabs, function (tab) {
                    return tab.shown(basicModel);
                });

                expect(tabs.length).toBe(3);
            });

            it('should display author tab', function () {
                var basicModel = new DocumentModel({
                    authors: ['Humbert', 'Gereon']
                });

                var tabs = _.filter(DocumentDetailTabs, function (tab) {
                    return tab.shown(basicModel);
                });

                expect(tabs.length).toBe(4);
                expect(_.find(tabs, function(tab) {
                    return tab.title === i18n['search.document.detail.tabs.authors'];
                }))
            });

            it('should display similar sources tab', function () {
                var basicModel = new DocumentModel({
                    sourceType: 'News'
                });

                var tabs = _.filter(DocumentDetailTabs, function (tab) {
                    return tab.shown(basicModel);
                });

                expect(tabs.length).toBe(4);
                expect(_.find(tabs, function(tab) {
                    return tab.title === i18n['search.document.detail.tabs.similarSources'];
                }))
            });

            it('should display transcript tab', function () {
                var basicModel = new DocumentModel({
                    media: true,
                    url: true,
                    transcript: 'test transcript'
                });

                var tabs = _.filter(DocumentDetailTabs, function (tab) {
                    return tab.shown(basicModel);
                });

                expect(tabs.length).toBe(4);
                expect(_.find(tabs, function(tab) {
                    return tab.title === i18n['search.document.detail.tabs.transcript'];
                }))
            });
        });

    });
});
