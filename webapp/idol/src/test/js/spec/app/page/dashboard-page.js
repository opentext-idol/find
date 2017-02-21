/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'find/idol/app/page/dashboard-page'
], function(_, DashboardPage) {
    'use strict';

    const getUpdatingWidgets = function(dashboardPage) {
        return _.chain(dashboardPage.widgetViews)
            .pluck('view')
            .filter(function(view) {
                return view.isUpdating();
            })
            .value();
    };

    describe('Dashboard Page', function() {
        beforeEach(function() {
            jasmine.clock().install();

            this.dashboardPage = new DashboardPage({
                "dashboardName": "Test Dashboard",
                "enabled": true,
                "width": 3,
                "height": 3,
                "updateInterval": 60,
                "widgets": [{
                    "name": "Not Updating Widget",
                    "type": "widget",
                    "x": 0,
                    "y": 0,
                    "width": 1,
                    "height": 1
                }, {
                    "name": "Updating Widget 1",
                    "type": "updatingWidget",
                    "x": 1,
                    "y": 0,
                    "width": 1,
                    "height": 1
                }, {
                    "name": "Updating Widget 2",
                    "type": "updatingWidget",
                    "x": 2,
                    "y": 0,
                    "width": 1,
                    "height": 1
                }]
            });

            spyOn(this.dashboardPage, 'update').and.callThrough();
            spyOn(this.dashboardPage, 'stopListening').and.callThrough();

            this.dashboardPage.render();

            _.each(getUpdatingWidgets(this.dashboardPage), function(view) {
                spyOn(view, 'update').and.callThrough();

                _.each(['onComplete', 'onIncrement', 'onCancelled', 'doUpdate'], function(spy) {
                    spyOn(view, spy)
                })
            });
        });

        afterEach(function() {
            jasmine.clock().uninstall();
        });

        it('should not update until the update interval has elapsed', function() {
            expect(this.dashboardPage.update).not.toHaveBeenCalled();

            jasmine.clock().tick(59000);

            expect(this.dashboardPage.update).not.toHaveBeenCalled();

            jasmine.clock().tick(2000);

            expect(this.dashboardPage.update).toHaveBeenCalled();
        });

        describe('after the update interval elapses', function() {
            beforeEach(function() {
                jasmine.clock().tick(61000);
            });

            it('should call update on the updating widgets', function() {
                _.each(getUpdatingWidgets(this.dashboardPage), function(view) {
                    expect(view.update).toHaveBeenCalled()
                });
            });

            describe('after a widget completes', function() {
                beforeEach(function() {
                    // call the done callback for the first updating widget
                    getUpdatingWidgets(this.dashboardPage)[0].doUpdate.calls.argsFor(0)[0]()
                });

                it('should not mark the tracker complete', function() {
                    expect(this.dashboardPage.updateTracker.get('complete')).toBe(false);
                });

                describe('after the second widget completes', function() {
                    beforeEach(function() {
                        // call the done callback for the second updating widget
                        getUpdatingWidgets(this.dashboardPage)[1].doUpdate.calls.argsFor(0)[0]()
                    });

                    it('should mark the tracker complete', function() {
                        expect(this.dashboardPage.updateTracker.get('complete')).toBe(true);
                    });

                    it('should stop listening to the tracker', function() {
                        expect(this.dashboardPage.stopListening.calls.count()).toBe(1);
                    });
                });

                describe('if the interval elapses before completion occurs', function() {
                    beforeEach(function() {
                        this.oldUpdateTracker = this.dashboardPage.updateTracker;

                        jasmine.clock().tick(60000);
                    });

                    it('should cancel the previous tracker', function() {
                        expect(this.oldUpdateTracker.get('cancelled')).toBe(true);

                        expect(this.dashboardPage.stopListening.calls.count()).toBe(1);
                    });
                });
            })
        });
    })
});
