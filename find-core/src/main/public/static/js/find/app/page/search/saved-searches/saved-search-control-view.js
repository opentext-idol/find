define([
    'backbone',
    'find/app/util/array-equality',
    'find/app/page/search/saved-searches/save-search-input',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/util/confirm-view',
    'text!find/templates/app/page/search/saved-searches/saved-search-control-view.html',
    'i18n!find/nls/bundle'
], function(Backbone, arrayEquality, SaveSearchInput, SavedSearchModel, Confirm, template, i18n) {

    'use strict';

    var html = _.template(template)({i18n: i18n});

    return Backbone.View.extend({
        events: {
            'click .show-save-as-button': function() {
                this.model.set('showSaveAs', !this.model.get('showSaveAs'));
            },
            'click .save-search-button': function() {
                var attributes = SavedSearchModel.attributesFromQueryState(this.queryState);

                // TODO: Handle success/error
                this.savedSearchModel.save(attributes, {wait: true});
            },
            'click .saved-search-delete-option': function(e) {
                e.preventDefault();

                new Confirm({
                    cancelClass: 'btn-default',
                    cancelIcon: '',
                    cancelText: i18n['app.cancel'],
                    okText: i18n['app.delete'],
                    okClass: 'btn-danger',
                    okIcon: '',
                    message: i18n['search.savedSearches.confirm.deleteMessage'](this.savedSearchModel.get('title')),
                    title: i18n['search.savedSearches.confirm.deleteMessage.title'],
                    hiddenEvent: 'hidden.bs.modal',
                    okHandler: _.bind(function() {
                        // TODO: Handle success/error
                        this.savedSearchModel.destroy();
                    }, this)
                });
            },
            'click .search-reset-option': function(e) {
                e.preventDefault();

                new Confirm({
                    cancelClass: 'btn-default',
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
                        this.queryState.queryModel.set(this.savedSearchModel.toQueryModelAttributes());
                        this.queryState.selectedIndexes.set(this.savedSearchModel.toSelectedIndexes());
                        this.queryState.selectedParametricValues.set(this.savedSearchModel.toSelectedParametricValues());
                    }, this)
                });
            }
        },

        initialize: function(options) {
            this.savedSearchModel = options.savedSearchModel;

            this.queryState = {
                queryTextModel: options.queryTextModel,
                queryModel: options.queryModel,
                selectedIndexes: options.selectedIndexesCollection,
                selectedParametricValues: options.selectedParametricValues
            };

            this.model = new Backbone.Model({showSaveAs: false});

            this.listenTo(this.model, 'change:showSaveAs', this.updateShowSaveAsVisibility);
            this.listenTo(this.savedSearchModel, 'change:id', this.updateShowSaveAsButtonText);

            this.listenTo(this.savedSearchModel, 'change', this.updateResetAndSaveControls);
            this.listenTo(this.queryState.queryModel, 'change', this.updateResetAndSaveControls);

            this.saveSearchInput = new SaveSearchInput({
                savedSearchModel: this.savedSearchModel,
                queryState: this.queryState,
                savedSearchControlModel: this.model
            });
        },

        render: function() {
            this.$el.html(html);

            this.saveSearchInput.setElement(this.$('.save-search-input-container')).render();

            this.updateResetAndSaveControls();
            this.updateShowSaveAsButtonText();
            this.updateShowSaveAsVisibility();
        },

        updateResetAndSaveControls: function() {
            var hide = this.savedSearchModel.isNew() || this.savedSearchModel.equalsQueryState(this.queryState);
            this.$('.search-reset-option, .save-search-button').toggleClass('hide', hide);
        },

        updateShowSaveAsButtonText: function() {
            var keySuffix = (this.savedSearchModel.isNew() ? 'create' : 'edit');
            this.$('.show-save-as-button').text(i18n['search.savedSearchControl.openEdit.' + keySuffix]);
        },

        updateShowSaveAsVisibility: function() {
            var showSaveAs = this.model.get('showSaveAs');
            this.saveSearchInput.$el.toggleClass('hide', !showSaveAs);

            this.$('.show-save-as-button')
                .toggleClass('active', showSaveAs)
                .attr('aria-pressed', showSaveAs);
        }
    });

});