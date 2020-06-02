/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/filters/documentselection/document-selection-view',
    'find/app/model/document-selection-model'
], function (Backbone, DocumentSelectionView, DocumentSelectionModel) {

    describe('DocumentSelectionView', function () {

        beforeEach(function () {
            this.documentSelectionModel = new DocumentSelectionModel();
            this.savedSearchModel = new Backbone.Model();
            this.view = new DocumentSelectionView({
                documentSelectionModel: this.documentSelectionModel,
                savedSearchModel: this.savedSearchModel
            });
        });

        it('should not render', function () {
            expect(this.view.$el.html()).toBe('');
        });

        describe('when the saved search model syncs', function () {

            beforeEach(function () {
                this.savedSearchModel.trigger('sync');
            });

            it('should show the Exclude All button', function () {
                expect(this.view.$el.html()).toContain('Exclude All');
                expect(this.view.$el.html()).not.toContain('Select All');
            });

            describe('then a document is excluded', function () {

                beforeEach(function () {
                    this.documentSelectionModel.exclude('ref1');
                });

                it('should show the Select All button', function () {
                    expect(this.view.$el.html()).not.toContain('Exclude All');
                    expect(this.view.$el.html()).toContain('Select All');
                });

                describe('then the Select All button is clicked', function () {

                    beforeEach(function () {
                        this.view.$('.toggle-all-button').click();
                    });

                    it('should select all documents', function () {
                        expect(this.documentSelectionModel.get('isWhitelist')).toBe(false);
                        expect(this.documentSelectionModel.get('references')).toEqual({});
                    });

                });

            });

            describe('then the Exclude All button is clicked', function () {

                beforeEach(function () {
                    this.view.$('.toggle-all-button').click();
                });

                it('should exclude all documents', function () {
                    expect(this.documentSelectionModel.get('isWhitelist')).toBe(true);
                    expect(this.documentSelectionModel.get('references')).toEqual({});
                });

                it('should show the Select All button', function () {
                    expect(this.view.$el.html()).not.toContain('Exclude All');
                    expect(this.view.$el.html()).toContain('Select All');
                });

            });

        });

    });

});
