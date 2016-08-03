/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'find/app/page/search/saved-searches/saved-search-control-view',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/model/dates-filter-model',
    'find/app/model/min-score-model',
    'find/app/util/confirm-view',
    'find/app/util/database-name-resolver',
    'moment',
    'i18n!find/nls/bundle'
], function(Backbone, $, SavedSearchControlView, SavedSearchModel, DatesFilterModel, MinScoreModel, MockConfirmView, databaseNameResolver, moment, i18n) {

    var CREATE_TEXT = 'Make a new one!';
    var EDIT_TEXT = 'Change an old one!';

    function checkPopoverExists(view) {
        return Boolean(view.$('.popover').length);
    }

    function checkButtonEnabled(view) {
        return !view.$('.popover-control').hasClass('disabled');
    }

    function saveDisabled(view) {
        return view.$('.save-search-button').hasClass('disabled not-clickable') &&
            view.$('.show-save-as').hasClass('disabled not-clickable');
    }

    function testDeactivatesTheSaveSearchButton() {
        it('deactivates the "Save Search" button', function() {
            expect(this.view.$('.show-save-as')).not.toHaveClass('active');
        });
    }

    function testDeactivatesTheSaveAsButton() {
        it('deactivates the "Save As" button', function() {
            expect(this.view.$('.show-save-as')).not.toHaveClass('active');
        });
    }

    function testDisablesOptionButton(className, name) {
        it('disables the ' + name + ' button', function() {
            expect(this.view.$(className)).toHaveClass('disabled');
        });
    }

    function testEnablesOptionButton(className, name) {
        describe('enables option button', function() {
            beforeEach(function(done) {
                var view = this.view;

                var intervalId = setInterval(function() {
                    if (checkButtonEnabled(view)) {
                        clearTimeout(intervalId);
                        done();
                    }
                }, 50);
            });

            it('enables the ' + name + ' button', function() {
                expect(this.view.$(className)).not.toHaveClass('disabled');
            });
        });
    }

    function assertPopoverLength(length) {
        expect(this.view.$('.popover')).toHaveLength(length);
        expect(this.view.$('.popover-content .search-title-input-container')).toHaveLength(length);
        expect(this.view.$('.popover-content .search-title-input-container .search-title-input')).toHaveLength(length);
    }

    function testRemovesTheSetTitleInput() {
        describe('shows the popover with set title input form', function() {
            beforeEach(function(done) {
                var view = this.view;

                var intervalId = setInterval(function() {
                    if (!checkPopoverExists(view)) {
                        clearTimeout(intervalId);
                        done();
                    }
                }, 50);
            });

            it('removes the popover with set title input form', function() {
                assertPopoverLength.call(this, 0);
            });
        });
    }

    function testShowsTheSetTitleInput() {
        it('shows the popover with set title input form', function() {
            assertPopoverLength.call(this, 1);
        });
    }

    function testHidesTheSaveButton() {
        it('hides the save button', function() {
            expect(this.view.$('.save-search-button')).toHaveClass('hide');
        });
    }

    function testHidesTheResetButton() {
        it('hides the reset button', function() {
            expect(this.view.$('.search-reset-option')).toHaveClass('hide');
        });
    }

    function testShowsTheRenameButton() {
        it('shows the rename button', function() {
            expect(this.view.$('.show-rename-button')).not.toHaveClass('hide');
        });
    }

    function clickSaveAsQueryButton() {
        this.view.$('.show-save-as[data-search-type="QUERY"]').click();
    }

    function clickSaveAsSnapshotButton() {
        this.view.$('.show-save-as[data-search-type="SNAPSHOT"]').click();
    }

    function clickShowRename() {
        this.view.$('.show-rename-button').click();
    }

    function clickSaveSearchButton() {
        this.view.$('.save-search-button').click();
    }

    describe('SavedSearchControlView', function() {
        beforeEach(function() {
            this.queryModel = new Backbone.Model({
                queryText: 'cat AND Copenhagen'
            });

            var queryTextModel = new Backbone.Model({
                inputText: 'cat',
                relatedConcepts: [['Copenhagen']]
            });

            this.documentsCollection = new Backbone.Collection();
            this.documentsCollection.totalResults = 50;

            var selectedIndexes = new Backbone.Collection([
                {name: 'Wikipedia', domain: 'PUBLIC'}
            ]);
            databaseNameResolver.getDatabaseInfoFromCollection.and.callFake(function () {
                return [{name: 'Wikipedia', domain: 'PUBLIC'}];
            });

            var selectedParametricValues = new Backbone.Collection([
                {field: 'WIKIPEDIA_CATEGORY', value: 'Concepts in Physics'}
            ]);

            var datesFilterModel = new DatesFilterModel({
                dateRange: null,
                customMinDate: null,
                customMaxDate: null
            });

            var minScoreModel = new MinScoreModel({
                minScore: 20
            });

            this.queryState = {
                datesFilterModel: datesFilterModel,
                queryTextModel: queryTextModel,
                selectedIndexes: selectedIndexes,
                selectedParametricValues: selectedParametricValues,
                minScoreModel: minScoreModel
            };

            this.savedSearchModel = new SavedSearchModel({
                title: 'New Search',
                type: SavedSearchModel.Type.QUERY,
                queryText: '*'
            });

            spyOn(this.savedSearchModel, 'destroy');
            spyOn(this.savedSearchModel, 'save').and.returnValue($.Deferred());

            this.selectedTabModel = new Backbone.Model({
                selectedSearchCid: this.savedSearchModel.cid
            });

            this.savedQueryCollection = new Backbone.Collection();
            this.savedSearchCollection = new Backbone.Collection();
            this.savedSnapshotCollection = new Backbone.Collection();

            this.viewOptions = {
                savedSearchModel: this.savedSearchModel,
                queryModel: this.queryModel,
                documentsCollection: this.documentsCollection,
                savedSearchCollection: this.savedSearchCollection,
                queryState: this.queryState,
                selectedTabModel: this.selectedTabModel,
                searchCollections: {
                    QUERY: this.savedQueryCollection,
                    SNAPSHOT: this.savedSnapshotCollection
                },
                searchTypes: {
                    QUERY: {
                        isMutable: true,
                        openEditText: {
                            create: CREATE_TEXT,
                            edit: EDIT_TEXT
                        }
                    },
                    SNAPSHOT: {
                        isMutable: false,
                        openEditText: {
                            create: CREATE_TEXT,
                            edit: EDIT_TEXT
                        }
                    }
                }
            };
        });

        afterEach(function() {
            MockConfirmView.reset();
        });

        describe('when the search is a previously saved query and the view renders while the documents collection is fetching', function() {
            beforeEach(function() {
                this.savedSearchModel.set(_.extend({
                    id: 3,
                    title: 'Quantum Cats'
                }, SavedSearchModel.attributesFromQueryState(this.queryState)));

                this.savedQueryCollection.add(this.savedSearchModel);
                this.savedSearchCollection.add(this.savedSearchModel);

                this.documentsCollection.currentRequest = true;

                this.view = new SavedSearchControlView(this.viewOptions);
                this.view.render();
                $('body').append(this.view.$el);
            });

            afterEach(function() {
                this.view.remove();
            });

            it('saving should be disabled', function() {
                expect(saveDisabled(this.view)).toBe(true);
            });

            describe('and the documents collection syncs', function() {
                beforeEach(function() {
                    this.documentsCollection.trigger('sync');
                });

                it('saving should be enabled', function() {
                    expect(saveDisabled(this.view)).toBe(false);
                });
            });

            describe('and the documents collection errors', function() {
                beforeEach(function() {
                    this.documentsCollection.trigger('error');
                });

                it('saving should be disabled', function() {
                    expect(saveDisabled(this.view)).toBe(true);
                });
            });

            describe('and the documents collection makes a request', function() {
                beforeEach(function() {
                    this.documentsCollection.trigger('request');
                });

                it('saving should be disabled', function() {
                    expect(saveDisabled(this.view)).toBe(true);
                });
            });
        });

        describe('when the saved search is new', function() {
            beforeEach(function() {
                this.savedQueryCollection.add(this.savedSearchModel);
                this.savedSearchCollection.add(this.savedSearchModel);

                this.view = new SavedSearchControlView(this.viewOptions);
                this.view.render();
                $('body').append(this.view.$el);
            });

            afterEach(function() {
                this.view.remove();
            });

            testHidesTheSaveButton();
            testHidesTheResetButton();

            it('hides the rename button', function() {
                expect(this.view.$('.show-rename-button')).toHaveClass('hide');
            });

            it('sets the text of the show save as button to "Save Search"', function() {
                expect(this.view.$('.show-save-as[data-search-type="QUERY"]')).toHaveText(CREATE_TEXT);
            });

            it('shows the "Save As" button', function() {
                expect(this.view.$('.show-save-as[data-search-type="QUERY"]')).toHaveLength(1);
            });

            it('does not show the "Open as Query" button', function() {
                expect(this.view.$('.open-as-query-option')).toHaveLength(0);
            });

            describe('then the "Save Search" button is clicked', function() {
                beforeEach(clickSaveAsQueryButton);

                testShowsTheSetTitleInput();

                it('activates the "Save Query" button', function() {
                   expect(this.view.$('.show-save-as[data-search-type="QUERY"]')).toHaveClass('active');
                });

                describe('then the user enters and saves a title', function() {
                    var TITLE = 'Star Wars';
                    var TYPE = SavedSearchModel.Type.QUERY;

                    beforeEach(function() {
                        this.view.$('.popover-content .search-title-input-container .search-title-input').val(TITLE).trigger('input');
                        this.view.$('.popover-content .search-title-input-container .save-title-confirm-button').click();
                    });

                    it('saves the model with the title', function() {
                        expect(this.savedSearchModel.save.calls.count()).toBe(1);
                        expect(this.savedSearchModel.save.calls.argsFor(0)[0].title).toBe(TITLE);
                        expect(this.savedSearchModel.save.calls.argsFor(0)[0].type).toBe(TYPE);
                    });
                });

                describe('then the "Save Search" button is clicked again', function() {
                    beforeEach(clickSaveAsQueryButton);

                    testRemovesTheSetTitleInput();
                    testDeactivatesTheSaveSearchButton();

                    // Test we can add, remove and add the search title input correctly (previously broken functionality)
                    describe('then the "Save As" button is clicked a third time', function() {
                        beforeEach(clickSaveAsQueryButton);

                        testShowsTheSetTitleInput();
                    });
                });
            });
        });

        describe('when the search is saved as a snapshot', function() {
            beforeEach(function() {
                this.savedSearchModel.set(_.extend({
                    id: 3,
                    title: 'Quantum Cats',
                    type: SavedSearchModel.Type.SNAPSHOT
                }, SavedSearchModel.attributesFromQueryState(this.queryState)));

                this.savedSnapshotCollection.add(this.savedSearchModel);
                this.savedSearchCollection.add(this.savedSearchModel);

                this.view = new SavedSearchControlView(this.viewOptions);
                this.view.render();
                $('body').append(this.view.$el);
            });

            afterEach(function() {
                this.view.remove();
            });

            testShowsTheRenameButton();

            it('does not show the "Save as query" or "Save as snapshot" button', function() {
                expect(this.view.$('.show-save-as[data-search-type="QUERY"]')).toHaveLength(0);
                expect(this.view.$('.show-save-as[data-search-type="SNAPSHOT"]')).toHaveLength(0);
            });

            it('displays the "Open as Query" button', function() {
                expect(this.view.$('.open-as-query-option')).toHaveLength(1);
            });

            describe('when the open as query button is clicked', function() {
                beforeEach(function() {
                    this.view.$('.open-as-query-option').click();
                });

                it('adds a new saved query to the collection', function() {
                    expect(this.savedQueryCollection.length).toBe(1);
                });

                it('creates a new saved search model', function() {
                    expect(this.savedQueryCollection.at(0).isNew()).toBe(true);
                });

                it('copies the query restriction parameters to the new model', function() {
                    var model = this.savedQueryCollection.at(0);
                    expect(model.get('queryText')).toBe('cat');
                    expect(model.get('indexes').length).toBe(1);
                    expect(model.get('minScore')).toBe(20);
                });

                it('switches to the new query tab', function() {
                    expect(this.selectedTabModel.get('selectedSearchCid')).toBe(this.savedQueryCollection.at(0).cid);
                });
            });

            describe('when the snapshot is renamed', function() {
                var NEW_TITLE = 'The new title for my snapshot';

                beforeEach(function() {
                    clickShowRename.call(this);
                    this.view.$('.popover-content .search-title-input').val(NEW_TITLE).trigger('input');
                    this.view.$('.popover-content .search-title-form').submit();
                });

                it('saves the model with the new title', function() {
                    expect(this.savedSearchModel.save).toHaveBeenCalled();
                });
            });
        });

        describe('when the search is saved as a query', function() {
            beforeEach(function() {
                this.savedSearchModel.set(_.extend({
                    id: 3,
                    title: 'Quantum Cats'
                }, SavedSearchModel.attributesFromQueryState(this.queryState)));

                this.savedQueryCollection.add(this.savedSearchModel);
                this.savedSearchCollection.add(this.savedSearchModel);

                this.view = new SavedSearchControlView(this.viewOptions);
                this.view.render();
                $('body').append(this.view.$el);
            });

            afterEach(function() {
                this.view.remove();
            });

            testHidesTheSaveButton();
            testHidesTheResetButton();
            testShowsTheRenameButton();

            it('does not show the "Open as Query" button', function() {
                expect(this.view.$('.open-as-query-option')).toHaveLength(0);
            });

            it('shows the "Save Query" button', function() {
                expect(this.view.$('.show-save-as[data-search-type="QUERY"]')).toHaveLength(1);
            });

            it('shows the "Save As Snapshot" button', function() {
                expect(this.view.$('.show-save-as[data-search-type="SNAPSHOT"]')).toHaveLength(1);
            });

            it('sets the text of the show save as button to the edit text', function() {
                expect(this.view.$('.show-save-as[data-search-type="QUERY"]')).toHaveText(EDIT_TEXT);
            });

            describe('then the "Save as query" button is clicked', function() {
                beforeEach(clickSaveAsQueryButton);

                testShowsTheSetTitleInput();

                it('activates the "Save as query" button and disabled rename button', function() {
                    expect(this.view.$('.show-save-as[data-search-type="QUERY"]')).toHaveClass('active');
                    testDisablesOptionButton('.show-rename-button', 'rename button');
                });

                describe('then the "Save as query" button is clicked again', function() {
                    beforeEach(clickSaveAsQueryButton);

                    testRemovesTheSetTitleInput();
                    testDeactivatesTheSaveAsButton();
                    testEnablesOptionButton('.show-rename-button', 'rename button');
                });

                describe('then a new title is input, then cancel is clicked, then "Rename" button is clicked', function() {
                    beforeEach(function() {
                        this.view.$('.popover-content .search-title-input-container .search-title-input').val('Star Wars').trigger('input');
                        this.view.$('.save-title-cancel-button').click();
                        clickShowRename.call(this);
                    });

                    it('the user input is reset to original value', function() {
                        expect(this.view.$('.popover-content .search-title-input-container .search-title-input')).toHaveValue('Quantum Cats');
                    });

                    it('activates the "Rename" button and disables "Save as query" button', function() {
                        expect(this.view.$('.show-rename-button')).toHaveClass('active');
                        testDisablesOptionButton('.show-save-as[data-search-type="QUERY"]', 'save as query button');
                    });
                });

                describe('then the user enters a title and clicks "Save"', function() {
                    var NEW_TITLE = 'Star Wars';

                    beforeEach(function() {
                        var createdQueries = this.createdQueries = [];

                        spyOn(this.savedQueryCollection, 'create').and.callFake(function(attributes) {
                            var model = new Backbone.Model(attributes);
                            createdQueries.push(model);
                            return model;
                        });

                        this.view.$('.popover-content .search-title-input-container .search-title-input').val(NEW_TITLE).trigger('input');
                        this.view.$('.popover-content .search-title-input-container .save-title-confirm-button').click();
                    });

                    it('creates a new model in the collection with the correct title', function() {
                        expect(this.savedQueryCollection.create.calls.count()).toBe(1);
                        expect(this.savedQueryCollection.create.calls.argsFor(0)[0].title).toBe(NEW_TITLE);
                    });

                    describe('then the save request succeeds', function() {
                        beforeEach(function() {
                            this.savedQueryCollection.create.calls.argsFor(0)[1].success(this.createdQueries[0]);
                        });

                        it('selects the new tab', function() {
                            expect(this.selectedTabModel.get('selectedSearchCid')).toBe(this.createdQueries[0].cid);
                        });
                    });
                });

                describe('then the user clicks the cancel button', function() {
                    beforeEach(function() {
                        this.view.$('.popover-content .search-title-input-container .save-title-cancel-button').click();
                    });

                    testRemovesTheSetTitleInput();
                    testDeactivatesTheSaveAsButton();
                    testEnablesOptionButton('.show-save-as[data-search-type="QUERY"]', 'save as query button');
                });
            });

            describe('then the "Save as Snapshot" button is clicked', function() {
                beforeEach(clickSaveAsSnapshotButton);

                testShowsTheSetTitleInput();

                it('activates the "Save as snapshot" button and disabled rename button', function() {
                    expect(this.view.$('.show-save-as[data-search-type="SNAPSHOT"]')).toHaveClass('active');
                    testDisablesOptionButton('.show-rename-button', 'rename button');
                });

                describe('then the "Save as snapshot" button is clicked again', function() {
                    beforeEach(clickSaveAsSnapshotButton);

                    testRemovesTheSetTitleInput();
                    testDeactivatesTheSaveAsButton();
                    testEnablesOptionButton('.show-rename-button', 'rename button');
                });

                describe('then a new title is input, then cancel is clicked, then "Rename" button is clicked', function() {
                    beforeEach(function() {
                        this.view.$('.popover-content .search-title-input-container .search-title-input').val('Star Wars').trigger('input');
                        this.view.$('.save-title-cancel-button').click();
                        clickShowRename.call(this);
                    });

                    it('the user input is reset to original value', function() {
                        expect(this.view.$('.popover-content .search-title-input-container .search-title-input')).toHaveValue('Quantum Cats');
                    });

                    it('activates the "Rename" button and disables "Save as snapshot" button', function() {
                        expect(this.view.$('.show-rename-button')).toHaveClass('active');
                        testDisablesOptionButton('.show-save-as[data-search-type="SNAPSHOT"]', 'save as snapshot button');
                    });
                });

                describe('then the user presses "Save snapshot" and clicks "Save"', function() {
                    var NEW_TITLE = 'Star Wars';

                    beforeEach(function() {
                        var createdSnapshots = this.createdSnapshots = [];

                        spyOn(this.savedSnapshotCollection, 'create').and.callFake(function(attributes) {
                            var model = new Backbone.Model(attributes);
                            createdSnapshots.push(model);
                            return model;
                        });

                        this.view.$('.popover-content .search-title-input-container .search-title-input').val(NEW_TITLE).trigger('input');
                        this.view.$('.popover-content .search-title-input-container .save-title-confirm-button').click();
                    });

                    it('creates a new model in the collection with the correct title', function() {
                        expect(this.savedSnapshotCollection.create.calls.count()).toBe(1);
                        expect(this.savedSnapshotCollection.create.calls.argsFor(0)[0].title).toBe(NEW_TITLE);
                    });

                    describe('then the save request succeeds', function() {
                        beforeEach(function() {
                            this.savedSnapshotCollection.create.calls.argsFor(0)[1].success(this.createdSnapshots[0]);
                        });

                        it('does not select the new tab', function() {
                            expect(this.selectedTabModel.get('selectedSearchCid')).toBe(this.savedSearchModel.cid);
                        });
                    });
                });

                describe('then the user clicks the cancel button', function() {
                    beforeEach(function() {
                        this.view.$('.popover-content .search-title-input-container .save-title-cancel-button').click();
                    });

                    testRemovesTheSetTitleInput();
                    testDeactivatesTheSaveAsButton();
                    testEnablesOptionButton('.show-save-as[data-search-type="SNAPSHOT"]', 'save as snapshot button');
                });
            });

            describe('then the "Rename" button is clicked', function() {
                beforeEach(clickShowRename);

                testShowsTheSetTitleInput();

                it('activates the "Rename" button', function() {
                    expect(this.view.$('.show-rename-button')).toHaveClass('active');
                    testDisablesOptionButton('.show-save-as[data-search-type="QUERY"]', 'save as query button');
                });

                describe('then the "Rename" button is clicked again', function() {
                    beforeEach(clickShowRename);

                    testRemovesTheSetTitleInput();

                    it('deactivates the "Rename" button', function() {
                        expect(this.view.$('.show-rename-button')).not.toHaveClass('active');
                        testEnablesOptionButton('.show-save-as[data-search-type="QUERY"]', 'save as query button');
                    });
                });

                describe('then a new title is input, then cancel button is clicked, then "Save As" button is clicked', function() {
                    beforeEach(function() {
                        this.view.$('.popover-content .search-title-input-container .search-title-input').val('Star Wars').trigger('input');
                        this.view.$('.save-title-cancel-button').click();
                        clickSaveAsQueryButton.call(this);
                    });

                    it('the user input is reset to original value', function() {
                        expect(this.view.$('.popover-content .search-title-input-container .search-title-input')).toHaveValue('Quantum Cats');
                    });

                    it('deactivates the "Rename" button', function() {
                        expect(this.view.$('.show-rename-button')).not.toHaveClass('active');
                    });

                    it('activates the "Save as query" button', function() {
                        expect(this.view.$('.show-save-as[data-search-type="QUERY"]')).toHaveClass('active');
                        testDisablesOptionButton('.show-rename-button', 'rename button');
                    });
                });

                describe('then the user enters and saves a new title', function() {
                    var NEW_TITLE = 'Star Wars';

                    beforeEach(function() {
                        this.view.$('.popover-content .search-title-input-container .search-title-input').val(NEW_TITLE).trigger('input');
                        this.view.$('.popover-content .search-title-input-container .save-title-confirm-button').click();
                    });

                    it('saves the new title on the model', function() {
                        expect(this.savedSearchModel.save.calls.count()).toBe(1);
                        expect(this.savedSearchModel.save.calls.argsFor(0)[0].title).toBe(NEW_TITLE);
                    });
                });

                describe('then the user clicks the cancel button', function() {
                    beforeEach(function() {
                        this.view.$('.popover-content .search-title-input-container .save-title-cancel-button').click();
                    });

                    testRemovesTheSetTitleInput();

                    it('deactivates the "Rename" button', function() {
                        expect(this.view.$('.show-rename-button')).not.toHaveClass('active');
                        testDisablesOptionButton('.show-rename-button', 'rename button');
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

                    describe('then the destroy fails', function() {
                        beforeEach(function() {
                            this.savedSearchModel.destroy.calls.argsFor(0)[0].error({status: 500});
                        });

                        it('shows an error message', function() {
                            expect(this.view.$('.search-controls-error-message')).toHaveText(i18n['search.savedSearches.deleteFailed']);
                        });

                        describe('then the "Save As" button is clicked', function() {
                            beforeEach(clickSaveAsQueryButton);

                            it('removes the error message', function() {
                                expect(this.view.$('.search-controls-error-message')).toHaveText('');
                            });
                        });
                    });
                });
            });

            describe('then the query is changed', function() {
                beforeEach(function() {
                    this.queryState.queryTextModel.set({inputText: 'archipelago'});
                    this.queryModel.set('queryText', 'archipelago');
                });

                it('shows the save button', function() {
                    expect(this.view.$('.save-search-button')).not.toHaveClass('hide');
                });

                it('shows the reset button', function() {
                    expect(this.view.$('.search-reset-option')).not.toHaveClass('hide');
                });

                testShowsTheRenameButton();

                it('sets the text of the show save as button to "Save as query"', function() {
                    expect(this.view.$('.show-save-as[data-search-type="QUERY"]')).toHaveText(EDIT_TEXT);
                });

                describe('then the save button is clicked', function() {
                    beforeEach(clickSaveSearchButton);

                    it('saves the new query on the saved search model', function() {
                        expect(this.savedSearchModel.save).toHaveBeenCalled();

                        var savedAttributes = this.savedSearchModel.save.calls.argsFor(0)[0];
                        expect(savedAttributes.queryText).toBe('archipelago');
                    });

                    it('disables the save button', function() {
                        expect(this.view.$('.save-search-button')).toHaveProp('disabled', true);
                    });

                    describe('then the save fails', function() {
                        beforeEach(function() {
                            this.savedSearchModel.save.calls.argsFor(0)[1].error({status: 500});
                        });

                        it('enables the save button', function() {
                            expect(this.view.$('.save-search-button')).toHaveProp('disabled', false);
                        });

                        it('displays an error message', function() {
                            expect(this.view.$('.search-controls-error-message')).toHaveText(i18n['search.savedSearchControl.error']);
                        });

                        describe('then the save button is clicked again', function() {
                            beforeEach(clickSaveSearchButton);

                            it('removes the error message', function() {
                                expect(this.view.$('.search-controls-error-message')).toHaveText('');
                            });
                        });
                    });

                    describe('then the save succeeds', function() {
                        beforeEach(function() {
                            this.savedSearchModel.save.calls.argsFor(0)[1].success();
                        });

                        it('enables the save button', function() {
                            expect(this.view.$('.save-search-button')).toHaveProp('disabled', false);
                        });
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
                            expect(this.queryState.queryTextModel.get('inputText')).toBe('cat');
                            expect(this.queryState.queryTextModel.get('relatedConcepts')).toEqual([['Copenhagen']]);
                        });
                    });
                });

                describe('then the "Save As Snapshot" button is clicked and a Snapshot is saved', function() {
                    beforeEach(clickSaveAsQueryButton);

                    describe('and a Snapshot is saved', function() {
                        beforeEach(function() {
                            spyOn(this.savedSnapshotCollection, 'create');

                            this.view.$('.popover-content .search-title-input-container .search-title-input').val("My New Search").trigger('input');
                            this.view.$('.popover-content .search-title-input-container .save-title-confirm-button').click();
                        });

                        it('does not revert the search', function() {
                            expect(this.queryState.queryTextModel.get('inputText')).toBe('archipelago');
                        });
                    });

                    describe('and a Query is saved', function() {
                        beforeEach(function() {
                            spyOn(this.savedQueryCollection, 'create');

                            this.view.$('.popover-content .search-title-input-container .search-title-input').val("My New Search").trigger('input');
                            this.view.$('.popover-content .search-title-input-container .save-title-confirm-button').click();
                        });

                        it('reverts the search', function() {
                            expect(this.queryState.queryTextModel.get('inputText')).toBe('cat');
                        });
                    });
                });
            });

            describe('then the selected indexes are changed', function() {
                beforeEach(function() {
                    databaseNameResolver.getDatabaseInfoFromCollection.and.callFake(function () {
                        return [{name: 'Wikipedia', domain: 'PUBLIC'}, {name: 'NEW_DATABASE', domain: 'PUBLIC'}];
                    });
                    this.queryState.selectedIndexes.add({
                        name: 'NEW_DATABASE',
                        domain: 'PUBLIC'
                    });
                });

                // Index and parametric value selection only updates the query model after a debounce but we want to update
                // the save and reset buttons immediately
                it('updates the save and reset buttons immediately', function() {
                    expect(this.view.$('.show-rename-button')).not.toHaveClass('hide');
                    expect(this.view.$('.search-reset-option')).not.toHaveClass('hide');
                });
            });
        });
    });

});
