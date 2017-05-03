/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'find/app/configuration',
    'find/app/page/search/document/document-detail-view',
    'find/app/page/search/document/tab-content-view',
    'mock/model/document-model',
    'find/app/vent'
], function(Backbone, $, _, configuration, DocumentDetailView, TabContentView, MockDocumentModel, vent) {
    'use strict';

    const BACK_URL = 'search/goback';
    const DATABASE = 'Wikipedia';
    const REFERENCE = '099a5ab5-94ee-4cfc-a142-9b3503c92282';
    const ERROR_MESSAGE = 'Document not found';

    const NO_MMAP = {
        supported: _.constant(false)
    };

    describe('DocumentDetailView', function() {
        beforeEach(function() {
            configuration.and.returnValue({
                enableRelatedConcepts: false,
                mmapBaseUrl: 'http://mmap.com',
                map: {enabled: false}
            });

            this.view = new DocumentDetailView({
                backUrl: BACK_URL,
                database: DATABASE,
                indexesCollection: new Backbone.Collection(),
                mmapTab: NO_MMAP,
                reference: REFERENCE
            });

            this.documentModel = MockDocumentModel.instances[0];

            this.view.render();
        });

        afterEach(function() {
            this.view.remove();
            MockDocumentModel.reset();
            vent.navigateToDetailRoute.calls.reset();
            vent.navigate.calls.reset();
        });

        it('fetches document content on construction', function() {
            expect(this.documentModel.fetch.calls.count()).toBe(1);
        });

        it('displays the loading spinner', function() {
            expect(this.view.$('.document-detail-loading')).not.toHaveClass('hide');
        });

        it('hides the error message', function() {
            expect(this.view.$('.document-detail-error')).toHaveClass('hide');
        });

        it('hides the content', function() {
            expect(this.view.$('.document-detail-content')).toHaveClass('hide');
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
                const $error = this.view.$('.document-detail-error');
                expect($error).not.toHaveClass('hide');
                expect($error).toContainText(ERROR_MESSAGE);
            });

            it('hides the loading spinner', function() {
                expect(this.view.$('.document-detail-loading')).toHaveClass('hide');
            });

            it('hides the content', function() {
                expect(this.view.$('.document-detail-content')).toHaveClass('hide');
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
                expect(this.view.$('.document-detail-content')).not.toHaveClass('hide');
            });

            it('hides the error message', function() {
                expect(this.view.$('.document-detail-error')).toHaveClass('hide');
            });

            it('hides the loading spinner', function() {
                expect(this.view.$('.document-detail-loading')).toHaveClass('hide');
            });
        });
    });
});
