/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/document-selection-model',
    'find/app/util/policy-selection-view',
    'jasmine-ajax'
], function (
    $, Backbone, configuration, i18n, SavedSearchModel, DocumentSelectionModel, PolicySelectionView
) {
    'use strict';

    const expectLoading = function () {
        it('should display loading spinner', function () {
            expect(this.view.$('.loading-spinner')).toHaveLength(1);
            expect(this.view.$('.error-message-for-user')).toHaveLength(0);
            expect(this.view.$('.policy-empty')).toHaveLength(0);
            expect(this.view.$('.parametric-select')).toHaveLength(0);
        });
    }

    const expectError = function () {
        it('should display error', function () {
            expect(this.view.$('.loading-spinner')).toHaveLength(0);
            expect(this.view.$('.error-message-for-user')).toHaveLength(1);
            expect(this.view.$('.policy-empty')).toHaveLength(0);
            expect(this.view.$('.parametric-select')).toHaveLength(0);
        });
    }

    const expectEmpty = function () {
        it('should display empty message', function () {
            expect(this.view.$('.loading-spinner')).toHaveLength(0);
            expect(this.view.$('.error-message-for-user')).toHaveLength(0);
            expect(this.view.$('.policy-empty')).toHaveLength(1);
            expect(this.view.$('.parametric-select')).toHaveLength(0);
        });
    }

    const expectPolicies = function () {
        it('should display policies', function () {
            expect(this.view.$('.loading-spinner')).toHaveLength(0);
            expect(this.view.$('.error-message-for-user')).toHaveLength(0);
            expect(this.view.$('.policy-empty')).toHaveLength(0);
            expect(this.view.$('.parametric-select')).toHaveLength(1);
        });
    }

    describe('Policy Selection View', function () {

        beforeEach(function (done) {
            configuration.and.returnValue({});
            jasmine.Ajax.install();
            const queryState = {
                selectedIndexes: [],
                selectedParametricValues: [],
                conceptGroups: new Backbone.Collection([{ concepts: 'term-new' }]),
                minScoreModel: new Backbone.Model({ minScore: 7 }),
                datesFilterModel: { toQueryModelAttributes: _.constant({}) },
                documentSelectionModel: new DocumentSelectionModel()
            };
            this.savedSearchModel = new Backbone.Model({
                type: SavedSearchModel.Type.QUERY,
                relatedConcepts: ['term']
            });

            this.view = new PolicySelectionView({
                queryState: queryState,
                savedSearchModel: this.savedSearchModel
            });

            const events = this.events = [];
            this.view.on('all', function (event) {
                events.push(event)
            });
            spyOn(this.view.policyCollection, 'fetch');

            $('body').append(this.view.$el);
            this.view.render();
            setTimeout(done, 0);
        });

        afterEach(function () {
            jasmine.Ajax.uninstall();
            this.view.remove();
        })

        it('should disable the confirm button', function () {
            expect(this.events).toEqual(['primary-button-disable']);
        })

        expectLoading();

        describe('then the policy collection syncs with policies', function () {

            beforeEach(function () {
                this.view.policyCollection.add([
                    { id: 'a1', name: 'delete' },
                    { id: 'b2', name: 'archive' },
                    { id: 'c3', name: 'spellcheck' }
                ]);
                this.view.policyCollection.trigger('sync');
            });

            expectPolicies();

            it('policy selection should include all values', function () {
                const options = this.view.$('.parametric-select option');
                expect(options.length).toBe(3);
                expect(options.eq(0).attr('value')).toBe('a1');
                expect(options.eq(0).text()).toBe('delete');
                expect(options.eq(1).attr('value')).toBe('b2');
                expect(options.eq(1).text()).toBe('archive');
                expect(options.eq(2).attr('value')).toBe('c3');
                expect(options.eq(2).text()).toBe('spellcheck');
            });

            describe('then the policy is applied (query)', function () {

                beforeEach(function () {
                    this.applyCallback = jasmine.createSpy();
                    this.view.$('.parametric-select').val('c3').change();
                    this.view.applyPolicy(this.applyCallback);
                });

                expectLoading();

                it('should make a request to apply the policy', function () {
                    const request = jasmine.Ajax.requests.mostRecent();
                    expect(request.method).toBe('POST');
                    expect(request.url).toBe('api/public/controlpoint/policy/apply?policy=c3');
                    // from saved query
                    expect(request.data().type).toBe(SavedSearchModel.Type.QUERY);
                    // from query state
                    expect(request.data().minScore).toBe(7);
                    // override from query state
                    expect(request.data().relatedConcepts).toEqual(['term-new']);
                });

                it('should not call the success callback', function () {
                    expect(this.applyCallback.calls.count()).toBe(0);
                })

                describe('then the request succeeds', function () {

                    beforeEach(function () {
                        jasmine.Ajax.requests.mostRecent().respondWith({ status: 200 });
                    });

                    it('should call the success callback', function () {
                        expect(this.applyCallback.calls.count()).toBe(1);
                    })

                });

                // TODO: request error
                describe('then the request fails', function () {

                    beforeEach(function () {
                        jasmine.Ajax.requests.mostRecent().respondWith({
                            status: 400,
                            responseText: '{' +
                                '"message": "bad things",' +
                                '"uuid": "123",' +
                                '"isUserError": false}'
                        });
                    });

                    it('should not call the success callback', function () {
                        expect(this.applyCallback.calls.count()).toBe(0);
                    })

                    expectError();

                    it('should show the error message', function () {
                        expect(this.view.$('.error-message-for-user').html())
                            .toContain(i18n['search.savedSearchControl.applyCPPolicy.error.apply']);
                    })

                });

            });

            describe('then the policy is applied (snapshot)', function () {

                beforeEach(function () {
                    this.savedSearchModel.clear();
                    this.savedSearchModel.set({
                        type: SavedSearchModel.Type.SNAPSHOT,
                        id: 'snap-1'
                    });
                    this.view.$('.parametric-select').val('b2').change();
                    this.view.applyPolicy(_.noop);
                });

                expectLoading();

                it('should make a request to apply the policy', function () {
                    const request = jasmine.Ajax.requests.mostRecent();
                    expect(request.method).toBe('POST');
                    expect(request.url).toBe('' +
                        'api/public/controlpoint/policy/apply?' +
                        'policy=b2&' +
                        'savedSnapshotId=snap-1');
                });

            });

        });

        describe('then the policy collection syncs with no policies', function () {

            beforeEach(function () {
                this.view.policyCollection.trigger('sync');
            });

            expectEmpty();

        });

        describe('then the policy collection sync fails', function () {

            beforeEach(function () {
                this.view.policyCollection.trigger('error', null, {
                    status: 1,
                    responseJSON: { message: 'bad things', uuid: '123', isUserError: false }
                });
            });

            expectError();

            it('should show the error message', function () {
                expect(this.view.$('.error-message-for-user').html())
                    .toContain(i18n['search.savedSearchControl.applyCPPolicy.error.fetch']);
            })

        });

    });

});
