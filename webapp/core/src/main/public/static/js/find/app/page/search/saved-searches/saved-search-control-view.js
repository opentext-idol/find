/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'find/app/util/array-equality',
    'find/app/page/search/saved-searches/search-title-input',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/util/confirm-view',
    'find/app/util/csv-field-selection-view',
    'find/app/util/sharing-options',
    'js-whatever/js/modal',
    'text!find/templates/app/page/search/saved-searches/saved-search-control-view.html',
    'i18n!find/nls/bundle',
    'find/app/util/popover',
    'underscore'
], function(Backbone, $, arrayEquality, SearchTitleInput, SavedSearchModel, Confirm, CsvFieldSelectView, SharingOptions,
            Modal, template, i18n, popover, _) {
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
        if(savedSearchModel.isNew()) {
            return SavedState.NEW;
        } else {
            return savedSearchModel.equalsQueryState(queryState) ? SavedState.SAVED : SavedState.MODIFIED;
        }
    }

    function toggleTitleEditState(titleEditState, searchType) {
        return function() {
            var isCurrentMethod = this.model.get('titleEditState') === titleEditState;

            this.model.set({
                error: null,
                searchType: searchType,
                titleEditState: isCurrentMethod ? TitleEditState.OFF : titleEditState
            });
        };
    }

    return Backbone.View.extend({
        template: _.template(template),
        titleInput: null,

        events: {
            'click .compare-modal-button': function() {
                this.comparisonModalCallback();
            },
            'click .show-rename-button': function() {
                var searchType = this.savedSearchModel.get('type');
                toggleTitleEditState(TitleEditState.RENAME, searchType).call(this)
            },
            'click .popover-control': function(e) {
                this.$('.popover-control, .save-search-button').addClass('disabled not-clickable');
                $(e.currentTarget).removeClass('disabled not-clickable');
            },
            'click .show-save-as': function(e) {
                var searchType = $(e.currentTarget).attr('data-search-type');
                toggleTitleEditState(TitleEditState.SAVE_AS, searchType).call(this);
            },
            'click .open-as-query-option': function() {
                var newSearch = new SavedSearchModel(_.defaults({
                    id: null,
                    title: i18n['search.newSearch'],
                    type: SavedSearchModel.Type.QUERY
                }, this.savedSearchModel.attributes));

                this.searchCollections.QUERY.add(newSearch);
                const selectedTab = this.resultsViewSelectionModel.get('selectedTab');
                this.selectedTabModel.set({
                    selectedSearchCid: newSearch.cid,
                    selectedResultsView: selectedTab,
                });
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
            'click .export-csv-option': function() {
                var csvFieldSelectView = new CsvFieldSelectView({
                    queryModel: this.queryModel
                });

                new Modal({
                    actionButtonClass: 'button-primary',
                    actionButtonText: i18n['app.button.exportCsv'],
                    className: Modal.prototype.className + ' fixed-height-modal',
                    contentView: csvFieldSelectView,
                    secondaryButtonText: i18n['app.cancel'],
                    title: i18n['app.exportToCsv.modal.title'],
                    actionButtonCallback: function() {
                        csvFieldSelectView.requestCsv();
                        this.hide();
                    }
                })
            },
            'click .saved-search-delete-option': function() {
                this.model.set('error', null);

                new Confirm({
                    cancelClass: 'btn-white',
                    cancelIcon: '',
                    cancelText: i18n['app.cancel'],
                    okText: i18n['app.button.delete'],
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
            'click .saved-search-close-option': function() {
                this.model.set({error: null, loading: true});

                this.savedSearchModel.collection.remove(this.savedSearchModel);
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
                        this.resetQueryState();
                    }, this)
                });
            },
            'click .js-open-sharing-options-modal': function() {
                new SharingOptions({
                    savedSearchModel: this.savedSearchModel
                });
            }
        },

        initialize: function(options) {
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchModel = options.savedSearchModel;
            this.documentsCollection = options.documentsCollection;
            this.queryModel = options.queryModel;
            this.queryState = options.queryState;
            this.selectedTabModel = options.selectedTabModel;
            this.searchCollections = options.searchCollections;
            this.searchTypes = options.searchTypes;
            this.comparisonModalCallback = options.comparisonModalCallback;
            this.resultsViewSelectionModel = options.resultsViewSelectionModel;

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

                if(savedState === SavedState.NEW && this.model.get('titleEditState') === TitleEditState.RENAME) {
                    // This shouldn't happen; a saved model cannot become new again
                    attributes.titleEditState = TitleEditState.OFF;
                }

                this.model.set(attributes);
            }, this);

            this.listenTo(this.savedSearchCollection, 'reset update', function() {
                this.updateCompareModalButton();
                this.updateSharingOptionsModalButton();
            });
            this.listenTo(this.savedSearchModel, 'change', updateSavedState);
            this.listenTo(options.queryModel, 'change', updateSavedState);

            // Index and parametric value selection only updates the query model after a debounce but we want to update
            // the save and reset buttons immediately
            this.listenTo(this.queryState.selectedIndexes, 'add remove', updateSavedState);
            this.listenTo(this.queryState.selectedParametricValues, 'add remove', updateSavedState);
        },

        render: function() {
            var isMutable = this.searchTypes[this.savedSearchModel.get('type')].isMutable;

            // only allow Save As buttons for these search types
            var saveAsSearchTypes = [SavedSearchModel.Type.QUERY, SavedSearchModel.Type.SNAPSHOT];

            this.$el.html(this.template({
                i18n: i18n,
                showCompare: Boolean(this.comparisonModalCallback),
                showSaveAs: isMutable,
                searchTypes: saveAsSearchTypes,
                showOpenAsQuery: !isMutable,
                readOnly: this.savedSearchModel.get('type').indexOf('READ_ONLY') !== -1
            }));

            this.renderTitleInput();
            this.updateErrorMessage();
            this.updateForSavedState();
            this.updateForTitleEditState();
            this.updateLoading();
            this.createPopover();
            this.updateCompareModalButton();
            this.updateSharingOptionsModalButton();

            this.$saveButtons = this.$('.save-button');

            this.updateSearchValidityUI();
        },

        updateCompareModalButton: function() {
            this.$('.compare-modal-button').toggleClass('disabled not-clickable', this.savedSearchCollection.length <= 1);
        },

        updateSharingOptionsModalButton: function() {
            var savedSearchModelType = this.savedSearchModel.get('type');
            var showButton = savedSearchModelType === 'QUERY' || savedSearchModelType === 'SNAPSHOT';

            this.$('.js-open-sharing-options-modal').toggleClass('hidden', !showButton);
        },

        createPopover: function() {
            var $popover;
            var $popoverControl = this.$('.popover-control');

            var clickHandler = _.bind(function(e) {
                var $target = $(e.target);
                var notPopover = !$target.is($popover) && !$.contains($popover[0], $target[0]);
                var notPopoverControl = !$target.is($popoverControl) && !$.contains($popoverControl[0], $target[0]);

                if(notPopover && notPopoverControl) {
                    //$popoverControl.popover('hide');
                    this.$('.popover-control.active').click();

                }
            }, this);

            popover($popoverControl, 'click', function(content) {
                content.html('<div class="search-title-input-container"></div>');
                $popover = content.closest('.popover');
                $(document.body).on('click', clickHandler);
            }, _.bind(function() {
                $(document.body).off('click', clickHandler);

                this.$('.popover-control, .save-search-button').removeClass('active disabled not-clickable');
            }, this));
        },

        updateForSavedState: function() {
            var savedState = this.model.get('savedState');

            this.$('.search-reset-option, .save-search-button').toggleClass('hide', savedState !== SavedState.MODIFIED);
            this.$('.show-rename-button').toggleClass('hide', savedState === SavedState.NEW);
            this.$('.js-open-sharing-options-modal').toggleClass('hide', savedState === SavedState.NEW);

            if(this.searchTypes[this.savedSearchModel.get('type')].isMutable) {
                var createOrEdit = savedState === SavedState.NEW ? 'create' : 'edit';

                _.each(this.$('.show-save-as[data-search-type]'), function(el) {
                    var $el = $(el);
                    $el.text(this.searchTypes[$el.attr('data-search-type')].openEditText[createOrEdit]);
                }, this);
            }
        },

        updateForTitleEditState: function() {
            var searchType = this.model.get('searchType');
            var titleEditState = this.model.get('titleEditState');

            if(this.searchTypes[this.savedSearchModel.get('type')].isMutable) {
                var editToggleQuery = TitleEditState.SAVE_AS === titleEditState && searchType === 'QUERY';
                this.$('.show-save-as[data-search-type="QUERY"]')
                    .toggleClass('active', editToggleQuery)
                    .attr('aria-pressed', editToggleQuery);

                var editToggleSnapshot = TitleEditState.SAVE_AS === titleEditState && searchType === 'SNAPSHOT';
                this.$('.show-save-as[data-search-type="SNAPSHOT"]')
                    .toggleClass('active', editToggleSnapshot)
                    .attr('aria-pressed', editToggleSnapshot);
            }

            this.$('.show-rename-button')
                .toggleClass('active', TitleEditState.RENAME === titleEditState)
                .attr('aria-pressed', TitleEditState.RENAME === titleEditState);

            // Destroy the title input if we are not editing the title
            if(titleEditState === TitleEditState.OFF) {
                this.destroyTitleInput();
            }

            // Create a title input if we have clicked "Save as query/snapshot" or "Rename" and the title input is not displayed
            if(titleEditState !== TitleEditState.OFF && this.titleInput === null) {
                this.titleInput = new SearchTitleInput({
                    savedSearchModel: this.savedSearchModel,
                    savedSearchCollection: this.savedSearchCollection,
                    saveCallback: _.bind(function(newAttributes, success, error) {
                        var savedState = this.model.get('savedState');
                        var titleEditState = this.model.get('titleEditState');

                        var attributes = _.extend(newAttributes,
                            {type: searchType},
                            SavedSearchModel.attributesFromQueryState(this.queryState)
                        );

                        var saveOptions = {
                            error: error,
                            success: _.bind(function(model) {
                                // If we have just created a saved query, switch to its tab
                                if(this.searchTypes[searchType].isMutable) {
                                    this.selectedTabModel.set('selectedSearchCid', model.cid);
                                }

                                success();
                            }, this),
                            wait: true,
                            timeout: 90000
                        };

                        if(titleEditState === TitleEditState.SAVE_AS && (savedState !== SavedState.NEW || !this.searchTypes[searchType].isMutable)) {
                            this.searchCollections[searchType].create(attributes, saveOptions);

                            // Saving a new query from a query tab
                            if(this.searchTypes[this.savedSearchModel.get('type')].isMutable && this.searchTypes[searchType].isMutable) {
                                this.resetQueryState();
                            }
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

        resetQueryState: function() {
            this.queryState.datesFilterModel.set(this.savedSearchModel.toDatesFilterModelAttributes());
            this.queryState.geographyModel.set(this.savedSearchModel.toGeographyModelAttributes());
            this.queryState.conceptGroups.set(this.savedSearchModel.toConceptGroups());
            this.queryState.selectedIndexes.set(this.savedSearchModel.toSelectedIndexes());
            this.queryState.selectedParametricValues.set(this.savedSearchModel.toSelectedParametricValues());
            this.queryState.minScoreModel.set(this.savedSearchModel.toMinScoreModelAttributes());
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
            if(this.titleInput !== null) {
                this.titleInput.remove();
                this.stopListening(this.titleInput);
                this.titleInput = null;
            }
        },

        renderTitleInput: function() {
            if(this.titleInput) {
                // Append before render so we can focus the input
                this.$('.search-title-input-container').append(this.titleInput.$el);
                this.titleInput.render();
            }
        }
    });
});
