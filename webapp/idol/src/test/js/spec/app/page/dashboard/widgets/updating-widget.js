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
    'find/idol/app/page/dashboard/widgets/updating-widget',
    'find/idol/app/page/dashboard/update-tracker-model'
], function(_, $, UpdatingWidget, UpdateTrackerModel) {
    'use strict';

    const spies = jasmine.createSpyObj('spies', ['onComplete', 'onIncrement', 'onCancelled', 'doUpdate']);

    const TestUpdatingWidget = UpdatingWidget.extend(spies);

    describe('Updating Widget', function() {
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

            this.widget = new TestUpdatingWidget({
                name: 'Test Widget'
            });

            this.widget.render();

            this.updateDeferred = $.Deferred();

            this.updateTrackerModel = new UpdateTrackerModel();
        });

        afterEach(function() {
            _.each(spies, function(spy) {
                spy.calls.reset();
            })
        });

        describe('when the update is synchronous', function() {
            beforeEach(function() {
                this.widget.doUpdate.and.callFake(function(done) {
                    done();
                });

                this.widget.update(this.updateTrackerModel);
            });

            it('it should increment the model when the done callback is called', function() {
                expect(this.updateTrackerModel.get('count')).toBe(1);
            });

            it('should call onIncrement when the count increases', function() {
                // count increased when the widget updated
                expect(this.widget.onIncrement.calls.count()).toBe(1);
            });

            it('should call onComplete when the model is set to complete', function() {
                this.updateTrackerModel.set('complete', true);

                expect(this.widget.onComplete.calls.count()).toBe(1);
            });

            it('should call onCancelled when the model is set to cancelled', function() {
                this.updateTrackerModel.set('cancelled', true);

                expect(this.widget.onCancelled.calls.count()).toBe(1);
            });
        });

        describe('when the update is asynchronous', function() {
            beforeEach(function() {
                // when a test resolves the deferred, call the done callback
                this.widget.doUpdate.and.callFake(function(done) {
                    this.updateDeferred.done(done);
                }.bind(this));

            });

            describe('and the update is called', function() {
                beforeEach(function() {
                    this.widget.update(this.updateTrackerModel);
                });

                it('should show the loading spinner until the update completes', function() {
                    expect(this.widget).toShowLoadingSpinner();

                    this.updateDeferred.resolve();

                    expect(this.widget).not.toShowLoadingSpinner();
                });

                it('should not increment the model until the update is complete', function() {
                    expect(this.updateTrackerModel.get('count')).toBe(0);

                    this.updateDeferred.resolve();

                    expect(this.updateTrackerModel.get('count')).toBe(1);
                });

                it('should call onIncrement when the count increases', function() {
                    this.updateTrackerModel.increment();

                    expect(this.widget.onIncrement.calls.count()).toBe(1);
                });

                it('should call onComplete when the model is set to complete', function() {
                    this.updateTrackerModel.set('complete', true);

                    expect(this.widget.onComplete.calls.count()).toBe(1);
                });

                it('should call onCancelled when the model is set to cancelled', function() {
                    this.updateTrackerModel.set('cancelled', true);

                    expect(this.widget.onCancelled.calls.count()).toBe(1);
                });
            });
        })
    });
});
