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
                savedSearch: {type: 'QUERY-or-SNAPSHOT', id: 123}
            });

            this.savedSearchPromises = [];
            this.widgetInitializePromises = [];
            this.widgetUpdatePromises = [];

            _.each([
                {
                    spy: 'fetch',
                    target: this.widget.savedSearchModel,
                    promiseArray: this.savedSearchPromises
                },
                {
                    spy: 'postInitialize',
                    target: this.widget,
                    promiseArray: this.widgetInitializePromises
                },
                {
                    spy: 'getData',
                    target: this.widget,
                    promiseArray: this.widgetUpdatePromises
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
                expect(this.widget.$content).toBeTruthy();
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
                        this.widgetInitializePromises[0].resolve();
                    });

                    describe('then the widget updates', function() {
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
                                expect(this.widget.getData.calls.count()).toEqual(1);
                            });
                        });
                    });
                });

                describe('then the widget updates', function() {
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

                        // The widget is not fully initialized by this point, because
                        // the postInitialize() promise hasn't resolved yet
                        it('widget does not call getData()', function() {
                            expect(this.widget.getData).not.toHaveBeenCalled();
                        });

                        describe('then the postInitialize() promise is resolved', function() {
                            beforeEach(function() {
                                this.widgetInitializePromises[0].resolve();
                            });

                            it('the widget calls getData()', function() {
                                expect(this.widget.getData.calls.count()).toEqual(1);
                            });
                        });
                    });
                });
            });
        });
    });
});
