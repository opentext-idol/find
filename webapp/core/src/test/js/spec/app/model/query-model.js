/*
 * Copyright 2017 Open Text.
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
    'backbone',
    'find/app/configuration',
    'find/app/model/query-model',
    'parametric-refinement/selected-values-collection',
    'find/app/model/dates-filter-model',
    'find/app/model/geography-model',
    'find/app/model/document-selection-model'
], function (
    Backbone, configuration, QueryModel, SelectedValuesCollection,
    DatesFilterModel, GeographyModel, DocumentSelectionModel
) {
    'use strict';

    const makeDefaultQueryState = function () {
        return {
            datesFilterModel: new DatesFilterModel(),
            minScoreModel: new Backbone.Model({minScore: 0}),
            selectedIndexes: new Backbone.Collection(),
            selectedParametricValues: new SelectedValuesCollection(),
            conceptGroups: new Backbone.Collection([
                {concepts: ['cat']}
            ]),
            geographyModel: new GeographyModel(),
            documentSelectionModel: new DocumentSelectionModel()
        };
    }

    describe('QueryModel', function() {

        beforeEach(function () {
            configuration.and.returnValue({
                referenceField: 'CUSTOMREF',
                search: {
                    defaultSortOption: 'labeledSort',
                    sortOptions: {
                        unlabeledSort: { sort: 'unlabeled', label: null },
                        labeledSort: { sort: 'labeled', label: 'the label' }
                    }
                }
            });
        });

        describe('defaults', function () {

            beforeEach(function () {
                this.queryState = makeDefaultQueryState();
                this.queryModel = new QueryModel({}, {
                    enableAutoCorrect: true,
                    queryState: this.queryState
                });
            });

            it('uses default sort option from config', function () {
                expect(this.queryModel.get('sort')).toBe('labeled');
                expect(this.queryModel.get('questionText')).toBe('cat');
            });

            it('updates questionText when concepts change', function () {
                this.queryState.conceptGroups.reset([{ concepts: ['dog'] }]);
                expect(this.queryModel.get('questionText')).toBe('dog');
            });

            it('sets null questionText with empty concepts list', function () {
                this.queryState.conceptGroups.reset([]);
                expect(this.queryModel.get('questionText')).toBeNull();
            });

            it('sets null questionText with multiple concept groups', function () {
                this.queryState.conceptGroups.reset([{ concepts: ['cat'] }, { concepts: ['dog'] }]);
                expect(this.queryModel.get('questionText')).toBeNull();
            });

            it('sets questionText to first concept term in group', function () {
                this.queryState.conceptGroups.reset([{ concepts: ['cat', 'dog'] }]);
                expect(this.queryModel.get('questionText')).toBe('cat');
            });

        });

        describe('with auto-correct enabled', function() {
            beforeEach(function() {
                this.queryState = makeDefaultQueryState();
                this.queryModel = new QueryModel({
                    autoCorrect: true
                }, {
                    enableAutoCorrect: true,
                    queryState: this.queryState
                });
            });

            it('resets the auto-correct flag when the selected concepts are changed', function() {
                this.queryModel.set('autoCorrect', false);
                this.queryState.conceptGroups.reset();

                expect(this.queryModel.get('autoCorrect')).toBe(true);
            });
        });

        describe('with auto-correct disabled', function() {
            beforeEach(function() {
                this.queryState = makeDefaultQueryState();
                this.queryModel = new QueryModel({
                    autoCorrect: false
                }, {
                    enableAutoCorrect: false,
                    queryState: this.queryState
                });
            });

            it('does not change the auto-correct flag when the selected concepts are changed', function() {
                this.queryState.conceptGroups.reset();

                expect(this.queryModel.get('autoCorrect')).toBe(false);
            });
        });
    });
});
