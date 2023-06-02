/*
 * Copyright 2016-2017 Open Text.
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
    'underscore',
    'jquery',
    'backbone',
    'find/idol/app/page/search/comparison/search-to-compare-view'
], function(_, $, Backbone, SearchToCompareView) {
    'use strict';

    describe('Search To Compare View', function() {
        beforeEach(function() {
            this.primaryModel = new Backbone.Model({
                id: 'a',
                title: 'Primary'
            });

            this.candidateOne = new Backbone.Model({
                id: 'b',
                title: 'Candidate One'
            });

            this.candidateTwo = new Backbone.Model({
                id: 'c',
                title: 'Candidate Two'
            });

            this.savedSearchCollection = new Backbone.Collection([this.primaryModel, this.candidateOne, this.candidateTwo]);

            this.view = new SearchToCompareView({
                savedSearchCollection: this.savedSearchCollection,
                selectedSearch: this.primaryModel,
                originalSelectedModelCid: this.primaryModel.cid
            });

            this.view.render();

            this.listener = jasmine.createSpy('listener');
            this.view.on('selected', this.listener);

            this.$bElement = this.view.$('[data-search-cid=' + this.savedSearchCollection.get('b').cid + ']');
            this.$cElement = this.view.$('[data-search-cid=' + this.savedSearchCollection.get('c').cid + ']');
        });

        it('should indicate the primary model', function() {
            expect(this.view.$('.primary-model-title')).toHaveText('Primary');
        });

        it('should display only the models other than the primary', function() {
            const titles = _.map(this.view.$('.secondary-model-title'), function(el) {
                return $(el).text().trim();
            });

            expect(titles).toHaveLength(2);
            expect(titles).toContain('Candidate One');
            expect(titles).toContain('Candidate Two');
        });

        describe('when search b is clicked', function() {
            beforeEach(function() {
                this.$bElement.click();
            });

            it('highlights search b', function() {
                expect(this.$bElement).toHaveClass('selected-saved-search');
            });

            it('does not highlight search c', function() {
                expect(this.$cElement).not.toHaveClass('selected-saved-search');
            });

            it('triggers the selected element cid', function() {
                expect(this.listener).toHaveBeenCalledWith(this.savedSearchCollection.get('b').cid);
            });

            describe('then search c is clicked', function() {
                beforeEach(function() {
                    this.$cElement.click();
                });

                it('highlights search c', function() {
                    expect(this.$cElement).toHaveClass('selected-saved-search');
                });

                it('does not highlight search b', function() {
                    expect(this.$bElement).not.toHaveClass('selected-saved-search');
                });

                it('triggers the selected element cid', function() {
                    expect(this.listener).toHaveBeenCalledWith(this.savedSearchCollection.get('c').cid);
                });
            });
        });
    });
});
