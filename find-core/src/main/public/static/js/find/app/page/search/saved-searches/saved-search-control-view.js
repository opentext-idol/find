define([
    'backbone',
    'jquery',
    'find/app/util/array-equality',
    'find/app/page/search/saved-searches/search-title-input',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/util/confirm-view',
    'text!find/templates/app/page/search/saved-searches/saved-search-control-view.html',
    'i18n!find/nls/bundle',
    'find/app/util/popover'
], function(Backbone, $, arrayEquality, SearchTitleInput, SavedSearchModel, Confirm, template, i18n, popover) {

    'use strict';

    var SavedState = {
        NEW: 'NEW',
        MODIFIED: 'MODIFIED',
        SAVED: 'SAVED'
    };

    var TitleEditState = {
        OFF: 'OFF',
        RENAME: 'RENAME',
        SAVE_AS: 'SAVE_AS'
    };

    function resolveSavedState(queryState, savedSearchModel) {
        if (savedSearchModel.isNew()) {
            return SavedState.NEW;
        } else {
            return savedSearchModel.equalsQueryState(queryState) ? SavedState.SAVED : SavedState.MODIFIED;
        }
    }

    function toggleTitleEditState(titleEditState) {
        return function() {
            var isCurrentMethod = this.model.get('titleEditState') === titleEditState;

            this.model.set({
                error: null,
                titleEditState: isCurrentMethod ? TitleEditState.OFF : titleEditState
            });
        };
    }

    return Backbone.View.extend({
        template: _.template(template),
        titleInput: null,

        events: {
            'click .show-save-as-button': toggleTitleEditState(TitleEditState.SAVE_AS),
            'click .show-rename-button': toggleTitleEditState(TitleEditState.RENAME),
            'click .popover-control': function(e) {
                this.$('.popover-control, .save-search-button').addClass('disabled not-clickable');
                $(e.currentTarget).removeClass('disabled not-clickable');
            },
            'click .open-as-query-option': function() {
                var newSearch = new SavedSearchModel(_.defaults({
                    id: null,
                    title: i18n['search.newSearch'],
                    type: SavedSearchModel.Type.QUERY
                }, this.savedSearchModel.attributes));

                this.savedQueryCollection.add(newSearch);
                this.selectedTabModel.set('selectedSearchCid', newSearch.cid);
            },
            'click .save-search-button': function() {
                this.model.set({error: null, loading: true});

                this.savedSearchModel.save(SavedSearchModel.attributesFromQueryState(this.queryState), {
                    wait: true,
                    error: _.bind(function() {
                        this.model.set({error: i18n['search.savedSearchControl.error'], loading: false});
                    }, this),
                    success: _.bind(function() {
                        this.model.set({error: null, loading: false});
                    }, this)
                });
            },
            'click .saved-search-delete-option': function() {
                this.model.set('error', null);

                new Confirm({
                    cancelClass: 'btn-white',
                    cancelIcon: '',
                    cancelText: i18n['app.cancel'],
                    okText: i18n['app.delete'],
                    okClass: 'btn-danger',
                    okIcon: '',
                    message: i18n['search.savedSearches.confirm.deleteMessage'](this.savedSearchModel.get('title')),
                    title: i18n['search.savedSearches.confirm.deleteMessage.title'],
                    hiddenEvent: 'hidden.bs.modal',
                    okHandler: _.bind(function() {
                        this.model.set({error: null, loading: true});

                        this.savedSearchModel.destroy({
                            wait: true,
                            error: _.bind(function() {
                                this.model.set({error: i18n['search.savedSearches.deleteFailed'], loading: false});
                            }, this),
                            success: _.bind(function() {
                                this.model.set({error: null, loading: false});
                            }, this)
                        });
                    }, this)
                });
            },
            'click .search-reset-option': function() {
                this.model.set('error', null);

                new Confirm({
                    cancelClass: 'btn-white',
                    cancelIcon: '',
                    cancelText: i18n['app.cancel'],
                    okText: i18n['app.reset'],
                    okClass: 'btn-danger',
                    okIcon: '',
                    message: i18n['search.savedSearches.confirm.resetMessage'](this.savedSearchModel.get('title')),
                    title: i18n['search.savedSearches.confirm.resetMessage.title'],
                    hiddenEvent: 'hidden.bs.modal',
                    okHandler: _.bind(function() {
                        this.queryState.queryTextModel.set(this.savedSearchModel.toQueryTextModelAttributes());
                        this.queryState.datesFilterModel.set(this.savedSearchModel.toDatesFilterModelAttributes());
                        this.queryState.selectedIndexes.set(this.savedSearchModel.toSelectedIndexes());
                        this.queryState.selectedParametricValues.set(this.savedSearchModel.toSelectedParametricValues());
                    }, this)
                });
            }
        },

        initialize: function(options) {
            this.savedQueryCollection = options.savedQueryCollection;
            this.savedSnapshotCollection = options.savedSnapshotCollection;
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchModel = options.savedSearchModel;
            this.documentsCollection = options.documentsCollection;
            this.queryState = options.queryState;
            this.selectedTabModel = options.selectedTabModel;

            this.model = new Backbone.Model({
                // Is the saved search new, modified or up to date with the server?
                savedState: resolveSavedState(this.queryState, this.savedSearchModel),

                // Are we renaming, saving a new search or neither?
                titleEditState: TitleEditState.OFF,

                // Is there a save or delete in progress?
                loading: false,

                // Either null or an error message to display
                error: null,

                validForSave: !this.documentsCollection.currentRequest
            });

            this.listenTo(this.model, 'change:loading', this.updateLoading);
            this.listenTo(this.model, 'change:error', this.updateErrorMessage);
            this.listenTo(this.model, 'change:savedState', this.updateForSavedState);
            this.listenTo(this.model, 'change:titleEditState', this.updateForTitleEditState);
            this.listenTo(this.model, 'change:validForSave', this.updateSearchValidityUI);

            this.listenTo(this.documentsCollection, 'error request', function() {
                this.model.set('validForSave', false);
            });

            this.listenTo(this.documentsCollection, 'sync', function() {
                this.model.set('validForSave', true);
            });

            var updateSavedState = _.bind(function() {
                var savedState = resolveSavedState(this.queryState, this.savedSearchModel);
                var attributes = {savedState: savedState};

                if (savedState === SavedState.NEW && this.model.get('titleEditState') === TitleEditState.RENAME) {
                    // This shouldn't happen; a saved model cannot become new again
                    attributes.titleEditState = TitleEditState.OFF;
                }

                this.model.set(attributes);
            }, this);

            this.listenTo(this.savedSearchModel, 'change', updateSavedState);
            this.listenTo(options.queryModel, 'change', updateSavedState);

            // Index and parametric value selection only updates the query model after a debounce but we want to update
            // the save and reset buttons immediately
            this.listenTo(this.queryState.selectedIndexes, 'add remove', updateSavedState);
            this.listenTo(this.queryState.selectedParametricValues, 'add remove', updateSavedState);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                searchType: this.savedSearchModel.get('type'),
                SearchType: SavedSearchModel.Type
            }));

            this.renderTitleInput();
            this.updateErrorMessage();
            this.updateForSavedState();
            this.updateForTitleEditState();
            this.updateLoading();
            this.createPopover();

            this.$saveButtons = this.$('.save-button');

            this.updateSearchValidityUI();
        },

        createPopover: function() {
            var $popover;
            var $popoverControl = this.$('.popover-control');

            var clickHandler = _.bind(function(e) {
                var $target = $(e.target);
                var notPopover = !$target.is($popover) && !$.contains($popover[0], $target[0]);
                var notPopoverControl = !$target.is($popoverControl) && !$.contains($popoverControl[0], $target[0]);

                if (notPopover && notPopoverControl) {
                    //$popoverControl.popover('hide');
                    this.$('.popover-control.active').click();

                }
            }, this);

            popover($popoverControl, 'click', function(content) {
                content.html('<div class="search-title-input-container"></div>');
                $popover = content.closest('.popover');
                $('body').on('click', clickHandler);
            }, _.bind(function() {
                $(document.body).off('click', clickHandler);

                this.$('.popover-control, .save-search-button').removeClass('active disabled not-clickable');
            }, this));
        },

        updateForSavedState: function() {
            var savedState = this.model.get('savedState');

            this.$('.search-reset-option, .save-search-button').toggleClass('hide', savedState !== SavedState.MODIFIED);
            this.$('.show-rename-button').toggleClass('hide', savedState === SavedState.NEW);

            if (this.savedSearchModel.get('type') === SavedSearchModel.Type.QUERY) {
                var createOrEdit = savedState === SavedState.NEW ? 'create' : 'edit';
                this.$('.show-save-as-button').text(i18n['search.savedSearchControl.openEdit.' + createOrEdit]);
            }
        },

        updateForTitleEditState: function() {
            var titleEditState = this.model.get('titleEditState');

            if (this.savedSearchModel.get('type') === SavedSearchModel.Type.QUERY) {
                this.$('.show-save-as-button')
                    .toggleClass('active', TitleEditState.SAVE_AS === titleEditState)
                    .attr('aria-pressed', TitleEditState.SAVE_AS === titleEditState);
            }

            this.$('.show-rename-button')
                .toggleClass('active', TitleEditState.RENAME === titleEditState)
                .attr('aria-pressed', TitleEditState.RENAME === titleEditState);

            // Destroy the title input if we are not editing the title
            if (titleEditState === TitleEditState.OFF) {
                this.destroyTitleInput();
            }

            // Create a title input if we have clicked "Save As" or "Rename" and the title input is not displayed
            if (titleEditState !== TitleEditState.OFF && this.titleInput === null) {
                this.titleInput = new SearchTitleInput({
                    savedSearchModel: this.savedSearchModel,
                    savedSearchCollection: this.savedSearchCollection,
                    showSearchTypes: titleEditState !== TitleEditState.RENAME,
                    saveCallback: _.bind(function(newAttributes, success, error) {
                        var savedState = this.model.get('savedState');
                        var titleEditState = this.model.get('titleEditState');
                        var searchType = newAttributes.type;

                        var attributes = _.extend(newAttributes, SavedSearchModel.attributesFromQueryState(this.queryState));

                        var saveOptions = {
                            error: error,
                            success: success,
                            wait: true,
                            timeout: 90000
                        };

                        if (titleEditState === TitleEditState.SAVE_AS && searchType === SavedSearchModel.Type.SNAPSHOT) {
                            this.savedSnapshotCollection.create(attributes, saveOptions);
                        } else if (titleEditState === TitleEditState.SAVE_AS && savedState !== SavedState.NEW) {
                            this.savedQueryCollection.create(attributes, saveOptions);
                        } else {
                            this.savedSearchModel.save(attributes, saveOptions);
                        }
                    }, this)
                });

                this.renderTitleInput();

                this.listenTo(this.titleInput, 'remove', function() {
                    this.$('.popover-control.active').click();
                });
            }
        },

        updateErrorMessage: function() {
            this.$('.search-controls-error-message').text(this.model.get('error') || '');
        },

        updateLoading: function() {
            this.$('.save-search-button').prop('disabled', this.model.get('loading'));
        },

        updateSearchValidityUI: function() {
            this.$saveButtons.toggleClass('disabled not-clickable', !this.model.get('validForSave'));
        },

        remove: function() {
            this.destroyTitleInput();
            Backbone.View.prototype.remove.call(this);
        },

        destroyTitleInput: function() {
            if (this.titleInput !== null) {
                this.titleInput.remove();
                this.stopListening(this.titleInput);
                this.titleInput = null;
            }
        },

        renderTitleInput: function() {
            if (this.titleInput) {
                // Append before render so we can focus the input
                this.$('.search-title-input-container').append(this.titleInput.$el);
                this.titleInput.render();
            }
        }
    });

});