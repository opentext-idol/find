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

    // TODO happy path tests only, dashboards have no error handling at this time
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

        describe('when the page renders and updates the widget', function() {
            beforeEach(function() {
                this.widget.render();
                this.widget.update(this.tracker);
            });

            it('navigates to the saved search when clicked', function() {
                this.widget.$content.click();
                expect(vent.navigate).toHaveBeenCalledWith('/search/tab/QUERY-or-SNAPSHOT:123');
            });

            it('displays the widget\'s container without waiting for data to fetch', function() {
                expect(this.widget.$content).toBeDefined();
            });

            it('fetches its saved search data', function() {
                expect(this.widget.savedSearchModel.fetch.calls.count()).toEqual(1);
            });

            it('does not fetch additional initialization data', function() {
                expect(this.widget.postInitialize).not.toHaveBeenCalled();
            });

            describe('then the saved search fetch succeeds', function() {
                beforeEach(function() {
                    this.savedSearchPromises[0].resolve();
                });

                it('the widget fetches additional data', function() {
                    expect(this.widget.postInitialize.calls.count()).toEqual(1);
                });

                describe('then the postInitialize() promise is resolved', function() {
                    beforeEach(function() {
                        this.postInitializePromises[0].resolve();
                    });

                    it('the widget calls getData() after initialisation', function() {
                        expect(this.widget.getData.calls.count()).toEqual(1);
                    });

                    describe('then the widget fetches data and is updated', function() {
                        beforeEach(function() {
                            this.getDataPromises[0].resolve();
                            this.widget.update(this.tracker);
                        });

                        it('fetches the saved search again', function() {
                            expect(this.widget.savedSearchModel.fetch.calls.count()).toEqual(2);
                        });

                        it('does not call postInitialize() again', function() {
                            expect(this.widget.postInitialize.calls.count()).toEqual(1);
                        });

                        describe('then the saved search fetch succeeds', function() {
                            beforeEach(function() {
                                this.savedSearchPromises[1].resolve();
                            });

                            it('the widget calls getData() again', function() {
                                expect(this.widget.getData.calls.count()).toEqual(2);
                            });
                        });
                    });

                    describe('then the widget updates before the first promise resolves', function() {
                        beforeEach(function() {
                            this.widget.update(this.tracker);
                        });

                        it('fetches the saved search again', function() {
                            expect(this.widget.savedSearchModel.fetch.calls.count()).toEqual(2);
                        });

                        it('does not call postInitialize() again', function() {
                            expect(this.widget.postInitialize.calls.count()).toEqual(1);
                        });

                        describe('then the saved search fetch succeeds', function() {
                            beforeEach(function() {
                                this.savedSearchPromises[1].resolve();
                            });

                            it('the widget calls getData()', function() {
                                expect(this.widget.getData.calls.count()).toEqual(2);
                            });
                        });
                    });
                });

                describe('then the widget updates before the postInitialize() promise resolves', function() {
                    beforeEach(function() {
                        this.widget.update(this.tracker);
                    });

                    it('does not call postInitialize() again', function() {
                        expect(this.widget.postInitialize.calls.count()).toEqual(1);
                    });

                    // TODO this is not ideal. If saved search is modified between
                    // the first saved search fetch and the postInitialize() promise resolving, the first update
                    // will be based on stale data.
                    it('does not fetch the saved search again', function() {
                        expect(this.widget.savedSearchModel.fetch.calls.count()).toEqual(1);
                    });

                    describe('then the postInitialize() promise is resolved', function() {
                        beforeEach(function() {
                            this.postInitializePromises[0].resolve();
                        });

                        it('the widget only calls getData() once', function() {
                            expect(this.widget.getData.calls.count()).toEqual(1);
                        });
                    });
                });
            });

            describe('then the widget updates before the saved search returns', function() {
                beforeEach(function() {
                    this.widget.update(this.tracker);
                });

                it('does not fetch the saved search again', function() {
                    expect(this.widget.savedSearchModel.fetch.calls.count()).toEqual(1);
                });

                describe('then the saved search fetch succeeds', function() {
                    beforeEach(function() {
                        this.savedSearchPromises[0].resolve();
                    });

                    it('postInitialize() is called', function() {
                        expect(this.widget.postInitialize.calls.count()).toEqual(1);
                    });

                    it('getData() is not called', function() {
                        expect(this.widget.getData).not.toHaveBeenCalled();
                    });

                    describe('then the postInitialize() promise is resolved', function() {
                        beforeEach(function() {
                            this.postInitializePromises[0].resolve();
                        });

                        it('the widget only calls getData() once', function() {
                            expect(this.widget.getData.calls.count()).toEqual(1);
                        });
                    });
                });
            });
        });
    });
});
