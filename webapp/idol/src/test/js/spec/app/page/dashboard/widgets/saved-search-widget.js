/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'find/idol/app/page/dashboard/widgets/saved-search-widget',
    'find/app/vent'
], function(_, $, SavedSearchWidget, vent) {
    'use strict';

    function resolveLatest(promiseArray) {
        promiseArray[promiseArray.length - 1].resolve();
    }

    function rejectLatest(promiseArray) {
        promiseArray[promiseArray.length - 1].reject();
    }

    // This is the scenario where the widget is fully initialized and is undergoing a regular scheduled update
    // Parameters describe how many times a given fetch (saved search, postInitialize() or getData()) had been done.
    // Unsuccessful calls count towards these numbers. The parameter depth is a recursion-limiting integer.
    function updateWithInit(savedSearchCallCount, postInitializeCallCount, getDataCallCount, depth) {
        return function() {
            beforeEach(function() {
                this.widget.update(this.tracker);
            });

            it('displays a loading spinner', function() {
                expect(this.widget.$('.widget-loading')).not.toHaveClass('hide');
                expect(this.widget.$('.widget-content')).toHaveClass('hide');
            });

            it('fetches the saved search', function() {
                expect(this.widget.savedSearchModel.fetch.calls.count()).toEqual(savedSearchCallCount + 1);
            });

            it('does not call postInitialize() yet', function() {
                expect(this.widget.postInitialize.calls.count()).toEqual(postInitializeCallCount);
            });

            it('does not call getData() yet', function() {
                expect(this.widget.getData.calls.count()).toEqual(getDataCallCount);
            });

            describe('saved search fetch succeeds -> ', function() {
                beforeEach(function() {
                    resolveLatest(this.savedSearchPromises);
                });

                it('still displays a loading spinner', function() {
                    expect(this.widget.$('.widget-loading')).not.toHaveClass('hide');
                    expect(this.widget.$('.widget-content')).toHaveClass('hide');
                });

                it('calls postInitialize()', function() {
                    expect(this.widget.postInitialize.calls.count()).toEqual(postInitializeCallCount + 1);
                });

                it('does not call getData() yet', function() {
                    expect(this.widget.getData.calls.count()).toEqual(getDataCallCount);
                });

                describe('postInitialize() succeeds -> ', function() {
                    beforeEach(function() {
                        resolveLatest(this.postInitializePromises);
                    });

                    it('still displays a loading spinner', function() {
                        expect(this.widget.$('.widget-loading')).toHaveClass('hide');
                        expect(this.widget.$('.widget-content')).not.toHaveClass('hide');
                        expect(this.widget.$('.widget-loading-spinner')).not.toHaveClass('hide');
                    });

                    it('calls getData()', function() {
                        expect(this.widget.getData.calls.count()).toEqual(getDataCallCount + 1);
                    });

                    describe('getData() succeeds -> ', function() {
                        beforeEach(function() {
                            resolveLatest(this.getDataPromises);
                        });

                        it('no longer displays a loading spinner', function() {
                            expect(this.widget.$('.widget-loading-spinner')).toHaveClass('hide');
                        });

                        it('does not display an error message', function() {
                            expect(this.widget.$error).toHaveClass('hide');
                        });

                        if(depth > 0) {
                            describe('scheduled update -> ',
                                updateWithoutInit(
                                    savedSearchCallCount + 1,
                                    postInitializeCallCount + 1,
                                    getDataCallCount + 1,
                                    depth - 1
                                ));
                        }
                    });

                    describe('getData() fails -> ', function() {
                        beforeEach(function() {
                            rejectLatest(this.getDataPromises);
                        });

                        it('no longer displays a loading spinner', function() {
                            expect(this.widget.$('.widget-loading-spinner')).toHaveClass('hide');
                        });

                        it('displays an error message', function() {
                            expect(this.widget.$error).not.toHaveClass('hide');
                        });

                        if(depth > 0) {
                            describe('scheduled update -> ',
                                updateWithoutInit(
                                    savedSearchCallCount + 1,
                                    postInitializeCallCount + 1,
                                    getDataCallCount + 1,
                                    depth - 1
                                ));
                        }
                    });

                    if(depth > 0) {
                        describe('scheduled update before getData() succeeds -> ',
                            updateWithoutInit(
                                savedSearchCallCount + 1,
                                postInitializeCallCount + 1,
                                getDataCallCount + 1,
                                depth - 1
                            ));
                    }
                });

                describe('postInitialize() fails -> ', function() {
                    beforeEach(function() {
                        rejectLatest(this.postInitializePromises);
                    });

                    it('no longer displays a loading spinner', function() {
                        expect(this.widget.$('.widget-loading-spinner')).toHaveClass('hide');
                    });

                    it('displays an error message', function() {
                        expect(this.widget.$error).not.toHaveClass('hide');
                    });

                    it('does not call getData()', function() {
                        expect(this.widget.getData.calls.count()).toEqual(getDataCallCount);
                    });

                    if(depth > 0) {
                        describe('scheduled update -> ',
                            updateWithInit(
                                savedSearchCallCount + 1,
                                postInitializeCallCount + 1,
                                getDataCallCount,
                                depth - 1
                            ));
                    }
                });

                if(depth > 0) {
                    describe('scheduled update before postInitialize() succeeds -> ',
                        updateWithInit(
                            savedSearchCallCount + 1,
                            postInitializeCallCount + 1,
                            getDataCallCount,
                            depth - 1
                        ));
                }
            });

            describe('saved search fetch fails -> ', function() {
                beforeEach(function() {
                    rejectLatest(this.savedSearchPromises);
                });

                it('no longer displays a loading spinner', function() {
                    expect(this.widget.$('.widget-loading-spinner')).toHaveClass('hide');
                });

                it('displays an error message', function() {
                    expect(this.widget.$error).not.toHaveClass('hide');
                });

                it('does not call postInitialize()', function() {
                    expect(this.widget.postInitialize.calls.count()).toEqual(postInitializeCallCount);
                });

                it('does not call getData()', function() {
                    expect(this.widget.getData.calls.count()).toEqual(getDataCallCount);
                });

                if(depth > 0) {
                    describe('scheduled update before postInitialize() succeeds -> ',
                        updateWithInit(
                            savedSearchCallCount + 1,
                            postInitializeCallCount,
                            getDataCallCount,
                            depth - 1
                        ));
                }
            });

            if(depth > 0) {
                describe('scheduled update before saved search fetch succeeds -> ',
                    updateWithInit(
                        savedSearchCallCount + 1,
                        postInitializeCallCount,
                        getDataCallCount,
                        depth - 1
                    ));
            }
        };
    }

    // This is the scenario where the widget has not yet executed postInitialize() or the promise was rejected.
    // A widget should only call postInitialize() once during its lifetime.
    // Parameters describe how many times a given fetch (saved search, postInitialize() or getData()) had been done.
    // Unsuccessful calls count towards these numbers. The parameter depth is a recursion-limiting integer.
    function updateWithoutInit(savedSearchCallCount, postInitializeCallCount, getDataCallCount, depth) {
        return function() {
            beforeEach(function() {
                this.widget.update(this.tracker);
            });

            it('displays a loading spinner', function() {
                expect(this.widget.$('.widget-loading-spinner')).not.toHaveClass('hide');
            });

            it('fetches the saved search', function() {
                expect(this.widget.savedSearchModel.fetch.calls.count()).toEqual(savedSearchCallCount + 1);
            });

            it('does not call getData() yet', function() {
                expect(this.widget.getData.calls.count()).toEqual(getDataCallCount);
            });

            describe('saved search fetch succeeds -> ', function() {
                beforeEach(function() {
                    resolveLatest(this.savedSearchPromises);
                });

                it('still displays a loading spinner', function() {
                    expect(this.widget.$('.widget-loading-spinner')).not.toHaveClass('hide');
                });

                it('does not call postInitialize() again', function() {
                    expect(this.widget.postInitialize.calls.count()).toEqual(postInitializeCallCount);
                });

                it('calls getData()', function() {
                    expect(this.widget.getData.calls.count()).toEqual(getDataCallCount + 1);
                });

                describe('getData() succeeds -> ', function() {
                    beforeEach(function() {
                        resolveLatest(this.getDataPromises);
                    });

                    it('no longer displays a loading spinner', function() {
                        expect(this.widget.$('.widget-loading-spinner')).toHaveClass('hide');
                    });

                    it('does not display an error message', function() {
                        expect(this.widget.$error).toHaveClass('hide');
                    });

                    if(depth > 0) {
                        describe('scheduled update -> ',
                            updateWithoutInit(
                                savedSearchCallCount + 1,
                                postInitializeCallCount,
                                getDataCallCount + 1,
                                depth - 1
                            ));
                    }
                });

                describe('getData() fails -> ', function() {
                    beforeEach(function() {
                        rejectLatest(this.getDataPromises);
                    });

                    it('no longer displays a loading spinner', function() {
                        expect(this.widget.$('.widget-loading-spinner')).toHaveClass('hide');
                    });

                    it('displays an error message', function() {
                        expect(this.widget.$error).not.toHaveClass('hide');
                    });

                    if(depth > 0) {
                        describe('scheduled update -> ',
                            updateWithoutInit(
                                savedSearchCallCount + 1,
                                postInitializeCallCount,
                                getDataCallCount + 1,
                                depth - 1
                            ));
                    }
                });

                if(depth > 0) {
                    describe('scheduled update before getData() succeeds -> ',
                        updateWithoutInit(
                            savedSearchCallCount + 1,
                            postInitializeCallCount,
                            getDataCallCount + 1,
                            depth - 1
                        ));
                }
            });

            describe('saved search fetch fails -> ', function() {
                beforeEach(function() {
                    rejectLatest(this.savedSearchPromises);
                });

                it('no longer displays a loading spinner', function() {
                    expect(this.widget.$('.widget-loading-spinner')).toHaveClass('hide');
                });

                it('displays an error message', function() {
                    expect(this.widget.$error).not.toHaveClass('hide');
                });

                it('does not call postInitialize() again', function() {
                    expect(this.widget.postInitialize.calls.count()).toEqual(postInitializeCallCount);
                });

                it('does not call getData()', function() {
                    expect(this.widget.getData.calls.count()).toEqual(getDataCallCount);
                });

                if(depth > 0) {
                    describe('scheduled update before getData() succeeds -> ',
                        updateWithoutInit(
                            savedSearchCallCount + 1,
                            postInitializeCallCount,
                            getDataCallCount,
                            depth - 1
                        ));
                }
            });
        };
    }

    describe('Saved Search Widget', function() {
        beforeEach(function() {
            this.widget = new SavedSearchWidget({
                name: 'Test Widget',
                datasource: {
                    source: "SavedSearch",
                    config: {
                        type: 'QUERY-or-SNAPSHOT',
                        id: 123
                    }
                }
            });

            this.savedSearchPromises = [];
            this.postInitializePromises = [];
            this.getDataPromises = [];

            _.each([
                {
                    spy: 'fetch',
                    target: this.widget.savedSearchModel,
                    promiseArray: this.savedSearchPromises
                },
                {
                    spy: 'postInitialize',
                    target: this.widget,
                    promiseArray: this.postInitializePromises
                },
                {
                    spy: 'getData',
                    target: this.widget,
                    promiseArray: this.getDataPromises
                }
            ], function(obj) {
                spyOn(obj.target, obj.spy).and.callFake(function() {
                    const deferred = $.Deferred();
                    obj.promiseArray.push(deferred);
                    return deferred.promise();
                }.bind(this));
            }.bind(this));

            spyOn(vent, 'navigate');

            this.tracker = jasmine.createSpyObj('mockTracker', ['increment']);
        });

        it('does not fetch any data before it is displayed', function() {
            expect(this.widget.savedSearchModel.fetch).not.toHaveBeenCalled();
            expect(this.widget.postInitialize).not.toHaveBeenCalled();
        });

        describe('when the page renders the widget -> ', function() {
            beforeEach(function() {
                this.widget.render();
            });

            it('navigates to the saved search when clicked', function() {
                this.widget.$content.click();
                expect(vent.navigate).toHaveBeenCalledWith('/search/tab/QUERY-or-SNAPSHOT:123');
            });

            it('displays the widget\'s container without waiting for data to fetch', function() {
                this.widget.update(this.tracker);
                expect(this.widget.$content).toBeDefined();
                expect(this.widget.$loading).not.toHaveClass('hide');
            });

            // No fetches have yet been carried out -- parameters are 0.
            // Test 3 cycles of initialisation/update
            describe('initial update -> ', updateWithInit(0, 0, 0, 2));
        });
    });
});
