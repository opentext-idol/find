/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/saved-searches/search-title-input',
    'find/app/model/saved-searches/saved-search-model',
    'i18n!find/nls/bundle',
    'backbone'
], function(SearchTitleInput, SavedSearchModel, i18n, Backbone) {

    describe('Search title input', function() {
        var INITIAL_TITLE = 'Initial Title';
        var INITIAL_SEARCH_TYPE = SavedSearchModel.Type.SNAPSHOT;

        beforeEach(function() {
            this.saveCallback = jasmine.createSpy('saveCallback');
        });

        describe('for a new saved search model', function() {
            beforeEach(function() {
                this.savedSearchModel = new Backbone.Model({
                    title: INITIAL_TITLE,
                    type: INITIAL_SEARCH_TYPE
                });

                this.view = new SearchTitleInput({
                    saveCallback: this.saveCallback,
                    savedSearchModel: this.savedSearchModel,
                    savedSearchCollection: new Backbone.Collection()
                });

                this.view.render();
            });

            it('displays no title', function() {
                expect(this.view.$('.search-title-input')).toHaveValue('');
            });

            describe('if the save button is clicked', function () {
                beforeEach(function() {
                    this.view.$('.save-title-confirm-button').click();
                });

                it('should not be possible to save without a title', function () {
                    expect(this.saveCallback).not.toHaveBeenCalled();
                });
            });

            describe('when the user adds a title', function() {
                var NEW_TITLE = 'My new title';

                beforeEach(function() {
                    this.view.$('.search-title-input').val(NEW_TITLE).trigger('input');
                });

                it('should enable the confirm button', function() {
                    expect(this.view.$('.save-title-confirm-button')).not.toHaveClass('disabled');
                });

                describe('and clicks save', function() {
                    beforeEach(function() {
                        this.view.$el.submit();
                    });

                    it('should call the save callback with the title, a success callback and an error callback', function() {
                        expect(this.saveCallback).toHaveBeenCalled();

                        expect(this.saveCallback.calls.argsFor(0)).toEqual([{
                            title: NEW_TITLE
                        }, jasmine.any(Function), jasmine.any(Function)]);
                    });
                });
            });
        });

        describe('with an existing saved search model', function() {
            beforeEach(function() {
                this.savedSearchModel = new Backbone.Model({
                    id: 1,
                    title: INITIAL_TITLE,
                    type: INITIAL_SEARCH_TYPE
                });

                this.view = new SearchTitleInput({
                    savedSearchModel: this.savedSearchModel,
                    saveCallback: this.saveCallback,
                    savedSearchCollection: new Backbone.Collection()
                });

                this.listener = jasmine.createSpy('listener');
                this.view.on('remove', this.listener);

                this.view.render();
            });

            it('should display the initial title', function() {
                expect(this.view.$('.search-title-input')).toHaveValue(INITIAL_TITLE);
            });

            describe('when the cancel button is clicked', function() {
                beforeEach(function() {
                    this.view.$('.save-title-cancel-button').click();
                });

                it('should fire a "remove" event', function() {
                    expect(this.listener.calls.count()).toBe(1);
                });

                it('should not call the save callback', function() {
                    expect(this.saveCallback).not.toHaveBeenCalled();
                });
            });

            describe('when the title is blank', function() {
                beforeEach(function() {
                    this.view.$('.search-title-input').val('').trigger('input');
                });

                it('should disable the confirm button', function() {
                    expect(this.view.$('.save-title-confirm-button')).toHaveClass('disabled');
                });
            });

            describe('when the title has not changed', function() {
                beforeEach(function() {
                    var savedTitle = 'My Search';
                    this.savedSearchModel.set('title', savedTitle);
                    this.view.$('.search-title-input').val(savedTitle).trigger('input');
                });

                it('should disable the confirm button', function() {
                    expect(this.view.$('.save-title-confirm-button')).toHaveClass('disabled');
                });
            });

            describe('when there is a new title', function() {
                var NEW_TITLE = 'My Search';

                beforeEach(function () {
                    this.view.$('.search-title-input').val('  ' + NEW_TITLE).trigger('input');
                });

                it('should enable the confirm button', function() {
                    expect(this.view.$('.save-title-confirm-button')).not.toHaveClass('disabled');
                });

                describe('and the save button is clicked', function () {
                    beforeEach(function() {
                        this.view.$('.save-title-confirm-button').click();
                    });

                    it('should not fire a "remove" event', function () {
                        expect(this.listener).not.toHaveBeenCalled();
                    });

                    it('should call the save callback with the trimmed title, a success and an error callback', function () {
                        expect(this.saveCallback).toHaveBeenCalled();

                        expect(this.saveCallback.calls.argsFor(0)).toEqual([{
                            title: NEW_TITLE
                        }, jasmine.any(Function), jasmine.any(Function)]);
                    });

                    it('should disable the input', function () {
                        expect(this.view.$('.search-title-input')).toHaveProp('disabled', true);
                    });

                    it('should disable the save button', function () {
                        expect(this.view.$('.save-title-confirm-button')).toHaveProp('disabled', true);
                    });

                    it('should disable the cancel button', function () {
                        expect(this.view.$('.save-title-cancel-button')).toHaveProp('disabled', true);
                    });

                    describe('then the save fails', function () {
                        beforeEach(function () {
                            this.saveCallback.calls.argsFor(0)[2]({status: 404}, {statusText: ''});
                        });

                        it('enables the input', function () {
                            expect(this.view.$('.search-title-input')).toHaveProp('disabled', false);
                        });

                        it('enables the save button', function () {
                            expect(this.view.$('.save-title-confirm-button')).toHaveProp('disabled', false);
                        });

                        it('enables the cancel button', function () {
                            expect(this.view.$('.save-title-cancel-button')).toHaveProp('disabled', false);
                        });

                        it('displays an error message', function () {
                            var $errorMessage = this.view.$('.search-title-error-message');
                            expect($errorMessage).toHaveText(i18n['search.savedSearchControl.error']);
                            expect($errorMessage).not.toHaveClass('hide');
                        });

                        it('does not fire a "remove" event', function () {
                            expect(this.listener).not.toHaveBeenCalled();
                        });
                    });

                    describe('then the save succeeds', function () {
                        beforeEach(function () {
                            this.saveCallback.calls.argsFor(0)[1]();
                        });

                        it('fires a "remove" event', function () {
                            expect(this.listener).toHaveBeenCalled();
                        });
                    });
                });
            });
        });
    });

});
