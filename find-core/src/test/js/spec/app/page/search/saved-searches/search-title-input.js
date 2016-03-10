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

        describe('for a new saved search model with showSearchTypes true', function() {
            beforeEach(function() {
                this.savedSearchModel = new Backbone.Model({
                    title: INITIAL_TITLE,
                    type: INITIAL_SEARCH_TYPE
                });

                this.view = new SearchTitleInput({
                    saveCallback: this.saveCallback,
                    showSearchTypes: true,
                    savedSearchModel: this.savedSearchModel,
                    savedSearchCollection: new Backbone.Collection()
                });

                this.view.render();
            });

            it('displays no title', function() {
                expect(this.view.$('.search-title-input')).toHaveValue('');
            });

            it('shows the search types', function() {
                expect(this.view.$('[name="saved-search-type"]')).toHaveLength(2);
            });

            describe('when the user adds a title, selects a type', function() {
                var NEW_TITLE = 'My new title';
                var NEW_TYPE = SavedSearchModel.Type.QUERY;

                beforeEach(function() {
                    this.view.$('.search-title-input').val(NEW_TITLE).trigger('input');
                    this.view.$('[value="' + NEW_TYPE + '"]').trigger('ifChecked');
                });

                it('the confirm button is enabled', function() {
                    expect(this.view.$('.save-title-confirm-button')).not.toHaveClass('disabled');
                });

                describe('and clicks save', function() {
                    beforeEach(function() {
                        this.view.$el.submit();
                    });

                    it('calls the save callback with the title and type, a success callback and an error callback', function() {
                        expect(this.saveCallback).toHaveBeenCalled();

                        expect(this.saveCallback.calls.argsFor(0)).toEqual([{
                            title: NEW_TITLE,
                            type: NEW_TYPE
                        }, jasmine.any(Function), jasmine.any(Function)]);
                    });
                });
            });
        });

        describe('with an existing saved search model with showSearchTypes false', function() {
            beforeEach(function() {
                this.savedSearchModel = new Backbone.Model({
                    id: 1,
                    title: INITIAL_TITLE,
                    type: INITIAL_SEARCH_TYPE
                });

                this.view = new SearchTitleInput({
                    savedSearchModel: this.savedSearchModel,
                    saveCallback: this.saveCallback,
                    showSearchTypes: false,
                    savedSearchCollection: new Backbone.Collection()
                });

                this.listener = jasmine.createSpy('listener');
                this.view.on('remove', this.listener);

                this.view.render();
            });

            it('displays the initial title', function() {
                expect(this.view.$('.search-title-input')).toHaveValue(INITIAL_TITLE);
            });

            it('does not show the search types', function() {
                expect(this.view.$('[name="saved-search-type"]')).toHaveLength(0);
            });

            describe('when the cancel button is clicked', function() {
                beforeEach(function() {
                    this.view.$('.save-title-cancel-button').click();
                });

                it('fires a "remove" event', function() {
                    expect(this.listener.calls.count()).toBe(1);
                });

                it('does not call the save callback', function() {
                    expect(this.saveCallback).not.toHaveBeenCalled();
                });
            });

            describe('when the title is blank', function() {
                beforeEach(function() {
                    this.view.$('.search-title-input').val('').trigger('input');
                });

                it('the confirm button is disabled', function() {
                    expect(this.view.$('.save-title-confirm-button')).toHaveClass('disabled');
                });
            });

            describe('when the title has not changed', function() {
                beforeEach(function() {
                    var savedTitle = 'My Search';
                    this.savedSearchModel.set('title', savedTitle);
                    this.view.$('.search-title-input').val(savedTitle).trigger('input');
                });

                it('the confirm button is disabled', function() {
                    expect(this.view.$('.save-title-confirm-button')).toHaveClass('disabled');
                });
            });

            describe('when there is a new title', function() {
                var NEW_TITLE = 'My Search';

                beforeEach(function () {
                    this.view.$('.search-title-input').val('  ' + NEW_TITLE).trigger('input');
                });

                it('the confirm button is enabled', function() {
                    expect(this.view.$('.save-title-confirm-button')).not.toHaveClass('disabled');
                });

                describe('and the save button is clicked', function () {
                    beforeEach(function() {
                        this.view.$('.save-title-confirm-button').click();
                    });

                    it('does not fire a "remove" event', function () {
                        expect(this.listener).not.toHaveBeenCalled();
                    });

                    it('calls the save callback with the trimmed title and type, a success and an error callback', function () {
                        expect(this.saveCallback).toHaveBeenCalled();

                        expect(this.saveCallback.calls.argsFor(0)).toEqual([{
                            title: NEW_TITLE,
                            type: INITIAL_SEARCH_TYPE
                        }, jasmine.any(Function), jasmine.any(Function)]);
                    });

                    it('disables the input', function () {
                        expect(this.view.$('.search-title-input')).toHaveProp('disabled', true);
                    });

                    it('disables the save button', function () {
                        expect(this.view.$('.save-title-confirm-button')).toHaveProp('disabled', true);
                    });

                    it('disables the cancel button', function () {
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
