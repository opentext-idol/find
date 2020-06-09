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
    'underscore',
    'jquery',
    'backbone',
    'find/idol/app/page/search/filters/indexes/idol-indexes-view',
    'databases-view/js/idol-databases-collection',
    'jasmine-jquery'
], function(_, $, Backbone, IndexesView, DatabasesCollection) {
    'use strict';

    describe('Indexes View', function() {
        const INDEXES = _.map(['a', 'b', 'c'], function(name) {
            return {name: name, id: name};
        });

        beforeEach(function() {
            jasmine.clock().install();
            jasmine.clock().mockDate();

            // underscore has already cached Date.now, so isn't using the fake version
            this.originalNow = _.now;
            _.now = Date.now;

            this.indexesCollection = new DatabasesCollection();
            this.selectedIndexesCollection = new DatabasesCollection();

            this.queryModel = new Backbone.Model();

            this.indexesView = new IndexesView({
                queryModel: this.queryModel,
                indexesCollection: this.indexesCollection,
                selectedDatabasesCollection: this.selectedIndexesCollection
            });

            this.idElement = function(indexAttributes) {
                return this.indexesView.$('li[data-id="' + indexAttributes.id + '"]');
            };

            this.indexesView.render();

            this.indexesCollection.reset(INDEXES);
            this.queryModel.set('indexes', _.pluck(INDEXES, 'id'));
        });

        afterEach(function() {
            jasmine.clock().uninstall();
            _.now = this.originalNow;
        });

        describe('after initialization', function() {
            it('should display indexes in the IndexesCollection', function() {
                const elements = this.indexesView.$el.find('[data-id]');

                const dataIds = _.map(elements, function(element) {
                    return $(element).attr('data-id');
                });

                expect(dataIds).toContain(INDEXES[0].id);
                expect(dataIds).toContain(INDEXES[1].id);
                expect(dataIds).toContain(INDEXES[2].id);
            });

            it('sets all indexes on the selected indexes collection', function() {
                expect(this.selectedIndexesCollection.length).toBe(3);
            });

            it('does not select indexes in the UI', function() {
                expect(this.indexesView.$('i.hp-icon hp-check')).toHaveLength(0);
            });

            describe('clicking an index once', function() {
                beforeEach(function() {
                    this.idElement(INDEXES[0]).click();
                });

                it('should not update the selected indexes collection', function() {
                    expect(this.selectedIndexesCollection.length).toBe(3);
                    expect(this.selectedIndexesCollection.at(0).get('name')).toEqual(INDEXES[0].name);
                    expect(this.selectedIndexesCollection.at(1).get('name')).toEqual(INDEXES[1].name);
                    expect(this.selectedIndexesCollection.at(2).get('name')).toEqual(INDEXES[2].name);
                });

                it('should check the clicked index', function() {
                    const checkedCheckbox = this.idElement(INDEXES[0]).find('i');
                    const uncheckedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                    const uncheckedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                    expect(checkedCheckbox).toHaveClass('hp-check');
                    expect(uncheckedCheckboxOne).not.toHaveClass('hp-check');
                    expect(uncheckedCheckboxTwo).not.toHaveClass('hp-check');
                });

                describe('then the debounce timeout elapses', function() {
                    beforeEach(function() {
                        jasmine.clock().tick(1000);
                    });

                    it('updates the selected indexes collection', function() {
                        expect(this.selectedIndexesCollection.length).toBe(1);
                        expect(this.selectedIndexesCollection.first().get('name')).toEqual(INDEXES[0].name);
                    });

                    it('should check the clicked index', function() {
                        const checkedCheckbox = this.idElement(INDEXES[0]).find('i');
                        const uncheckedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                        const uncheckedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                        expect(checkedCheckbox).toHaveClass('hp-check');
                        expect(uncheckedCheckboxOne).not.toHaveClass('hp-check');
                        expect(uncheckedCheckboxTwo).not.toHaveClass('hp-check');
                    });
                });
            });

            describe('clicking an index twice', function() {
                beforeEach(function() {
                    this.idElement(INDEXES[0]).click().click();

                    jasmine.clock().tick(1000);
                });

                it('updates the selected indexes collection with all of the indexes', function() {
                    expect(this.selectedIndexesCollection.length).toBe(3);

                    _.each(INDEXES, function(index) {
                        expect(this.selectedIndexesCollection.findWhere({name: index.name})).toBeDefined();
                    }, this);
                });

                it('should leave the indexes selected in the ui unchanged', function() {
                    expect(this.indexesView.$('i.hp-icon hp-check')).toHaveLength(0);
                });
            });

        });

        describe('when selected indexes collection', function() {
            describe('is set to contain all but the first index', function() {
                beforeEach(function() {
                    this.selectedIndexesCollection.set(_.tail(INDEXES));
                });

                it('should select the right indexes', function() {
                    const uncheckedCheckbox = this.idElement(INDEXES[0]).find('i');
                    const checkedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                    const checkedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                    expect(uncheckedCheckbox).not.toHaveClass('hp-check');
                    expect(checkedCheckboxOne).toHaveClass('hp-check');
                    expect(checkedCheckboxTwo).toHaveClass('hp-check');
                });
            });

            describe('is set to contain only first index', function() {
                beforeEach(function() {
                    this.selectedIndexesCollection.set(_.head(INDEXES));
                });

                it('should select only the first index', function() {
                    const checkedCheckbox = this.idElement(INDEXES[0]).find('i');
                    const uncheckedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                    const uncheckedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                    expect(checkedCheckbox).toHaveClass('hp-check');
                    expect(uncheckedCheckboxOne).not.toHaveClass('hp-check');
                    expect(uncheckedCheckboxTwo).not.toHaveClass('hp-check');
                });
            });
        });
    });
});
