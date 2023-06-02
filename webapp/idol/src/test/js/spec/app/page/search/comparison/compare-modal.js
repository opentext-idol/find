/*
 * Copyright 2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'find/idol/app/page/search/comparison/compare-modal',
    'find/app/util/modal',
    'find/idol/app/model/comparison/comparison-model',
    'jquery',
    'backbone',
    'underscore'
], function(CompareModal, Modal, ComparisonModel, $, Backbone, _) {
    'use strict';

    function waitFor(predicate, message, done, fail, timeout) {
        var start = Date.now();

        var intervalId = setInterval(function() {
            if(predicate()) {
                clearInterval(intervalId);
                done();
            } else if(Date.now() - start > (timeout || 1000)) {
                clearInterval(intervalId);
                fail('Timed out waiting for ' + message);
            }
        }, 10);
    }

    function modalToOpen() {
        return $('.modal').length === 1;
    }

    function modalToClose() {
        return $('.modal').length === 0;
    }

    describe('Compare Modal', function() {
        beforeEach(function(done) {
            var savedSearchModel = new Backbone.Model({
                id: 1,
                title: 'Pegasus'
            });

            var secondSavedSearchModel = new Backbone.Model({
                id: 2,
                title: 'Unicorns'
            });

            var queryState = {
                conceptGroups: new Backbone.Collection([{concepts: ['Unicorns']}]),
                selectedIndexes: [],
                parametricValues: null,
                parametricRanges: null,
                minScore: null
            };

            this.savedSearchCollection = new Backbone.Collection([savedSearchModel, secondSavedSearchModel]);
            this.queryStates = new Backbone.Model({2: queryState});

            this.comparisonSuccessCallback = jasmine.createSpy('comparisonSuccessCallback');

            this.view = new CompareModal({
                cid: 1,
                savedSearchCollection: this.savedSearchCollection,
                queryStates: this.queryStates,
                comparisonSuccessCallback: this.comparisonSuccessCallback
            });

            // Wait 500 ms for the modal to open
            waitFor(modalToOpen, 'modal to open', _.bind(function() {
                this.$bElement = this.view.$('[data-search-cid=' + this.savedSearchCollection.get(2).cid + ']');
                this.$confirmButton = this.view.$('.modal-action-button');
                done();
            }, this), done.fail);
        }, 2000);

        afterEach(function(done) {
            ComparisonModel.reset();

            this.view.hide();

            waitFor(modalToClose, 'modal to close', done, done.fail);
        });

        it('opens a modal', function() {
            expect($('.modal')).toHaveLength(1);
        });

        describe('after a selection', function() {
            beforeEach(function() {
                this.$bElement.click();
            });

            it('enables the confirm button', function() {
                expect(this.$confirmButton).not.toHaveClass('disabled');
            });

            describe('then the confirm button is clicked', function() {
                beforeEach(function() {
                    spyOn(this.view, 'hide').and.callThrough();

                    this.$confirmButton.click();
                });

                it('disables the confirm button', function() {
                    expect(this.$confirmButton.prop('disabled')).toBe(true);
                });

                it('displays a loading indicator', function() {
                    expect(this.view.$('.compare-modal-error-spinner')).not.toHaveClass('hide');
                });

                it('calls save on the comparison model', function() {
                    expect(ComparisonModel.instances[0].save).toHaveBeenCalled();
                });

                describe('then the request succeeds', function() {
                    beforeEach(function() {
                        var comparisonModel = ComparisonModel.instances[0];
                        comparisonModel.save.calls.argsFor(0)[1].success();
                    });

                    it('calls the comparison success callback', function() {
                        expect(this.comparisonSuccessCallback).toHaveBeenCalled();
                    });

                    it('hides the modal', function() {
                        expect(this.view.hide).toHaveBeenCalled()
                    })
                });

                describe('then the request fails', function() {
                    beforeEach(function() {
                        var comparisonModel = ComparisonModel.instances[0];
                        comparisonModel.save.calls.argsFor(0)[1].error();
                    });

                    it('doesn\'t hide the modal', function() {
                        expect(this.view.hide).not.toHaveBeenCalled()
                    });

                    it('enables the confirm button', function() {
                        expect(this.$confirmButton.prop('disabled')).toBe(false);
                    });

                    it('does not display a loading indicator', function() {
                        expect(this.view.$('.compare-modal-error-spinner')).toHaveClass('hide');
                    });
                });

                describe('then the cancel button is clicked', function() {
                    beforeEach(function(done) {
                        this.view.$('[data-dismiss="modal"]')[1].click();

                        waitFor(modalToClose, 'modal to close', done, done.fail);
                    });

                    it('cancels the request', function() {
                        expect(ComparisonModel.instances[0].mockXhrs[0].abort).toHaveBeenCalled();
                    });
                });
            });
        });
    });
});
