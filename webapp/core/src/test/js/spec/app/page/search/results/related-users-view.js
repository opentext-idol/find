/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/app/page/search/results/related-users-view'
], function($, Backbone, RelatedUsersView) {
    'use strict';

    const expectLoading = function () {
        it('should display loading spinner', function () {
            expect(this.view.$('.users-loading')).not.toHaveClass('hide');
            expect(this.view.$('.users-error')).toHaveClass('hide');
            expect(this.view.$('.users-empty')).toHaveClass('hide');
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

    describe('Related Users View', function () {

        beforeEach(function () {
            this.queryModel = new Backbone.Model({ indexes: ['db'], queryText: 'the search' });

            this.view = new RelatedUsersView({
                queryModel: this.queryModel,
                config: {
                    interests: { userDetailsFields: [
                        { name: 'common' }, { name: 'int' }
                    ] },
                    expertise: { userDetailsFields: [
                        { name: 'common' }, { name: 'exp' }
                    ] }
                },
            });

            spyOn(this.view.relatedUsersCollection, 'fetch').and.returnValue($.when());

            $('body').append(this.view.$el);
            this.view.render();
        });

        afterEach(function () {
            this.view.remove();
        })

        expectLoading();

        it('should fetch users', function () {
            const calls = this.view.relatedUsersCollection.fetch.calls;
            expect(calls.count()).toBe(1);
            expect(calls.mostRecent().args[0].data.searchText).toBe('the search');
        });

        describe('when the collection syncs', function () {

            beforeEach(function () {
                this.view.relatedUsersCollection.add([{ uid: 123, username: 'user' }]);
                this.view.relatedUsersCollection.trigger('sync');
            });

            expectUsers();

            describe('then the query text changes', function () {

                beforeEach(function () {
                    this.queryModel.set('queryText', 'new search');
                });

                expectLoading();

                it('should fetch users again', function () {
                    const calls = this.view.relatedUsersCollection.fetch.calls;
                    expect(calls.count()).toBe(2);
                    expect(calls.mostRecent().args[0].reset).toBe(true);
                    expect(calls.mostRecent().args[0].data.searchText).toBe('new search');
                });

                describe('then the query text changes back to the previous value', function () {

                    beforeEach(function () {
                        this.queryModel.set('queryText', 'new search');
                    });

                    // should fetch again
                    expectLoading();

                });

            });

            describe('then the indexes change', function () {

                beforeEach(function () {
                    this.queryModel.set('indexes', ['new']);
                });

                expectUsers();

                it('should not fetch users again', function () {
                    expect(this.view.relatedUsersCollection.fetch.calls.count()).toBe(1);
                });

            });

        });

        describe('getUserDetailsFields', function () {

            it('should return interests config for non-expert user', function () {
                const userModel = new Backbone.Model({ uid: 123, username: 'user', expert: false });
                const fields = this.view.getUserDetailsFields(userModel);
                expect(fields).toEqual([{ name: 'common' }, { name: 'int' }]);
            });

            it('should return expertise config for expert user', function () {
                const userModel = new Backbone.Model({ uid: 123, username: 'user', expert: true });
                const fields = this.view.getUserDetailsFields(userModel);
                expect(fields).toEqual([{ name: 'common' }, { name: 'exp' }]);
            });

        });

    });

});
