/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'jquery',
    'underscore',
    'js-testing/backbone-mock-factory',
    'find/app/configuration',
    'find/app/page/search/document-content-view',
    'find/app/page/search/document/tab-content-view',
    'mock/model/document-model',
    'find/app/vent'
], function(Backbone, $, _, mockFactory, configuration, DocumentContentView, TabContentView, MockDocumentModel, vent) {
    'use strict';

    const BACK_URL = 'search/goback';
    const DATABASE = 'Wikipedia';
    const REFERENCE = '099a5ab5-94ee-4cfc-a142-9b3503c92282';
    const ERROR_MESSAGE = 'Document not found';

    const MockContentView = mockFactory.getView();

    describe('DocumentContentView', function() {
        beforeEach(function() {
            configuration.and.returnValue({
                enableRelatedConcepts: false,
                mmapBaseUrl: 'http://mmap.com',
                map: {enabled: false},
                ContentView: MockContentView
            });

            this.view = new DocumentContentView({
                backUrl: BACK_URL,
                database: DATABASE,
                reference: REFERENCE,
                ContentView: MockContentView,
                contentViewOptions: {
                    indexesCollection: new Backbone.Collection()
                }
            });

            this.documentModel = MockDocumentModel.instances[0];

            this.view.render();
        });

        afterEach(function() {
            this.view.remove();
            MockContentView.reset();
            MockDocumentModel.reset();
            vent.navigateToDetailRoute.calls.reset();
            vent.navigate.calls.reset();
        });

        it('fetches document content on construction', function() {
            expect(this.documentModel.fetch.calls.count()).toBe(1);
        });

        it('displays the loading spinner', function() {
            expect(this.view.$('.document-content-loading')).not.toHaveClass('hide');
        });

        it('hides the error message', function() {
            expect(this.view.$('.document-content-error')).toHaveClass('hide');
        });

        it('hides the content', function() {
            expect(this.view.$('.document-content-content')).toHaveClass('hide');
        });

        describe('when the back button is clicked', function() {
            beforeEach(function() {
                this.view.$('.detail-view-back-button').click();
            });

            it('calls vent.navigate with the correct URL', function() {
                expect(vent.navigate).toHaveBeenCalledWith(BACK_URL);
            });
        });

        describe('when the fetch fails', function() {
            beforeEach(function() {
                this.documentModel.fetch.promises[0].reject({
                    responseJSON: {
                        message: ERROR_MESSAGE
                    }
                });
            });

            it('displays the error message', function() {
                const $error = this.view.$('.document-content-error');
                expect($error).not.toHaveClass('hide');
                expect($error).toContainText(ERROR_MESSAGE);
            });

            it('hides the loading spinner', function() {
                expect(this.view.$('.document-content-loading')).toHaveClass('hide');
            });

            it('hides the content', function() {
                expect(this.view.$('.document-content-content')).toHaveClass('hide');
            });
        });

        describe('when the fetch succeeds', function() {
            beforeEach(function() {
                this.documentModel.set({
                    authors: [],
                    fields: []
                });

                this.documentModel.fetch.promises[0].resolve();
            });

            it('displays the content', function() {
                expect(this.view.$('.document-content-content')).not.toHaveClass('hide');
                expect(MockContentView.instances.length).toBe(1);
            });

            it('hides the error message', function() {
                expect(this.view.$('.document-content-error')).toHaveClass('hide');
            });

            it('hides the loading spinner', function() {
                expect(this.view.$('.document-content-loading')).toHaveClass('hide');
            });
        });
    });
});
