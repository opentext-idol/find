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
    'find/app/router'
], function(Backbone, $, DocumentDetailView, TabContentView, DocumentModel, router) {

    var BACK_URL = 'find/search/goback';
    var DOCUMENT_MODEL_REF = 'reference';

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
        describe('when the back button is clicked', function() {
            beforeEach(function() {
                this.view = new DocumentDetailView({
                    model: new DocumentModel(),
                    backUrl: BACK_URL
                });

                spyOn(router, 'navigate');

                this.view.render();
                this.view.$('.detail-view-back-button').click();
            });


            it('calls router.navigate with the correct URL', function () {
                expect(router.navigate).toHaveBeenCalledWith(BACK_URL, jasmine.any(Object));
            });
        });

        describe('when requirements for a tab to be rendered are met', function() {
            describe('when the view renders with a single tab defined', function() {
                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: DOCUMENT_MODEL_REF
                        })
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
                        })
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
                    })
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
                    })
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
                        url: 'www.example.com'
                    })
                });
                this.view.render();
            });

            it('should render a document viewing iframe', function () {
                expect(this.view.$('.document-detail-view-container .preview-document-frame')).toHaveLength(1);
            });
        });

        describe('when the open original button is clicked', function() {
            describe('when the document has a url', function() {
                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: DOCUMENT_MODEL_REF,
                            url: 'www.example.com'
                        })
                    });

                    spyOn(window, 'open');

                    this.view.render();
                    this.view.$('.detail-view-open-button').click();
                });

                it('should call window.open', function () {
                    expect(window.open).toHaveBeenCalled();
                });
            });

            describe('when the document has no url but a reference that could be a url', function() {
                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: 'http://www.example.com'
                        })
                    });

                    spyOn(window, 'open');

                    this.view.render();
                    this.view.$('.detail-view-open-button').click();
                });

                it('should call window.open', function () {
                    expect(window.open).toHaveBeenCalled();
                });
            });

            describe('when the document has no url or url-like reference', function() {
                beforeEach(function() {
                    this.view = new DocumentDetailView({
                        model: new DocumentModel({
                            reference: DOCUMENT_MODEL_REF
                        })
                    });
                    this.view.render();
                    this.view.$('.detail-view-open-button').click();
                });

                it('should not render the open original button', function () {
                    expect(this.view.$('.detail-view-open-button')).toHaveLength(0);
                });
            });
        });
    });
});
