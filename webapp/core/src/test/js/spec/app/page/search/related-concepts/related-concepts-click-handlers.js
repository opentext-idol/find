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
    'find/app/page/search/related-concepts/related-concepts-click-handlers'
], function(Backbone, clickHandlers) {
    'use strict';

    describe('Related Concepts Click Handlers', function() {
        describe('updateQuery', function() {
            beforeEach(function() {
                this.conceptGroups = new Backbone.Collection([{concepts: ['baz']}]);

                this.handler = clickHandlers.updateQuery({
                    conceptGroups: this.conceptGroups
                });
            });

            it('creates a new model in the conceptGroups collection', function() {
                this.handler(['foo', 'bar']);

                expect(this.conceptGroups.pluck('concepts')).toEqual([
                    ['baz'],
                    ['"foo"', '"bar"']
                ]);
            });
        });

        describe('newQuery', function() {
            beforeEach(function() {
                this.savedQueryCollection = new Backbone.Collection([{
                    id: 1,
                    queryText: 'cat',
                    relatedConcepts: [['baz']]
                }]);
                const savedSearchModel = this.savedQueryCollection.at(0);

                this.selectedTabModel = new Backbone.Model({selectedSearchCid: savedSearchModel.cid});

                this.handler = clickHandlers.newQuery({
                    savedQueryCollection: this.savedQueryCollection,
                    selectedTabModel: this.selectedTabModel,
                    savedSearchModel: savedSearchModel
                });
            });

            it('creates and selects a new search containing the new concepts', function() {
                this.handler(['foo', 'bar']);

                expect(this.savedQueryCollection.length).toBe(2);

                const newSearch = this.savedQueryCollection.at(1);
                expect(newSearch.get('queryText')).toBe('cat');

                expect(newSearch.get('relatedConcepts')).toEqual([
                    ['baz'],
                    ['"foo"', '"bar"']
                ]);
            });
        });
    });
});
