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
    'find/hod/app/page/search/filters/indexes/hod-indexes-view',
    'find/app/configuration',
    'databases-view/js/hod-databases-collection',
    'jasmine-jquery'
], function(_, $, Backbone, IndexesView, configuration, DatabasesCollection) {
    'use strict';

    describe('Indexes View', function() {
        const DOMAIN = 'TEST';

        const indexId = function(name) {
            return DOMAIN + ':' + name;
        };

        const INDEXES = _.map(['a', 'b', 'c'], function(name) {
            return {name: name, domain: DOMAIN, id: indexId(name)};
        });

        // Convert index collection model attributes to a resource identifier object
        const toResourceIdentifier = _.partial(_.pick, _, 'domain', 'name');

        beforeEach(function() {
            jasmine.clock().install();
            jasmine.clock().mockDate();

            // underscore has already cached Date.now, so isn't using the fake version
            this.originalNow = _.now;
            _.now = Date.now;

            configuration.and.returnValue(function() {
                return {};
            });

            this.indexesCollection = new DatabasesCollection();
            this.selectedIndexesCollection = new DatabasesCollection();
            this.queryModel = new Backbone.Model();

            this.idElement = function(indexAttributes) {
                return this.indexesView.$('li[data-id="' + indexAttributes.id + '"]');
            };
        });

        afterEach(function() {
            jasmine.clock().uninstall();
            _.now = this.originalNow;
        });

        describe('initialized with a populated indexes collection', function() {
            beforeEach(function() {
                this.indexesCollection.reset(INDEXES);
                this.queryModel.set('indexes', _.pluck(INDEXES, 'id'));

                this.indexesView = new IndexesView({
                    queryModel: this.queryModel,
                    indexesCollection: this.indexesCollection,
                    selectedDatabasesCollection: this.selectedIndexesCollection
                });

                this.indexesView.render();
            });

            it('should display indexes in the IndexesCollection', function() {
                const elements = this.indexesView.$el.find('[data-id]');

                const dataIds = _.map(elements, function(element) {
                    return $(element).attr('data-id');
                });

                expect(dataIds).toContain(INDEXES[0].id);
                expect(dataIds).toContain(INDEXES[1].id);
                expect(dataIds).toContain(INDEXES[2].id);
            });
        });

        describe('initialized with an empty indexes collection which is then reset', function() {
            beforeEach(function() {
                this.indexesView = new IndexesView({
                    queryModel: this.queryModel,
                    indexesCollection: this.indexesCollection,
                    selectedDatabasesCollection: this.selectedIndexesCollection
                });

                this.indexesView.render();

                this.indexesCollection.reset(INDEXES);
                this.queryModel.set('indexes', _.pluck(INDEXES, 'id'));
            });

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

                    _.each(INDEXES, function(index) {
                        expect(this.selectedIndexesCollection.findWhere({name: index.name})).toBeDefined();
                    }, this);
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

                    it('should update the selected indexes collection', function() {
                        expect(this.selectedIndexesCollection.toResourceIdentifiers()).toEqual([toResourceIdentifier(INDEXES[0])]);
                    });

                    it('should check the clicked index', function() {
                        const checkedCheckbox = this.idElement(INDEXES[0]).find('i');
                        const uncheckedCheckboxOne = this.idElement(INDEXES[1]).find('i');
                        const uncheckedCheckboxTwo = this.idElement(INDEXES[2]).find('i');

                        expect(checkedCheckbox).toHaveClass('hp-check');
                        expect(uncheckedCheckboxOne).not.toHaveClass('hp-check');
                        expect(uncheckedCheckboxTwo).not.toHaveClass('hp-check');
                    });
                })
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
});
