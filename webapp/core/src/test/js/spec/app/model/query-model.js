/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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

    describe('QueryModel', function() {

        beforeEach(function () {
            configuration.and.callFake(function () {
                return {
                    referenceField: 'CUSTOMREF'
                };
            });
        });

        describe('with auto-correct enabled', function() {
            beforeEach(function() {
                this.queryState = {
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
                this.queryState = {
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
