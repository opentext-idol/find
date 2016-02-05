/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/saved-searches/search-title-input',
    'i18n!find/nls/bundle',
    'backbone',
    'underscore',
    'jquery'
], function(SearchTitleInput, i18n, Backbone, _, $) {

    describe('Search title input', function() {
        var INITIAL_TITLE = 'Initial Title';

        describe('for a new saved search model', function() {
            beforeEach(function() {
                this.savedSearchModel = new Backbone.Model({
                    title: INITIAL_TITLE
                });

                this.view = new SearchTitleInput({
                    savedSearchModel: this.savedSearchModel
                });

                this.view.render();
            });

            it('displays no title', function() {
                expect(this.view.$('.search-title-input')).toHaveValue('');
            });
        });

        describe('with an existing saved search model', function() {
            beforeEach(function() {
                this.saveCallback = jasmine.createSpy('saveCallback');

                this.savedSearchModel = new Backbone.Model({
                    id: 1,
                    title: INITIAL_TITLE
                });

                this.view = new SearchTitleInput({
                    savedSearchModel: this.savedSearchModel,
                    saveCallback: this.saveCallback
                });

                this.listener = jasmine.createSpy('listener');
                this.view.on('remove', this.listener);

                this.view.render();
            });

            it('displays the initial title', function() {
                expect(this.view.$('.search-title-input')).toHaveValue(INITIAL_TITLE);
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

            describe('when the save button is clicked with a blank title', function() {
                beforeEach(function() {
                    this.view.$('.search-title-input').val('').trigger('input');
                    this.view.$('.save-title-confirm-button').click();
                });

                it('displays an error message', function() {
                    var $errorMessage = this.view.$('.search-title-error-message');
                    expect($errorMessage).toHaveText(i18n['search.savedSearchControl.titleBlank']);
                    expect($errorMessage).not.toHaveClass('hide');
                });

                it('does not call the save callback', function() {
                    expect(this.saveCallback).not.toHaveBeenCalled();
                });
            });

            describe('when the save button is clicked when the title has not changed', function() {
                beforeEach(function() {
                    var savedTitle = 'My Search';
                    this.savedSearchModel.set('title', savedTitle);
                    this.view.$('.search-title-input').val(savedTitle).trigger('input');
                    this.view.$('.save-title-confirm-button').click();
                });

                it('fires a "remove" event', function() {
                    expect(this.listener.calls.count()).toBe(1);
                });

                it('does not call the save callback', function() {
                    expect(this.saveCallback).not.toHaveBeenCalled();
                });
            });

            describe('when the save button is clicked when there is a new title', function() {
                var NEW_TITLE = 'My Search';
                var NEW_TYPE = 'query';

                beforeEach(function() {
                    this.view.$('.search-title-input').val('  ' + NEW_TITLE).trigger('input');
                    this.view.$('.save-title-confirm-button').click();
                });

                it('does not fire a "remove" event', function() {
                    expect(this.listener).not.toHaveBeenCalled();
                });

                it('calls the save callback with the trimmed title, a success and an error callback', function() {
                    expect(this.saveCallback).toHaveBeenCalled();
                    expect(this.saveCallback.calls.argsFor(0)).toEqual([{title: NEW_TITLE, type: NEW_TYPE}, jasmine.any(Function), jasmine.any(Function)]);
                });

                it('disables the input', function() {
                    expect(this.view.$('.search-title-input')).toHaveProp('disabled', true);
                });

                it('disables the save button', function() {
                    expect(this.view.$('.save-title-confirm-button')).toHaveProp('disabled', true);
                });

                it('disables the cancel button', function() {
                    expect(this.view.$('.save-title-cancel-button')).toHaveProp('disabled', true);
                });

                describe('then the save fails', function() {
                    beforeEach(function() {
                        this.saveCallback.calls.argsFor(0)[2]({status: 404});
                    });

                    it('enables the input', function() {
                        expect(this.view.$('.search-title-input')).toHaveProp('disabled', false);
                    });

                    it('enables the save button', function() {
                        expect(this.view.$('.save-title-confirm-button')).toHaveProp('disabled', false);
                    });

                    it('enables the cancel button', function() {
                        expect(this.view.$('.save-title-cancel-button')).toHaveProp('disabled', false);
                    });

                    it('displays an error message', function() {
                        var $errorMessage = this.view.$('.search-title-error-message');
                        expect($errorMessage).toHaveText(i18n['search.savedSearchControl.error']);
                        expect($errorMessage).not.toHaveClass('hide');
                    });

                    it('does not fire a "remove" event', function() {
                        expect(this.listener).not.toHaveBeenCalled();
                    });
                });

                describe('then the save succeeds', function() {
                    beforeEach(function() {
                        this.saveCallback.calls.argsFor(0)[1]();
                    });

                    it('fires a "remove" event', function() {
                        expect(this.listener).toHaveBeenCalled();
                    });
                });
            });
        });
    });

});
