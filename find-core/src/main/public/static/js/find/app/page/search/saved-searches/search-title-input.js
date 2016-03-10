/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery',
    'text!find/templates/app/page/search/saved-searches/search-title-input.html',
    'find/app/model/saved-searches/saved-search-model',
    'i18n!find/nls/bundle',
    'iCheck'
], function(Backbone, _, $, template, SavedSearchModel, i18n) {

    // The initial title for an unsaved search should be blank, not "New Title"
    function resolveCurrentTitle(savedSearchModel) {
        var modelTitle = savedSearchModel.get('title');
        return savedSearchModel.isNew() || !modelTitle ? '' : modelTitle;
    }

    return Backbone.View.extend({
        className: 'search-title-form',
        tagName: 'form',
        template: _.template(template),

        events: {
            'click .save-title-cancel-button': function() {
                this.trigger('remove');
            },
            'input .search-title-input': function(event) {
                this.model.set({
                    error: null,
                    title: $(event.target).val()
                });
            },
            'ifChecked .i-check': function(event) {
                this.model.set({
                    error: null,
                    type: $(event.target).val()
                });
            },
            'submit': function(event) {
                event.preventDefault();

                var title = this.model.get('title').trim();
                var type = this.model.get('type');

                var isDuplicateName = Boolean(this.savedSearchCollection.findWhere({title: title}));

                if(isDuplicateName) {
                    this.model.set({
                        error: i18n['search.savedSearchControl.nameAlreadyExists'],
                        loading: false
                    });
                } else {
                    this.model.set({
                        error: null,
                        loading: true
                    });

                    this.saveCallback(
                        {title: title, type: type},
                        _.bind(function() {
                            this.trigger('remove');
                        }, this),
                        _.bind(function(collection, response) {
                            if(response.statusText === 'timeout') {
                                this.model.set('error', i18n['search.savedSearchControl.error.timeout']);
                            } else {
                                this.model.set('error', i18n['search.savedSearchControl.error']);
                            }
                            this.model.set('loading', false);
                        }, this)
                    );
                }
            }
        },

        initialize: function(options) {
            this.savedSearchModel = options.savedSearchModel;
            this.showSearchTypes = options.showSearchTypes;
            this.savedSearchCollection = options.savedSearchCollection;

            // Called with the new title, search type, and a success callback and an error callback
            this.saveCallback = options.saveCallback;

            this.model = new Backbone.Model({
                error: null,
                loading: false,
                title: resolveCurrentTitle(this.savedSearchModel),
                type: this.savedSearchModel.get('type')
            });

            this.listenTo(this.model, 'change:error', this.updateError);
            this.listenTo(this.model, 'change:loading', this.updateLoading);
            this.listenTo(this.model, 'change:title', this.updateTitle);
            this.listenTo(this.model, 'change:type', this.updateType);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                showSearchTypes: this.showSearchTypes,
                savedSearchTypes: [
                    SavedSearchModel.Type.QUERY,
                    SavedSearchModel.Type.SNAPSHOT
                ]
            }));

            this.$confirmButton = this.$('.save-title-confirm-button');

            this.$('.search-title-input').focus();

            this.updateError();
            this.updateLoading();
            this.updateTitle();
            this.updateType();

            this.$('.i-check').iCheck({radioClass: 'iradio-hp'});
        },

        updateError: function() {
            var error = this.model.get('error');

            this.$('.search-title-error-message')
                .toggleClass('hide', error === null)
                .text(error === null ? '' : error);
        },

        updateLoading: function() {
            this.$('.save-title-confirm-button > i').toggleClass('hide', !this.model.get('loading'));
            this.$('.search-title-input, .save-title-cancel-button, .save-title-confirm-button').prop('disabled', this.model.get('loading'));
        },

        updateTitle: function() {
            var title = this.model.get('title');
            var $titleInput = this.$('.search-title-input');

            if ($titleInput.val() !== title) {
                this.$('.search-title-input').val(title);
            }

            this.updateConfirmButton();
        },

        updateType: function() {
            var type = this.model.get('type');
            this.$('[name="saved-search-type"][value="' + type + '"]').iCheck('check');
        },

        updateConfirmButton: function() {
            var title = this.model.get('title');

            this.$confirmButton.toggleClass('disabled not-clickable', resolveCurrentTitle(this.savedSearchModel) === title || !title);
        }
    });

});
