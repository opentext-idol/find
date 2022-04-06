/*
 * (c) Copyright 2020 Micro Focus or one of its affiliates.
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
    'find/app/configuration',
    'find/app/page/search/sort-view'
], function (Backbone, configuration, SortView) {
    'use strict';

    describe('SortView', function () {

        beforeEach(function () {
            configuration.and.returnValue({
                search: {
                    defaultSortOption: 'labeledSort',
                    sortOptions: {
                        unlabeledSort: { sort: 'unlabeled', label: null },
                        labeledSort: { sort: 'labeled', label: 'the label' }
                    }
                }
            });

            this.queryModel = new Backbone.Model({ sort: 'labeled' });
            this.sortView = new SortView({ queryModel: this.queryModel });
            this.sortView.render();
        });

        it('should show sort options', function () {
            const sortOptionLabels = this.sortView.$('.search-results-sort li a')
                .map(function () { return this.innerHTML; })
                .get();
            expect(sortOptionLabels.length).toBe(2);
            expect(sortOptionLabels).toContain('the label');
            expect(sortOptionLabels).toContain('unlabeledSort');
        });

        it('should show default sort option as current', function () {
            expect(this.sortView.$('.current-search-sort').text()).toBe('the label');
        });

        describe('when another sort option is selected', function () {

            beforeEach(function () {
                this.sortView.$('.search-results-sort li a')
                    .filter(function () { return this.innerHTML === 'unlabeledSort'; })
                    .click();
            });

            it('should update the query model', function () {
                expect(this.queryModel.get('sort')).toBe('unlabeled');
            });

            it('should show the selection sort option as current', function () {
                expect(this.sortView.$('.current-search-sort').text()).toBe('unlabeledSort');
            });

        });

    });

});
