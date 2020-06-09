/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'js-whatever/js/list-view',
    'find/app/util/modal',
    'find/app/model/saved-searches/shared-with-everyone-model',
    'find/app/model/saved-searches/shared-with-users-collection',
    'find/app/model/saved-searches/not-shared-with-users-collection',
    'find/app/util/shared-with-users-item-view',
    'find/app/util/not-shared-with-users-item-view',
    'find/app/util/filtering-collection',
    'find/app/configuration',
    'text!find/templates/app/util/sharing-options.html',
    'moment',
    'iCheck'
], function(Backbone, _, $, i18n, ListView, Modal, SharedWithEveryoneModel, SharedWithUsersCollection, NotSharedWithUsersCollection, SharedWithUsersItemView,
            NotSharedWithUsersItemView, FilteringCollection, config, template) {
    'use strict';

    const getSharedWithUserModelCidFromEvent = function (event, attribute) {
        const $target = $(event.currentTarget);
        return $target.attr(attribute) ? $target.attr(attribute) : $target.closest('[' + attribute + ']').attr(attribute);
    };

    const ContentView = Backbone.View.extend({
        events: {
            'click .js-share-with-button': function(e) {
                const searchTitle = this.model.get('title');
                const username = getSharedWithUserModelCidFromEvent(e, 'data-username');

                this.sharedWithUsersCollection.create({
                    username: username,
                    searchId: this.model.get('id'),
                    modifiedDate: null,
                    sharedDate: null,
                    canEdit: false
                }, {
                    success: _.bind(function() {
                        this.trigger('success',
                            i18n['search.savedSearchControl.sharingOptions.success'] +
                            i18n['search.savedSearchControl.sharingOptions.success.shared'](searchTitle, username));
                    }, this),
                    error: _.bind(function(collection, xhr) {
                        const message = i18n['search.savedSearchControl.sharingOptions.error'] +
                            (xhr.responseJSON && xhr.responseJSON.message ||
                            i18n['search.savedSearchControl.sharingOptions.error.shared'](searchTitle, username));

                        this.trigger('error', message);
                    }, this)
                });
            },
            'click .js-unshare-with-button': function(e) {
                const searchTitle = this.model.get('title');
                const username = getSharedWithUserModelCidFromEvent(e, 'data-username');

                this.sharedWithUsersCollection
                    .get(username)
                    .destroy({
                        wait: true,
                        success: _.bind(function() {
                            this.trigger('success',
                                i18n['search.savedSearchControl.sharingOptions.success'] +
                                i18n['search.savedSearchControl.sharingOptions.success.unshared'](searchTitle, username));
                        }, this),
                        error: _.bind(function(collection, xhr) {
                            const message = i18n['search.savedSearchControl.sharingOptions.error'] +
                                (xhr.responseJSON && xhr.responseJSON.message ||
                                i18n['search.savedSearchControl.sharingOptions.error.unshared'](searchTitle, username));

                            this.trigger('error', message);
                        }, this)
                    });
            },
            'ifChecked .js-can-edit-checkbox': function(e) {
                const searchTitle = this.model.get('title');
                const username = getSharedWithUserModelCidFromEvent(e, 'data-username');

                this.sharedWithUsersCollection
                    .get(username)
                    .save({
                        canEdit: true
                    }, {
                        success: _.bind(function() {
                            this.trigger('success',
                                i18n['search.savedSearchControl.sharingOptions.success'] +
                                i18n['search.savedSearchControl.sharingOptions.success.canEdit'](searchTitle, username));
                        }, this),
                        error: _.bind(function(collection, xhr) {
                            const message = i18n['search.savedSearchControl.sharingOptions.error'] +
                                (xhr.responseJSON && xhr.responseJSON.message ||
                                i18n['search.savedSearchControl.sharingOptions.error.editPermissions'](searchTitle, username));

                            this.trigger('error', message);
                        }, this)
                    });
            },
            'ifUnchecked .js-can-edit-checkbox': function(e) {
                const searchTitle = this.model.get('title');
                const username = getSharedWithUserModelCidFromEvent(e, 'data-username');

                this.sharedWithUsersCollection
                    .get(username)
                    .save({
                        canEdit: false
                    }, {
                        success: _.bind(function() {
                            this.trigger('success',
                                i18n['search.savedSearchControl.sharingOptions.success'] +
                                i18n['search.savedSearchControl.sharingOptions.success.cannotEdit'](this.model.get('title'), getSharedWithUserModelCidFromEvent(e, 'data-username')));
                        }, this),
                        error: _.bind(function(collection, xhr) {
                            const message = i18n['search.savedSearchControl.sharingOptions.error'] +
                                (xhr.responseJSON && xhr.responseJSON.message ||
                                i18n['search.savedSearchControl.sharingOptions.error.editPermissions'](searchTitle, username));

                            this.trigger('error', message);
                        }, this)
                    });
            },
            'ifClicked .js-search-shared-with-everyone': function(e) {
                const searchTitle = this.model.get('title');

                if (e.target.checked) {
                    this.sharedWithEveryoneModel.destroy({
                        success: _.bind(function() {
                            this.trigger('success',
                                i18n['search.savedSearchControl.sharingOptions.success'] +
                                i18n['search.savedSearchControl.sharingOptions.success.unsharedWithEveryone'](this.model.get('title')));
                        }, this),
                        error: _.bind(function(collection, xhr) {
                            const message = i18n['search.savedSearchControl.sharingOptions.error'] +
                                (xhr.responseJSON && xhr.responseJSON.message ||
                                    i18n['search.savedSearchControl.sharingOptions.error.unsharedWithEveryone'](searchTitle));

                            this.trigger('error', message);
                        }, this)
                    });
                }
                else {
                    this.sharedWithEveryoneModel.save({ searchId: this.sharedWithEveryoneModel.searchId }, {
                        success: _.bind(function() {
                            this.trigger('success',
                                i18n['search.savedSearchControl.sharingOptions.success'] +
                                i18n['search.savedSearchControl.sharingOptions.success.sharedWithEveryone'](searchTitle));
                        }, this),
                        error: _.bind(function(collection, xhr) {
                            const message = i18n['search.savedSearchControl.sharingOptions.error'] +
                                (xhr.responseJSON && xhr.responseJSON.message ||
                                    i18n['search.savedSearchControl.sharingOptions.error.sharedWithEveryone'](searchTitle));

                            this.trigger('error', message);
                        }, this)
                    });
                }
            },
            'input .js-search-for-not-shared-with-users': function(e) {
                const inputText = $(e.currentTarget).val();

                this.notSharedWithUsersCollection.fetch({
                    data: {
                        searchText: inputText ? "*" + inputText + "*" : null,
                        startUser: 0,
                        maxUsers: 20
                    }
                });
            },
            'input .js-search-for-shared-with-users': function(e) {
                this.sharedWithUsersFilterModel.set('filterText', $(e.currentTarget).val() || null);
            }
        },

        template: _.template(template),

        initialize: function() {
            this.sharedWithEveryoneModel = new SharedWithEveryoneModel({}, {
                searchId: this.model.get('id')
            });

            this.sharedWithEveryoneModel.fetch();

            this.sharedWithUsersCollection = new SharedWithUsersCollection([], {
                searchId: this.model.get('id')
            });

            this.notSharedWithUsersCollection = new NotSharedWithUsersCollection();

            this.sharedWithUsersFilterModel = new Backbone.Model({
                filterText: null
            });

            this.filteredSharedWithUsersCollection = new FilteringCollection([], {
                collection: this.sharedWithUsersCollection,
                filterModel: this.sharedWithUsersFilterModel,
                predicate: function(model) {
                    const filterText = this.sharedWithUsersFilterModel.get('filterText');
                    return filterText === null ? true : model.get('username').indexOf(filterText) !== -1;
                }.bind(this)
            });

            this.filteredNotSharedWithUsersCollection = new FilteringCollection([], {
                collection: this.notSharedWithUsersCollection,
                predicate: function(model) {
                    const username = model.get('username');
                    return username !== config().username && !this.sharedWithUsersCollection.get(username);
                }.bind(this)
            });

            this.sharedWithUsersCollection.fetch({});

            this.notSharedWithUsersCollection.fetch({
                data:{
                    searchText: '*',
                    startUser: 0,
                    maxUsers: 20
                }
            });

            this.sharedWithList = new ListView({
                collection: this.filteredSharedWithUsersCollection,
                ItemView: SharedWithUsersItemView
            });

            this.notSharedWithList = new ListView({
                collection: this.filteredNotSharedWithUsersCollection,
                ItemView: NotSharedWithUsersItemView
            });

            _.each([this.sharedWithUsersCollection, this.filteredSharedWithUsersCollection, this.notSharedWithUsersCollection], function(collection) {
                this.listenTo(collection, 'sync update', function() {
                    this.updateNotSharedWithUsersCollection();

                    this.toggleEmptyUsersLists();
                }.bind(this));
            }.bind(this));

            this.listenTo(this.sharedWithEveryoneModel, 'sync update', this.updateSharedWithEveryone);
            this.listenTo(this.sharedWithEveryoneModel, 'destroy', function(){
                this.sharedWithEveryoneModel.set('searchId', undefined);
                this.updateSharedWithEveryone();
            });
        },

        updateSharedWithEveryone: function(){
            const tmp = this.sharedWithEveryoneModel;
            this.$('.js-search-shared-with-everyone').iCheck(tmp.isNew() ? 'uncheck' : 'check');
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.sharedWithList.setElement(this.$('.js-shared-with-list-container ul')).render();
            this.notSharedWithList.setElement(this.$('.js-not-shared-with-list-container ul')).render();

            this.toggleEmptyUsersLists();

            this.$('.js-search-shared-with-everyone').iCheck({
                checkboxClass: 'icheckbox-hp clickable'
            });
            this.updateSharedWithEveryone();
        },

        toggleEmptyUsersLists: function() {
            this.$('.js-no-not-shared-with-users').toggleClass('hide', this.filteredNotSharedWithUsersCollection.length !== 0);
            this.$('.js-no-shared-with-users').toggleClass('hide', this.filteredSharedWithUsersCollection.length !== 0);
        },

        updateNotSharedWithUsersCollection: function() {
            this.filteredNotSharedWithUsersCollection.filterModels();
        }
    });

    return Modal.extend({
        initialize: function(options) {
            const contentView = new ContentView({
                model: options.savedSearchModel
            });

            Modal.prototype.initialize.call(this, {
                secondaryButtonText: i18n['app.close'],
                title: i18n['search.savedSearchControl.sharingOptions'],
                contentView: contentView
            });

            this.listenTo(contentView, 'success', function(message) {
                this.toggleStatusText('success', message);
            });

            this.listenTo(contentView, 'error', function(message) {
                this.toggleStatusText('error', message);
            });
        },

        render: function() {
            Modal.prototype.render.apply(this, arguments);

            this.$('.modal-footer').prepend('<span class="js-sharing-options-status-message"></span>');
            this.$statusMessage = this.$('.js-sharing-options-status-message');
        },

        toggleStatusText: function(status, message) {
            this.$statusMessage
                .empty()
                .text(message)
                .addClass('text-' + status);
        }

    })
});
