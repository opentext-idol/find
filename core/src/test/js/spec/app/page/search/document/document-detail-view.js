/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'find/app/page/search/document/document-detail-view',
    'find/app/page/search/document/tab-content-view',
    'find/app/model/document-model',
    'find/app/configuration',
    'find/app/vent'
], function(Backbone, $, DocumentDetailView, TabContentView, DocumentModel, configuration, vent) {

    var BACK_URL = 'find/search/goback';
    var DOCUMENT_MODEL_REF = 'reference';

    var URL_LIKE_REFERENCE = 'http://www.example.com';
    var ANY_OLD_URL = 'www.example.com';

    var MOCK_TABS = [{
        TabContentConstructor: TabContentView.extend({ TabSubContentConstructor: Backbone.View }),
        title: 'some title',
        shown: function (documentModel) {
            return documentModel.get('reference') === DOCUMENT_MODEL_REF;
        }
    },
    {
        TabContentConstructor: TabContentView.extend({ TabSubContentConstructor: Backbone.View }),
        title: 'some other title',
        shown: function () { return true; }
    }];

    function getMockTabs(length) {
        return _.first(MOCK_TABS, length);
    }

    describe('DocumentDetailView', function() {
        beforeEach(function() {
            configuration.and.returnValue({
                mmapBaseUrl: ANY_OLD_URL,
                map: {enabled: false}
            });
        });

        afterEach(function() {
            vent.navigateToDetailRoute.calls.reset();
            vent.navigate.calls.reset();
        });

        describe('when the back button is clicked', function() {
            beforeEach(function() {
                this.view = new DocumentDetailView({
                    model: new DocumentModel(),
                    backUrl: BACK_URL,
                    indexesCollection: new Backbone.Collection()
                });

                this.view.render();
                this.view.$('.detail-view-back-button').click();
            });


            it('calls vent.navigate with the correct URL', function () {
                expect(vent.navigate).toHaveBeenCalledWith(BACK_URL);
            });
        });

        describe('when requirements for a tab to be rendered are met', function() {
            describe('when the view renders with a single tab defined', function() {
                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: DOCUMENT_MODEL_REF
                        }),
                        indexesCollection: new Backbone.Collection()
                    });

                    this.view.tabs = this.view.filterTabs(getMockTabs(1));
                    this.view.render();
                });

                it('should render a single (active) tab header', function () {
                    expect(this.view.$('.document-detail-tabs').children()).toHaveLength(1);
                    expect(this.view.$('.document-detail-tabs').children('.active')).toHaveLength(1);
                });

                it('should render a single (active) tab content', function () {
                    expect(this.view.$('.tab-content-view-container')).toHaveClass('active');
                });
            });

            describe('when the view renders with more than one tab defined', function() {
                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: DOCUMENT_MODEL_REF
                        }),
                        indexesCollection: new Backbone.Collection()
                    });

                    this.view.tabs = this.view.filterTabs(getMockTabs(2));
                    this.view.render();
                });

                it('should render 2 tab headers, with only 1 active', function () {
                    expect(this.view.$('.document-detail-tabs').children('.active')).toHaveLength(1);
                    expect(this.view.$('.document-detail-tabs').children()).toHaveLength(2);
                });

                it('should render 2 tab contents, with only 1 active', function () {
                    expect(this.view.$('.tab-content-view-container.active')).toHaveLength(1);
                    expect(this.view.$('.tab-content-view-container')).toHaveLength(2);
                });
            });
        });

        describe('when requirements for a tab to be rendered are not met', function() {
            beforeEach(function() {
                this.view = new DocumentDetailView({
                    model: new DocumentModel({
                        reference: 'some other reference'
                    }),
                    indexesCollection: new Backbone.Collection()
                });

                this.view.tabs = this.view.filterTabs(getMockTabs(1));
                this.view.render();
            });

            it('should render no tab headers', function () {
                expect(this.view.$('.document-detail-tabs').children()).toHaveLength(0);
            });

            it('should render no tab contents', function () {
                expect(this.view.$('.tab-content-view-container.active')).toHaveLength(0);
            });
        });

        describe('when the view renders with a media document', function() {
            beforeEach(function() {
                this.view = new DocumentDetailView({
                    model: new DocumentModel({
                        reference: DOCUMENT_MODEL_REF,
                        media: 'audio',
                        url: 'www.example.com'
                    }),
                    indexesCollection: new Backbone.Collection()
                });
                this.view.render();
            });

            it('should render a media player', function () {
                expect(this.view.$('.document-detail-view-container audio')).toHaveLength(1);
            });
        });

        describe('when the view renders with a non-media document', function() {
            beforeEach(function() {
                this.view = new DocumentDetailView({
                    model: new DocumentModel({
                        reference: DOCUMENT_MODEL_REF,
                        url: ANY_OLD_URL
                    }),
                    indexesCollection: new Backbone.Collection()
                });
                this.view.render();
            });

            it('should render a document viewing iframe', function () {
                expect(this.view.$('.document-detail-view-container .preview-document-frame')).toHaveLength(1);
            });
        });

            describe('when the view renders and the document has a url', function() {
                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: DOCUMENT_MODEL_REF,
                            url: ANY_OLD_URL
                        }),
                        indexesCollection: new Backbone.Collection()
                    });
                    this.view.render();
                });

                it('should render an open original button with the correct href', function () {
                    expect(this.view.$('.document-detail-open-original-link')).toHaveLength(1);
                    expect(this.view.$('.document-detail-open-original-link')).toHaveAttr('href', ANY_OLD_URL);
                });
            });

            describe('when the view renders and the document has no url but a reference that could be a url', function() {
                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: URL_LIKE_REFERENCE
                        }),
                        indexesCollection: new Backbone.Collection()
                    });
                    this.view.render();
                });

                it('should render an open original button with the correct href', function () {
                    expect(this.view.$('.document-detail-open-original-link')).toHaveLength(1);
                    expect(this.view.$('.document-detail-open-original-link')).toHaveAttr('href', URL_LIKE_REFERENCE);
                });
            });

            describe('when the view renders but the document has no url or url-like reference', function() {
                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: DOCUMENT_MODEL_REF
                        }),
                        indexesCollection: new Backbone.Collection()
                    });
                    this.view.render();
                });

                it('should not render the open original button', function () {
                    expect(this.view.$('.document-detail-open-original-link')).toHaveLength(0);
                });

                it('should not render the mmap link', function () {
                    expect(this.view.$('.document-detail-mmap-link')).toHaveLength(0);
                });
            });

            describe('when the view renders and the document has mmap references', function() {
                var mmapUrl = '/video/a-video';

                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: DOCUMENT_MODEL_REF,
                            mmapUrl: mmapUrl
                        }),
                        indexesCollection: new Backbone.Collection()
                    });
                    this.view.render();
                });

                it('should not render the open original button', function () {
                    expect(this.view.$('.document-detail-open-original-link')).toHaveLength(0);
                });

                it('should render the open mmap button', function () {
                    expect(this.view.$('.document-detail-mmap-link')).toHaveLength(1);
                    expect(this.view.$('.document-detail-mmap-link')).toHaveAttr('href', ANY_OLD_URL + mmapUrl);
                });
            });
    });
});
