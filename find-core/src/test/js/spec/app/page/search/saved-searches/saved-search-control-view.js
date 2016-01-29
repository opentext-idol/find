/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/saved-searches/saved-search-control-view',
    'find/app/model/saved-searches/saved-search-model',
    'databases-view/js/databases-collection',
    'moment',
    'i18n!find/nls/bundle'
], function(Backbone, SavedSearchControlView, SavedSearchModel, DatabasesCollection, moment, i18n) {

    describe('SavedSearchControlView', function() {
        beforeEach(function() {
            var queryModel = new Backbone.Model({
                minDate: undefined,
                maxDate: undefined
            });

            var queryTextModel = new Backbone.Model({
                inputText: 'cat',
                relatedConcepts: 'Copenhagen'
            });

            var selectedIndexes = new DatabasesCollection([
                {name: 'Wikipedia', domain: 'PUBLIC'}
            ]);

            var selectedParametricValues = new Backbone.Collection([
                {field: 'WIKIPEDIA_CATEGORY', value: 'Concepts in Physics'}
            ]);

            this.queryState = {
                queryModel: queryModel,
                queryTextModel: queryTextModel,
                selectedIndexes: selectedIndexes,
                selectedParametricValues: selectedParametricValues
            };

            this.savedSearchModel = new SavedSearchModel({
                title: 'New Search',
                queryText: '*'
            });

            this.view = new SavedSearchControlView({
                savedSearchModel: this.savedSearchModel,
                queryModel: queryModel,
                queryTextModel: queryTextModel,
                selectedIndexesCollection: selectedIndexes,
                selectedParametricValues: selectedParametricValues
            });

            this.view.render();
        });

        describe('when the saved search is new', function() {
            it('hides the save button', function() {
                expect(this.view.$('.save-search-button')).toHaveClass('hide');
            });

            it('hides the reset button', function() {
                expect(this.view.$('.search-reset-option')).toHaveClass('hide');
            });

            it('sets the text of the show save as button to "Save Search"', function() {
                expect(this.view.$('.show-save-as-button')).toHaveText(i18n['search.savedSearchControl.openEdit.create']);
            });
        });

        describe('when the search is saved', function() {
            beforeEach(function() {
                this.savedSearchModel.set(_.extend({
                    id: 3,
                    title: 'Quantum Cats'
                }, SavedSearchModel.attributesFromQueryState(this.queryState)));
            });

            it('hides the save button', function() {
                expect(this.view.$('.save-search-button')).toHaveClass('hide');
            });

            it('hides the reset button', function() {
                expect(this.view.$('.search-reset-option')).toHaveClass('hide');
            });

            it('sets the text of the show save as button to "Save As"', function() {
                expect(this.view.$('.show-save-as-button')).toHaveText(i18n['search.savedSearchControl.openEdit.edit']);
            });

            describe('then the query is changed', function() {
                beforeEach(function() {
                    this.queryState.queryModel.set('minDate', moment(15000));
                });

                it('shows the save button', function() {
                    expect(this.view.$('.save-search-button')).not.toHaveClass('hide');
                });

                it('shows the reset button', function() {
                    expect(this.view.$('.search-reset-option')).not.toHaveClass('hide');
                });

                it('sets the text of the show save as button to "Save As"', function() {
                    expect(this.view.$('.show-save-as-button')).toHaveText(i18n['search.savedSearchControl.openEdit.edit']);
                });
            });
        });
    });

});
