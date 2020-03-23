/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/app/page/search/document/preview-mode-fact-view'
], function($, Backbone, PreviewModeFactView) {
    'use strict';

    const FACTS_DETAIL_HTML = '' +
        '<a class="close-preview-mode">close preview</a>' +
        '<span class="document-url">' + (
            '<a data-docindex="clicked-docdb" data-docref="clicked-docref">open document</a>'
        ) + '</span>';

    describe('Preview Mode Fact View', function () {

        beforeEach(function () {
            this.factsView = {
                previewDoc: jasmine.createSpy()
            };
            this.previewModeModel = new Backbone.Model({ mode: 'fact' });
            this.documentRenderer = {
                renderEntityFactsDetail: jasmine.createSpy().and.returnValue(FACTS_DETAIL_HTML)
            };

            this.view = new PreviewModeFactView({
                model: new Backbone.Model({ the: 'fact' }),
                factsView: this.factsView,
                previewModeModel: this.previewModeModel,
                documentRenderer: this.documentRenderer
            });

            $('body').append(this.view.$el);
            this.view.render();
        });

        afterEach(function () {
            this.view.remove();
        })

        it('should render fact detail', function () {
            const calls = this.documentRenderer.renderEntityFactsDetail.calls;
            expect(calls.count()).toBe(1);
            expect(calls.mostRecent().args.length).toEqual(1);
            expect(calls.mostRecent().args[0].get('facts')).toEqual([{ the: 'fact' }]);
        });

        it('should display fact detail', function () {
            expect(this.view.$('.preview-mode-fact-container').html()).toBe(FACTS_DETAIL_HTML);
            expect(this.view.$('.preview-mode-fact-container')).not.toHaveClass('hide');
        });

        it('should not display loading spinner', function () {
            expect(this.view.$('.preview-mode-fact-loading')).toHaveClass('hide');
        });

        describe('then the Close button is clicked', function () {

            beforeEach(function () {
                this.view.$('.close-preview-mode').eq(0).click();
            });

            it('should set the preview mode to null', function () {
                expect(this.previewModeModel.get('mode')).toBe(null);
            });

        });

        describe('then a document is clicked', function () {

            beforeEach(function () {
                this.view.$('[data-docref]').eq(0).click();
            });

            it('should show the document preview', function () {
                const calls = this.factsView.previewDoc.calls;
                expect(calls.count()).toBe(1);
                expect(calls.mostRecent().args).toEqual(['clicked-docdb', 'clicked-docref']);
            });

            it('should display loading spinner', function () {
                expect(this.view.$('.preview-mode-fact-loading')).not.toHaveClass('hide');
            });

            it('should hide fact detail', function () {
                expect(this.view.$('.preview-mode-fact-container')).toHaveClass('hide');
            });

        });

    });

});
