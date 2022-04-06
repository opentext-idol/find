/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'find/app/page/search/filter-display/applied-filters-view',
    'i18n!find/nls/bundle'
], function(Backbone, AppliedFiltersView, i18n) {
    'use strict';

    describe('Applied Filters View', function() {
        beforeEach(function() {
            this.appliedFiltersCollection = new Backbone.Collection();
            this.view = new AppliedFiltersView({
                collection: this.appliedFiltersCollection,
                title: i18n['search.filters.applied'],
                containerClass: 'left-side-applied-filters-view',
                titleClass: 'inline-block'
            });
            this.view.render();
        });

        describe('when backed by an empty collection', function() {
            describe('has a header', function() {
                it('which displays the correct title', function() {
                    expect(this.view.$el.find('.left-side-applied-filters-view-title'))
                        .toContainText(i18n['search.filters.applied']);
                });

                it('which is visible even when collection is empty', function() {
                    expect(this.view.$el.find('.left-side-applied-filters-view-title')).not.toHaveClass('hide');
                });

                it('which contains a counter initialised to zero', function() {
                    expect(this.view.getHeaderCounter()).toContainText('(0)');
                });
            });

            it('does not display a Remove All button', function() {
                expect(this.view.getSectionControls()).toHaveClass('hide');
            });
        });

        describe('when backed by a non-empty collection', function() {
            beforeEach(function() {
                this.appliedFiltersCollection.add({});
            });

            it('displays a Remove All button', function() {
                expect(this.view.getSectionControls()).not.toHaveClass('hide');
            });

            it('displays a filter label', function() {
                expect(this.view.$el.find('.filter-label')).toHaveLength(1);
            });

            it('increments the counter accordingly', function() {
                expect(this.view.getHeaderCounter()).toContainText('(1)');
            });

            it('clicking the Remove All button removes test labels, the Remove All' +
                ' button, and resets the counter', function() {
                this.view.$el.find('.remove-all-filters').click();
                expect(this.view.collection.isEmpty()).toBe(true);
                expect(this.view.$el.find('.filter-label')).toHaveLength(0);
                expect(this.view.getSectionControls()).toHaveClass('hide');
                expect(this.view.getHeaderCounter()).toContainText('(0)');
            });
        });
    });
});
