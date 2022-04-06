/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/page/search/results/users-view'
], function($, Backbone, i18n, UsersView) {
    'use strict';

    const DETAILS_FIELDS = [
        { name: 'field_1' },
        { name: 'field_2' },
        { name: 'field_3' },
    ];

    const expectLoading = function () {
        it('should display loading spinner', function () {
            expect(this.view.$('.users-loading')).not.toHaveClass('hide');
            expect(this.view.$('.users-error')).toHaveClass('hide');
            expect(this.view.$('.users-empty')).toHaveClass('hide');
            expect(this.view.$('.users-noquery')).toHaveClass('hide');
            expect(this.view.$('.users-list')).toHaveClass('hide');
        });
    }

    const expectError = function () {
        it('should display error', function () {
            expect(this.view.$('.users-loading')).toHaveClass('hide');
            expect(this.view.$('.users-error')).not.toHaveClass('hide');
            expect(this.view.$('.users-empty')).toHaveClass('hide');
            expect(this.view.$('.users-noquery')).toHaveClass('hide');
            expect(this.view.$('.users-list')).toHaveClass('hide');
        });
    }

    const expectEmpty = function () {
        it('should display empty message', function () {
            expect(this.view.$('.users-loading')).toHaveClass('hide');
            expect(this.view.$('.users-error')).toHaveClass('hide');
            expect(this.view.$('.users-empty')).not.toHaveClass('hide');
            expect(this.view.$('.users-noquery')).toHaveClass('hide');
            expect(this.view.$('.users-list')).toHaveClass('hide');
        });
    }

    const expectUsers = function () {
        it('should display users', function () {
            expect(this.view.$('.users-loading')).toHaveClass('hide');
            expect(this.view.$('.users-error')).toHaveClass('hide');
            expect(this.view.$('.users-empty')).toHaveClass('hide');
            expect(this.view.$('.users-noquery')).toHaveClass('hide');
            expect(this.view.$('.users-list')).not.toHaveClass('hide');
        });
    }

    describe('Users View', function () {

        beforeEach(function () {
            this.usersCollection = new Backbone.Collection();
            this.previewModeModel = new Backbone.Model({ mode: null });
            this.getUserDetailsFields = jasmine.createSpy().and.returnValue(DETAILS_FIELDS);
            this.view = new UsersView({
                previewModeModel: this.previewModeModel,
                usersCollection: this.usersCollection,
                getUserDetailsFields: this.getUserDetailsFields
            });

            $('body').append(this.view.$el);
            this.view.render();
        });

        afterEach(function () {
            this.view.remove();
        })

        expectLoading();

        describe('when the collection syncs with no users', function () {

            beforeEach(function () {
                this.usersCollection.trigger('sync');
            });

            expectEmpty();

        });

        describe('when the collection syncs with users', function () {

            beforeEach(function () {
                this.usersCollection.add([
                    { uid: 5, username: 'user A' },
                    { uid: 8, username: 'user B', emailaddress: 'b@example.com' }
                ]);
                this.usersCollection.trigger('sync');
            });

            expectUsers();

            it('users list should contain users', function () {
                const $users = this.view.$('[data-uid]');
                expect($users.size()).toBe(2);
                expect($users.eq(0).data('uid')).toBe(5);
                expect($users.eq(0).find('h4').text()).toBe('user A');
                expect($users.eq(0).find('.document-reference').text()).toBe('');
                expect($users.eq(1).data('uid')).toBe(8);
                expect($users.eq(1).find('h4').text()).toBe('user B');
                expect($users.eq(1).find('.document-reference').text()).toBe('b@example.com');
            });

            describe('then a user is clicked', function () {

                beforeEach(function () {
                    this.view.$('[data-uid]').eq(1).click();
                });

                expectUsers();

                it('should show the user details', function () {
                    const userAttrs = { uid: 8, username: 'user B', emailaddress: 'b@example.com' };
                    expect(this.getUserDetailsFields.calls.count()).toBe(1);
                    expect(this.getUserDetailsFields.calls.argsFor(0)[0].toJSON())
                        .toEqual(userAttrs);

                    expect(this.previewModeModel.get('mode')).toBe('user');
                    expect(this.previewModeModel.get('user').toJSON()).toEqual(userAttrs);
                    expect(this.previewModeModel.get('fields')).toEqual(DETAILS_FIELDS);
                });

                it('should highlight the clicked user', function () {
                    const users = this.view.$('[data-uid]');
                    expect(users.eq(0).hasClass('selected-document')).toBe(false);
                    expect(users.eq(1).hasClass('selected-document')).toBe(true);
                });

                describe('then a different user is clicked', function () {

                    beforeEach(function () {
                        this.view.$('[data-uid]').eq(0).click();
                    });

                    expectUsers();

                    it('should show the user details', function () {
                        expect(this.previewModeModel.get('user').toJSON())
                            .toEqual({ uid: 5, username: 'user A' });
                    });

                    it('should highlight the clicked user', function () {
                        const users = this.view.$('[data-uid]');
                        expect(users.eq(0).hasClass('selected-document')).toBe(true);
                        expect(users.eq(1).hasClass('selected-document')).toBe(false);
                    });

                });

                describe('then the user is clicked again', function () {

                    beforeEach(function () {
                        this.view.$('[data-uid]').eq(1).click();
                    });

                    expectUsers();

                    it('should hide the user details', function () {
                        expect(this.previewModeModel.get('mode')).toBe(null);
                    });

                    it('should de-highlight the clicked user', function () {
                        const users = this.view.$('[data-uid]');
                        expect(users.eq(0).hasClass('selected-document')).toBe(false);
                        expect(users.eq(1).hasClass('selected-document')).toBe(false);
                    });

                });

            });

        });

        describe('when the collection sync fails', function () {

            beforeEach(function () {
                this.usersCollection.trigger('error', null, {
                    status: 1,
                    responseJSON: { message: 'bad things', uuid: '123', isUserError: false }
                });
            });

            expectError();

            it('should show the error message', function () {
                expect(this.view.$('.users-error').html())
                    .toContain(i18n['search.resultsView.users.error.fetchUsers']);
            })

        });

        // eg. new fetch started
        describe('when the collection sync is aborted', function () {

            beforeEach(function () {
                this.usersCollection.trigger('error', null, {
                    status: 0,
                    statusText: 'abort'
                });
            });

            expectLoading();

        });

        // eg. backend inaccessible
        describe('when the collection sync fails with no response', function () {

            beforeEach(function () {
                this.view.usersCollection.trigger('error', null, { status: 1 });
            });

            expectError();

            it('should show the error message', function () {
                expect(this.view.$('.users-error').html())
                    .toContain(i18n['search.resultsView.users.error.fetchUsers']);
            })

        });

        // methods for use by subclasses

        describe('showLoading', function () {

            beforeEach(function () {
                this.view.showLoading();
            })

            expectLoading();

        });

        describe('showNoQuery', function () {

            beforeEach(function () {
                this.view.showNoQuery();
            })

            it('should display no-query message', function () {
                expect(this.view.$('.users-loading')).toHaveClass('hide');
                expect(this.view.$('.users-error')).toHaveClass('hide');
                expect(this.view.$('.users-empty')).toHaveClass('hide');
                expect(this.view.$('.users-noquery')).not.toHaveClass('hide');
                expect(this.view.$('.users-list')).toHaveClass('hide');
            });

        });

    });

});
