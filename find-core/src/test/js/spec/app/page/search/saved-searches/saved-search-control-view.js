/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'find/app/page/search/saved-searches/saved-search-control-view',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/util/confirm-view',
    'databases-view/js/databases-collection',
    'moment',
    'i18n!find/nls/bundle'
], function(Backbone, $, SavedSearchControlView, SavedSearchModel, MockConfirmView, DatabasesCollection, moment, i18n) {

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

            spyOn(this.savedSearchModel, 'destroy');
            spyOn(this.savedSearchModel, 'save').and.returnValue($.Deferred());

            this.savedSearchCollection = new Backbone.Collection([this.savedSearchModel]);
            spyOn(this.savedSearchCollection, 'create');

            this.view = new SavedSearchControlView({
                savedSearchModel: this.savedSearchModel,
                queryModel: queryModel,
                queryTextModel: queryTextModel,
                savedSearchCollection: this.savedSearchCollection,
                selectedIndexesCollection: selectedIndexes,
                selectedParametricValues: selectedParametricValues
            });

            this.view.render();
        });

        afterEach(function() {
            MockConfirmView.reset();
        });

        describe('when the saved search is new', function() {
            it('hides the save button', function() {
                expect(this.view.$('.save-search-button')).toHaveClass('hide');
            });

            it('hides the reset button', function() {
                expect(this.view.$('.search-reset-option')).toHaveClass('hide');
            });

            it('hides the rename button', function() {
                expect(this.view.$('.show-rename-button')).toHaveClass('hide');
            });

            it('sets the text of the show save as button to "Save Search"', function() {
                expect(this.view.$('.show-save-as-button')).toHaveText(i18n['search.savedSearchControl.openEdit.create']);
            });

            describe('then the "Save Search" button is clicked', function() {
                beforeEach(function() {
                    this.view.$('.show-save-as-button').click();
                });

                it('shows the set title input', function() {
                    expect(this.view.$('.search-title-input-container')).not.toHaveClass('hide');
                    expect(this.view.$('.search-title-input-container .search-title-input')).toHaveLength(1);
                });

                it('activates the "Save Search" button', function() {
                    expect(this.view.$('.show-save-as-button')).toHaveClass('active');
                });

                describe('then the user enters and saves a title', function() {
                    var TITLE = 'Star Wars';

                    beforeEach(function() {
                        this.view.$('.search-title-input-container .search-title-input').val(TITLE).trigger('input');
                        this.view.$('.search-title-input-container .save-title-confirm-button').click();
                    });

                    it('saves the model with the title', function() {
                        expect(this.savedSearchModel.save.calls.count()).toBe(1);
                        expect(this.savedSearchModel.save.calls.argsFor(0)[0].title).toBe(TITLE);
                    });
                });

                describe('then the "Save Search" button is clicked again', function() {
                    beforeEach(function() {
                        this.view.$('.show-save-as-button').click();
                    });

                    it('removes the set title input', function() {
                        expect(this.view.$('.search-title-input-container .search-title-input')).toHaveLength(0);
                    });

                    it('deactivates the "Save Search" button', function() {
                        expect(this.view.$('.show-save-as-button')).not.toHaveClass('active');
                    });

                    // Test we can add, remove and add the search title input correctly (previously broken functionality)
                    describe('then the "Save As" button is clicked a third time', function() {
                        beforeEach(function() {
                            this.view.$('.show-save-as-button').click();
                        });

                        it('shows the set title input', function() {
                            expect(this.view.$('.search-title-input-container')).not.toHaveClass('hide');
                            expect(this.view.$('.search-title-input-container .search-title-input')).toHaveLength(1);
                        });
                    });
                });
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

            it('shows the rename button', function() {
                expect(this.view.$('.show-rename-button')).not.toHaveClass('hide');
            });

            it('sets the text of the show save as button to "Save As"', function() {
                expect(this.view.$('.show-save-as-button')).toHaveText(i18n['search.savedSearchControl.openEdit.edit']);
            });

            describe('then the "Save As" button is clicked', function() {
                beforeEach(function() {
                    this.view.$('.show-save-as-button').click();
                });

                it('shows the set title input', function() {
                    expect(this.view.$('.search-title-input-container')).not.toHaveClass('hide');
                    expect(this.view.$('.search-title-input-container .search-title-input')).toHaveLength(1);
                });

                it('activates the "Save As" button', function() {
                    expect(this.view.$('.show-save-as-button')).toHaveClass('active');
                });

                describe('then the "Save As" button is clicked again', function() {
                    beforeEach(function() {
                        this.view.$('.show-save-as-button').click();
                    });

                    it('removes the set title input', function() {
                        expect(this.view.$('.search-title-input-container .search-title-input')).toHaveLength(0);
                    });

                    it('deactivates the "Save As" button', function() {
                        expect(this.view.$('.show-save-as-button')).not.toHaveClass('active');
                    });
                });

                describe('then a new title is input and the "Rename" button is clicked', function() {
                    beforeEach(function() {
                        this.view.$('.search-title-input-container .search-title-input').val('Star Wars').trigger('input');
                        this.view.$('.show-rename-button').click();
                    });

                    it('the user input is not lost', function() {
                        expect(this.view.$('.search-title-input-container .search-title-input')).toHaveValue('Star Wars');
                    });

                    it('deactivates the "Save As" button', function() {
                        expect(this.view.$('.show-save-as-button')).not.toHaveClass('active');
                    });

                    it('activates the "Rename" button', function() {
                        expect(this.view.$('.show-rename-button')).toHaveClass('active');
                    });
                });

                describe('then the user enters and saves a new title', function() {
                    var NEW_TITLE = 'Star Wars';

                    beforeEach(function() {
                        this.view.$('.search-title-input-container .search-title-input').val(NEW_TITLE).trigger('input');
                        this.view.$('.search-title-input-container .save-title-confirm-button').click();
                    });

                    it('creates a new model in the collection with the new title', function() {
                        expect(this.savedSearchCollection.create.calls.count()).toBe(1);
                        expect(this.savedSearchCollection.create.calls.argsFor(0)[0].title).toBe(NEW_TITLE);
                    });
                });

                describe('then the user clicks the cancel button', function() {
                    beforeEach(function() {
                        this.view.$('.search-title-input-container .save-title-cancel-button').click();
                    });

                    it('removes the set title input', function() {
                        expect(this.view.$('.search-title-input-container .search-title-input')).toHaveLength(0);
                    });

                    it('deactivates the "Save As" button', function() {
                        expect(this.view.$('.show-save-as-button')).not.toHaveClass('active');
                    });
                });
            });

            describe('then the "Rename" button is clicked', function() {
                beforeEach(function() {
                    this.view.$('.show-rename-button').click();
                });

                it('shows the set title input', function() {
                    expect(this.view.$('.search-title-input-container')).not.toHaveClass('hide');
                    expect(this.view.$('.search-title-input-container .search-title-input')).toHaveLength(1);
                });

                it('activates the "Rename" button', function() {
                    expect(this.view.$('.show-rename-button')).toHaveClass('active');
                });

                describe('then the "Rename" button is clicked again', function() {
                    beforeEach(function() {
                        this.view.$('.show-rename-button').click();
                    });

                    it('removes the set title input', function() {
                        expect(this.view.$('.search-title-input-container .search-title-input')).toHaveLength(0);
                    });

                    it('deactivates the "Rename" button', function() {
                        expect(this.view.$('.show-rename-button')).not.toHaveClass('active');
                    });
                });

                describe('then a new title is input and the "Save As" button is clicked', function() {
                    beforeEach(function() {
                        this.view.$('.search-title-input-container .search-title-input').val('Star Wars').trigger('input');
                        this.view.$('.show-save-as-button').click();
                    });

                    it('the user input is not lost', function() {
                        expect(this.view.$('.search-title-input-container .search-title-input')).toHaveValue('Star Wars');
                    });

                    it('deactivates the "Rename" button', function() {
                        expect(this.view.$('.show-rename-button')).not.toHaveClass('active');
                    });

                    it('activates the "Save As" button', function() {
                        expect(this.view.$('.show-save-as-button')).toHaveClass('active');
                    });
                });

                describe('then the user enters and saves a new title', function() {
                    var NEW_TITLE = 'Star Wars';

                    beforeEach(function() {
                        this.view.$('.search-title-input-container .search-title-input').val(NEW_TITLE).trigger('input');
                        this.view.$('.search-title-input-container .save-title-confirm-button').click();
                    });

                    it('saves the new title on the model', function() {
                        expect(this.savedSearchModel.save.calls.count()).toBe(1);
                        expect(this.savedSearchModel.save.calls.argsFor(0)[0].title).toBe(NEW_TITLE);
                    });
                });

                describe('then the user clicks the cancel button', function() {
                    beforeEach(function() {
                        this.view.$('.search-title-input-container .save-title-cancel-button').click();
                    });

                    it('removes the set title input', function() {
                        expect(this.view.$('.search-title-input-container .search-title-input')).toHaveLength(0);
                    });

                    it('deactivates the "Rename" button', function() {
                        expect(this.view.$('.show-rename-button')).not.toHaveClass('active');
                    });
                });
            });

            describe('then the delete button is clicked', function() {
                beforeEach(function() {
                    this.view.$('.saved-search-delete-option').click();
                });

                it('does not destroy the saved search model', function() {
                    expect(this.savedSearchModel.destroy).not.toHaveBeenCalled();
                });

                it('opens a confirm modal', function() {
                    expect(MockConfirmView.instances.length).toBe(1);
                });

                describe('then the modal confirm button is clicked', function() {
                    beforeEach(function() {
                        MockConfirmView.instances[0].constructorArgs[0].okHandler();
                    });

                    it('destroys the saved search model', function() {
                        expect(this.savedSearchModel.destroy).toHaveBeenCalled();
                    });
                });
            });

            describe('then the query is changed', function() {
                var MIN_DATE = 15000;

                beforeEach(function() {
                    this.queryState.queryModel.set('minDate', moment(MIN_DATE));
                });

                it('shows the save button', function() {
                    expect(this.view.$('.save-search-button')).not.toHaveClass('hide');
                });

                it('shows the reset button', function() {
                    expect(this.view.$('.search-reset-option')).not.toHaveClass('hide');
                });

                it('shows the rename button', function() {
                    expect(this.view.$('.show-rename-button')).not.toHaveClass('hide');
                });

                it('sets the text of the show save as button to "Save As"', function() {
                    expect(this.view.$('.show-save-as-button')).toHaveText(i18n['search.savedSearchControl.openEdit.edit']);
                });

                describe('then the save button is clicked', function() {
                    beforeEach(function() {
                        this.view.$('.save-search-button').click();
                    });

                    it('saves the new query on the saved search model', function() {
                        expect(this.savedSearchModel.save).toHaveBeenCalled();

                        var savedMinDate = this.savedSearchModel.save.calls.argsFor(0)[0].minDate;
                        expect(savedMinDate.valueOf()).toBe(MIN_DATE);
                    });
                });

                describe('then the reset button is clicked', function() {
                    beforeEach(function() {
                        this.view.$('.search-reset-option').click();
                    });

                    it('opens a confirm modal', function() {
                        expect(MockConfirmView.instances.length).toBe(1);
                    });

                    describe('then the modal confirm button is clicked', function() {
                        beforeEach(function() {
                            MockConfirmView.instances[0].constructorArgs[0].okHandler();
                        });

                        it('restores the original query state', function() {
                            expect(this.queryState.queryModel.get('minDate')).toBeUndefined();
                        });
                    });
                });
            });
        });
    });

});
