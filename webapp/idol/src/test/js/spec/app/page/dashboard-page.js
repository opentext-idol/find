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
    'backbone',
    'find/idol/app/page/dashboard-page'
], function(_, Backbone, DashboardPage) {
    'use strict';

    function getUpdatingWidgets(dashboardPage) {
        return _.chain(dashboardPage.widgetViews)
            .pluck('view')
            .filter(function(view) {
                return view.isUpdating();
            })
            .value();
    }

    describe('Dashboard Page', function() {
        beforeEach(function() {
            jasmine.clock().install();

            this.sidebarModel = new Backbone.Model({collapsed: false});

            this.dashboardPage = new DashboardPage({
                dashboardName: 'Test Dashboard',
                sidebarModel: this.sidebarModel,
                enabled: true,
                width: 3,
                height: 3,
                updateInterval: 60,
                widgets: [
                    {
                        name: 'Not Updating Widget',
                        type: 'Widget',
                        x: 0,
                        y: 0,
                        width: 1,
                        height: 1
                    },
                    {
                        name: 'Updating Widget 1',
                        type: 'UpdatingWidget',
                        x: 1,
                        y: 0,
                        width: 1,
                        height: 1
                    },
                    {
                        name: 'Updating Widget 2',
                        type: 'UpdatingWidget',
                        x: 2,
                        y: 0,
                        width: 1,
                        height: 1
                    }
                ]
            });

            spyOn(this.dashboardPage, 'update').and.callThrough();
            spyOn(this.dashboardPage, 'onResize').and.callThrough();
            spyOn(this.dashboardPage, 'stopListening').and.callThrough();

            _.each(getUpdatingWidgets(this.dashboardPage), function(view) {
                spyOn(view, 'update').and.callThrough();

                // TODO verify onCancelled spy expectations
                _.each(['onComplete', 'onIncrement', 'onCancelled', 'doUpdate'], function(spy) {
                    spyOn(view, spy)
                })
            });
        });

        afterEach(function() {
            jasmine.clock().uninstall();
        });

        describe('before it is shown', function() {
            it('should not call update even after the update interval has elapsed', function() {
                expect(this.dashboardPage.update).not.toHaveBeenCalled();

                jasmine.clock().tick(59000);
                expect(this.dashboardPage.update).not.toHaveBeenCalled();

                jasmine.clock().tick(2000);
                expect(this.dashboardPage.update).not.toHaveBeenCalled();
            });

            it('should not call update on the updating widgets', function() {
                jasmine.clock().tick(61000);
                _.each(getUpdatingWidgets(this.dashboardPage), function(view) {
                    expect(view.update).not.toHaveBeenCalled();
                });
            });

            describe('when the sidebar is toggled', function() {
                it('should not resize', function() {
                    const callCount = this.dashboardPage.onResize.calls.count();
                    this.sidebarModel.trigger('change:collapsed');
                    expect(this.dashboardPage.onResize.calls.count()).toBe(callCount);
                });
            });
        });

        describe('after the page is shown', function() {
            beforeEach(function() {
                this.dashboardPage.show();
            });

            it('contains a dropdown, which has one button for multislide export', function() {
                expect(this.dashboardPage.$('.dropdown-menu .report-pptx.report-pptx-multipage')).toHaveLength(1);
            });

            it('contains a dropdown, which has one button for single-slide export', function() {
                expect(this.dashboardPage.$('.dropdown-menu .report-pptx:not(.report-pptx-multipage)')).toHaveLength(1);
            });

            it('contains a dropdown, which has one button for entering fullscreen mode', function() {
                expect(this.dashboardPage.$('.dropdown-menu .fullscreen')).toHaveLength(1);
            });

            it('should call update on the updating widgets', function() {
                _.each(getUpdatingWidgets(this.dashboardPage), function(view) {
                    expect(view.update.calls.count()).toBe(1);
                });
            });

            describe('after the first widget\'s update completes', function() {
                beforeEach(function() {
                    // call the done callback for the first updating widget
                    getUpdatingWidgets(this.dashboardPage)[0].doUpdate.calls.argsFor(0)[0]();
                });

                it('should not mark the tracker complete', function() {
                    expect(this.dashboardPage.updateTracker.get('complete')).toBe(false);
                });

                describe('after the second widget\'s update completes', function() {
                    beforeEach(function() {
                        // call the done callback for the second updating widget
                        getUpdatingWidgets(this.dashboardPage)[1].doUpdate.calls.argsFor(0)[0]();
                    });

                    it('should mark the tracker complete', function() {
                        expect(this.dashboardPage.updateTracker.get('complete')).toBe(true);
                    });

                    it('should stop listening to the tracker', function() {
                        expect(this.dashboardPage.stopListening.calls.count()).toBe(1);
                    });

                    describe('then an update interval elapses', function() {
                        beforeEach(function() {
                            jasmine.clock().tick(61000);
                        });

                        it('should call update on the updating widgets', function() {
                            _.each(getUpdatingWidgets(this.dashboardPage), function(view) {
                                expect(view.update.calls.count()).toBe(2);
                            });
                        });

                        describe('after the first widget\'s update completes', function() {
                            beforeEach(function() {
                                // call the done callback for the first updating widget
                                getUpdatingWidgets(this.dashboardPage)[0].doUpdate.calls.argsFor(1)[0]();
                            });

                            it('should not mark the tracker complete', function() {
                                expect(this.dashboardPage.updateTracker.get('complete')).toBe(false);
                            });

                            describe('after the second widget\'s update completes', function() {
                                beforeEach(function() {
                                    // call the done callback for the second updating widget
                                    getUpdatingWidgets(this.dashboardPage)[1].doUpdate.calls.argsFor(1)[0]();
                                });

                                it('should mark the tracker complete', function() {
                                    expect(this.dashboardPage.updateTracker.get('complete')).toBe(true);
                                });

                                it('should stop listening to the tracker', function() {
                                    expect(this.dashboardPage.stopListening.calls.count()).toBe(2);
                                });
                            });

                            describe('if the interval elapses before completion occurs', function() {
                                beforeEach(function() {
                                    this.oldUpdateTracker = this.dashboardPage.updateTracker;

                                    jasmine.clock().tick(61000);
                                });

                                it('should cancel the previous tracker', function() {
                                    expect(this.oldUpdateTracker.get('cancelled')).toBe(true);
                                    expect(this.dashboardPage.stopListening.calls.count()).toBe(2);
                                });
                            });
                        });
                    });
                });

                describe('if the interval elapses before completion occurs', function() {
                    beforeEach(function() {
                        this.oldUpdateTracker = this.dashboardPage.updateTracker;

                        jasmine.clock().tick(61000);
                    });

                    it('should cancel the previous tracker', function() {
                        expect(this.oldUpdateTracker.get('cancelled')).toBe(true);
                        expect(this.dashboardPage.stopListening.calls.count()).toBe(1);
                    });
                });
            });

            describe('when the sidebar is toggled', function() {
                it('should resize', function() {
                    const callCount = this.dashboardPage.onResize.calls.count();
                    this.sidebarModel.trigger('change:collapsed');
                    expect(this.dashboardPage.onResize.calls.count()).toBe(callCount + 1);
                });
            });

            describe('then the page is hidden', function() {
                beforeEach(function() {
                    this.dashboardPage.hide();
                    jasmine.clock().tick(61000);
                });

                it('should not update the widgets again', function() {
                    _.each(getUpdatingWidgets(this.dashboardPage), function(view) {
                        expect(view.update.calls.count()).toBe(1);
                    });
                });

                it('should cancel the tracker', function() {
                    expect(this.dashboardPage.updateTracker.get('cancelled')).toBe(true);
                    expect(this.dashboardPage.stopListening.calls.argsFor(0)[0])
                        .toBe(this.dashboardPage.updateTracker);
                });

                describe('when the sidebar is toggled', function() {
                    it('should not resize', function() {
                        const callCount = this.dashboardPage.onResize.calls.count();
                        this.sidebarModel.trigger('change:collapsed');
                        expect(this.dashboardPage.onResize.calls.count()).toBe(callCount);
                    });
                });
            });
        });
    });
});
