/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
        promiseArray[promiseArray.length - 1].reject({statusText: 'abc'});
    }

    // This is the scenario where the widget has not yet executed postInitialize() or the promise was rejected.
    // A widget should only call postInitialize() once during its lifetime.
    // Parameters describe how many times a given fetch (saved search, postInitialize() or getData()) had been done.
    // Unsuccessful calls count towards these numbers. The parameter depth is a recursion-limiting integer.
    function updateCycleWithInit(savedSearchCallCount, postInitializeCallCount, getDataCallCount, depth) {
        return function() {
            beforeEach(function() {
                this.widget.update(this.tracker);
            });

            it('displays a loading spinner', function() {
                expect(this.widget).toShowLoadingSpinner();
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
                    expect(this.widget).toShowLoadingSpinner();
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
                        expect(this.widget).toShowLoadingSpinner();
                    });

                    it('calls getData()', function() {
                        expect(this.widget.getData.calls.count()).toEqual(getDataCallCount + 1);
                    });

                    describe('getData() succeeds -> ', function() {
                        describe('result set is empty -> ', function() {
                            beforeEach(function() {
                                this.widget.isEmpty.and.returnValue(true);
                                resolveLatest(this.getDataPromises);
                            });

                            it('no longer displays a loading spinner', function() {
                                expect(this.widget).not.toShowLoadingSpinner();
                            });

                            it('does not display the visualizer', function() {
                                expect(this.widget.$content).toHaveClass('hide');
                            });

                            it('does not display an error message', function() {
                                expect(this.widget.$error).toHaveClass('hide');
                            });

                            it('displays the "no results" message', function() {
                                expect(this.widget.$empty).not.toHaveClass('hide');
                            });

                            if(depth > 0) {
                                describe('scheduled update -> ',
                                    updateCycle(
                                        savedSearchCallCount + 1,
                                        postInitializeCallCount + 1,
                                        getDataCallCount + 1,
                                        depth - 1
                                    ));
                            }
                        });

                        describe('result set is not empty -> ', function() {
                            beforeEach(function() {
                                this.widget.isEmpty.and.returnValue(false);
                                resolveLatest(this.getDataPromises);
                            });

                            it('no longer displays a loading spinner', function() {
                                expect(this.widget).not.toShowLoadingSpinner();
                            });

                            it('displays the visualizer', function() {
                                expect(this.widget.$content).not.toHaveClass('hide');
                            });

                            it('does not display an error message', function() {
                                expect(this.widget.$error).toHaveClass('hide');
                            });

                            it('does not display the "no results" message', function() {
                                expect(this.widget.$empty).toHaveClass('hide');
                            });

                            if(depth > 0) {
                                describe('scheduled update -> ',
                                    updateCycle(
                                        savedSearchCallCount + 1,
                                        postInitializeCallCount + 1,
                                        getDataCallCount + 1,
                                        depth - 1
                                    ));
                            }
                        });
                    });

                    describe('getData() fails -> ', function() {
                        describe('result set is empty -> ', function() {
                            beforeEach(function() {
                                this.widget.isEmpty.and.returnValue(true);
                                rejectLatest(this.getDataPromises);
                            });

                            it('no longer displays a loading spinner', function() {
                                expect(this.widget).not.toShowLoadingSpinner();
                            });

                            it('does not display the visualizer', function() {
                                expect(this.widget.$content).toHaveClass('hide');
                            });

                            it('displays an error message', function() {
                                expect(this.widget.$error).not.toHaveClass('hide');
                            });

                            it('does not display the "no results" message', function() {
                                expect(this.widget.$empty).toHaveClass('hide');
                            });

                            if(depth > 0) {
                                describe('scheduled update -> ',
                                    updateCycle(
                                        savedSearchCallCount + 1,
                                        postInitializeCallCount + 1,
                                        getDataCallCount + 1,
                                        depth - 1
                                    ));
                            }
                        });

                        describe('result set is not empty -> ', function() {
                            beforeEach(function() {
                                this.widget.isEmpty.and.returnValue(false);
                                rejectLatest(this.getDataPromises);
                            });

                            it('no longer displays a loading spinner', function() {
                                expect(this.widget).not.toShowLoadingSpinner();
                            });

                            it('does not display the visualizer', function() {
                                expect(this.widget.$content).toHaveClass('hide');
                            });

                            it('displays an error message', function() {
                                expect(this.widget.$error).not.toHaveClass('hide');
                            });

                            it('does not display the "no results" message', function() {
                                expect(this.widget.$empty).toHaveClass('hide');
                            });

                            if(depth > 0) {
                                describe('scheduled update -> ',
                                    updateCycle(
                                        savedSearchCallCount + 1,
                                        postInitializeCallCount + 1,
                                        getDataCallCount + 1,
                                        depth - 1
                                    ));
                            }
                        });
                    });

                    if(depth > 0) {
                        describe('scheduled update before getData() succeeds -> ',
                            updateCycle(
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
                        expect(this.widget).not.toShowLoadingSpinner();
                    });

                    it('does not display the visualizer', function() {
                        expect(this.widget.$content).toHaveClass('hide');
                    });

                    it('displays an error message', function() {
                        expect(this.widget.$error).not.toHaveClass('hide');
                    });

                    it('does not display the "no results" message', function() {
                        expect(this.widget.$empty).toHaveClass('hide');
                    });

                    it('does not call getData()', function() {
                        expect(this.widget.getData.calls.count()).toEqual(getDataCallCount);
                    });

                    if(depth > 0) {
                        describe('scheduled update -> ',
                            updateCycleWithInit(
                                savedSearchCallCount + 1,
                                postInitializeCallCount + 1,
                                getDataCallCount,
                                depth - 1
                            ));
                    }
                });

                if(depth > 0) {
                    describe('scheduled update before postInitialize() succeeds -> ',
                        updateCycleWithInit(
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
                    expect(this.widget).not.toShowLoadingSpinner();
                });

                it('does not display the visualizer', function() {
                    expect(this.widget.$content).toHaveClass('hide');
                });

                it('displays an error message', function() {
                    expect(this.widget.$error).not.toHaveClass('hide');
                });

                it('does not display the "no results" message', function() {
                    expect(this.widget.$empty).toHaveClass('hide');
                });

                it('does not call postInitialize()', function() {
                    expect(this.widget.postInitialize.calls.count()).toEqual(postInitializeCallCount);
                });

                it('does not call getData()', function() {
                    expect(this.widget.getData.calls.count()).toEqual(getDataCallCount);
                });

                if(depth > 0) {
                    describe('scheduled update before postInitialize() succeeds -> ',
                        updateCycleWithInit(
                            savedSearchCallCount + 1,
                            postInitializeCallCount,
                            getDataCallCount,
                            depth - 1
                        ));
                }
            });

            if(depth > 0) {
                describe('scheduled update before saved search fetch succeeds -> ',
                    updateCycleWithInit(
                        savedSearchCallCount + 1,
                        postInitializeCallCount,
                        getDataCallCount,
                        depth - 1
                    ));
            }
        };
    }

    // This is the scenario where the widget is fully initialized and is undergoing a regular scheduled update
    // Parameters describe how many times a given fetch (saved search, postInitialize() or getData()) had been done.
    // Unsuccessful calls count towards these numbers. The parameter depth is a recursion-limiting integer.
    function updateCycle(savedSearchCallCount, postInitializeCallCount, getDataCallCount, depth) {
        return function() {
            beforeEach(function() {
                this.widget.update(this.tracker);
            });

            it('displays a loading spinner', function() {
                expect(this.widget).toShowLoadingSpinner();
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
                    expect(this.widget).toShowLoadingSpinner();
                });

                it('does not call postInitialize() again', function() {
                    expect(this.widget.postInitialize.calls.count()).toEqual(postInitializeCallCount);
                });

                it('calls getData()', function() {
                    expect(this.widget.getData.calls.count()).toEqual(getDataCallCount + 1);
                });

                describe('getData() succeeds -> ', function() {
                    describe('result set is empty -> ', function() {
                        beforeEach(function() {
                            this.widget.isEmpty.and.returnValue(true);
                            resolveLatest(this.getDataPromises);
                        });

                        it('no longer displays a loading spinner', function() {
                            expect(this.widget).not.toShowLoadingSpinner();
                        });

                        it('does not display the visualizer', function() {
                            expect(this.widget.$content).toHaveClass('hide');
                        });

                        it('does not display an error message', function() {
                            expect(this.widget.$error).toHaveClass('hide');
                        });

                        it('displays the "no results" message', function() {
                            expect(this.widget.$empty).not.toHaveClass('hide');
                        });

                        if(depth > 0) {
                            describe('scheduled update -> ',
                                updateCycle(
                                    savedSearchCallCount + 1,
                                    postInitializeCallCount,
                                    getDataCallCount + 1,
                                    depth - 1
                                ));
                        }
                    });

                    describe('result set is not empty -> ', function() {
                        beforeEach(function() {
                            this.widget.isEmpty.and.returnValue(false);
                            resolveLatest(this.getDataPromises);
                        });

                        it('no longer displays a loading spinner', function() {
                            expect(this.widget).not.toShowLoadingSpinner();
                        });

                        it('displays the visualizer', function() {
                            expect(this.widget.$content).not.toHaveClass('hide');
                        });

                        it('does not display an error message', function() {
                            expect(this.widget.$error).toHaveClass('hide');
                        });

                        it('does not display the "no results" message', function() {
                            expect(this.widget.$empty).toHaveClass('hide');
                        });

                        if(depth > 0) {
                            describe('scheduled update -> ',
                                updateCycle(
                                    savedSearchCallCount + 1,
                                    postInitializeCallCount,
                                    getDataCallCount + 1,
                                    depth - 1
                                ));
                        }
                    });
                });

                describe('getData() fails -> ', function() {
                    describe('result set is empty -> ', function() {
                        beforeEach(function() {
                            this.widget.isEmpty.and.returnValue(true);
                            rejectLatest(this.getDataPromises);
                        });

                        it('no longer displays a loading spinner', function() {
                            expect(this.widget).not.toShowLoadingSpinner();
                        });

                        it('does not display the visualizer', function() {
                            expect(this.widget.$content).toHaveClass('hide');
                        });

                        it('displays an error message', function() {
                            expect(this.widget.$error).not.toHaveClass('hide');
                        });

                        it('does not display the "no results" message', function() {
                            expect(this.widget.$empty).toHaveClass('hide');
                        });

                        if(depth > 0) {
                            describe('scheduled update -> ',
                                updateCycle(
                                    savedSearchCallCount + 1,
                                    postInitializeCallCount,
                                    getDataCallCount + 1,
                                    depth - 1
                                ));
                        }
                    });

                    describe('result set is not empty -> ', function() {
                        beforeEach(function() {
                            this.widget.isEmpty.and.returnValue(false);
                            rejectLatest(this.getDataPromises);
                        });

                        it('no longer displays a loading spinner', function() {
                            expect(this.widget).not.toShowLoadingSpinner();
                        });

                        it('does not display the visualizer', function() {
                            expect(this.widget.$content).toHaveClass('hide');
                        });

                        it('displays an error message', function() {
                            expect(this.widget.$error).not.toHaveClass('hide');
                        });

                        it('does not display the "no results" message', function() {
                            expect(this.widget.$empty).toHaveClass('hide');
                        });

                        if(depth > 0) {
                            describe('scheduled update -> ',
                                updateCycle(
                                    savedSearchCallCount + 1,
                                    postInitializeCallCount,
                                    getDataCallCount + 1,
                                    depth - 1
                                ));
                        }
                    });
                });

                if(depth > 0) {
                    describe('scheduled update before getData() succeeds -> ',
                        updateCycle(
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
                    expect(this.widget).not.toShowLoadingSpinner();
                });

                it('does not display the visualizer', function() {
                    expect(this.widget.$content).toHaveClass('hide');
                });

                it('displays an error message', function() {
                    expect(this.widget.$error).not.toHaveClass('hide');
                });

                it('does not display the "no results" message', function() {
                    expect(this.widget.$empty).toHaveClass('hide');
                });

                it('does not call postInitialize() again', function() {
                    expect(this.widget.postInitialize.calls.count()).toEqual(postInitializeCallCount);
                });

                it('does not call getData()', function() {
                    expect(this.widget.getData.calls.count()).toEqual(getDataCallCount);
                });

                if(depth > 0) {
                    describe('scheduled update before getData() succeeds -> ',
                        updateCycle(
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
            jasmine.addMatchers({
                toShowLoadingSpinner: function() {
                    return {
                        compare: function(actual) {
                            const pass = !actual.$loadingSpinner.hasClass('hide');
                            return {
                                pass: pass,
                                message: 'Expected the view ' +
                                (pass ? 'not ' : '') +
                                'to show a loading spinner'
                            };
                        }
                    }
                }
            });

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
            spyOn(this.widget, 'isEmpty');

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

            it('does not display an error message', function() {
                expect(this.widget.$error).toHaveClass('hide');
            });

            it('does not display a "no results" message', function() {
                expect(this.widget.$empty).toHaveClass('hide');
            });

            // test initialisation spinner behaviour ()
            describe('on first update', function() {
                beforeEach(function() {
                    this.widget.update(this.tracker);
                });

                it('does not display an error message', function() {
                    expect(this.widget.$error).toHaveClass('hide');
                });

                it('does not display a "no results" message', function() {
                    expect(this.widget.$empty).toHaveClass('hide');
                });

                it('does not display the visualizer', function() {
                    expect(this.widget.$content).toHaveClass('hide');
                });

                it('displays an initialisation spinner instead of the usual update spinner', function() {
                    expect(this.widget.$('.widget-init-spinner')).toHaveLength(1);
                    expect(this.widget.$loadingSpinner.is(this.widget.$('.widget-init-spinner'))).toBe(true);
                    expect(this.widget).toShowLoadingSpinner();
                });

                describe('the update succeeds', function() {
                    beforeEach(function() {
                        this.widget.isEmpty.and.returnValue(false);
                        resolveLatest(this.savedSearchPromises);
                        resolveLatest(this.postInitializePromises);
                        resolveLatest(this.getDataPromises);
                    });

                    it('the initialisation spinner is removed from the DOM', function() {
                        expect(this.widget.$('.widget-init-spinner')).toHaveLength(0);
                    });

                    it('the $loadingSpinner reference is reassigned to the update spinner', function() {
                        expect(this.widget.$loadingSpinner).toHaveLength(1);
                    });
                });

                describe('the update succeeds, but returns no data', function() {
                    beforeEach(function() {
                        this.widget.isEmpty.and.returnValue(true);
                        resolveLatest(this.savedSearchPromises);
                        resolveLatest(this.postInitializePromises);
                        resolveLatest(this.getDataPromises);
                    });

                    it('the initialisation spinner is removed from the DOM', function() {
                        expect(this.widget.$('.widget-init-spinner')).toHaveLength(0);
                    });

                    it('the $loadingSpinner reference is reassigned to the update spinner', function() {
                        expect(this.widget.$loadingSpinner).toHaveLength(1);
                    });
                });

                describe('the update fails to fetch the saved search', function() {
                    beforeEach(function() {
                        rejectLatest(this.savedSearchPromises);
                    });

                    it('the initialisation spinner is removed from the DOM', function() {
                        expect(this.widget.$('.widget-init-spinner')).toHaveLength(0);
                    });

                    it('the $loadingSpinner reference is reassigned to the update spinner', function() {
                        expect(this.widget.$loadingSpinner).toHaveLength(1);
                    });
                });

                describe('the update fails to resolve postInitialize()', function() {
                    beforeEach(function() {
                        resolveLatest(this.savedSearchPromises);
                        rejectLatest(this.postInitializePromises);
                    });

                    it('the initialisation spinner is removed from the DOM', function() {
                        expect(this.widget.$('.widget-init-spinner')).toHaveLength(0);
                    });

                    it('the $loadingSpinner reference is reassigned to the update spinner', function() {
                        expect(this.widget.$loadingSpinner).toHaveLength(1);
                    });
                });

                describe('the update fails to resolve getData()', function() {
                    beforeEach(function() {
                        resolveLatest(this.savedSearchPromises);
                        resolveLatest(this.postInitializePromises);
                        rejectLatest(this.getDataPromises);
                    });

                    it('the initialisation spinner is removed from the DOM', function() {
                        expect(this.widget.$('.widget-init-spinner')).toHaveLength(0);
                    });

                    it('the $loadingSpinner reference is reassigned to the update spinner', function() {
                        expect(this.widget.$loadingSpinner).toHaveLength(1);
                    });
                });
            });

            // No fetches have yet been carried out -- parameters are 0.
            // Test 3 cycles of initialisation/update
            describe('initial update -> ', updateCycleWithInit(0, 0, 0, 2));
        });
    });
});
