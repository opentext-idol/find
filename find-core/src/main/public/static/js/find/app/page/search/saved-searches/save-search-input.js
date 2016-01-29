define([
    'backbone',
    'text!find/templates/app/page/search/saved-searches/save-search-input.html',
    'find/app/model/saved-searches/saved-search-model',
    'i18n!find/nls/bundle'
], function(Backbone, template, SavedSearchModel, i18n) {

    var html = _.template(template)({i18n: i18n});

    return Backbone.View.extend({
        events: {
            'click .save-confirm-button': 'saveSearch',
            'submit .find-form': function(event) {
                event.preventDefault();
                this.saveSearch();
            },
            'click .save-cancel-button': function() {
                this.savedSearchControlModel.set('showSaveAs', false);
            }
        },

        initialize: function(options) {
            this.savedSearchModel = options.savedSearchModel;
            this.queryState = options.queryState;
            this.savedSearchControlModel = options.savedSearchControlModel;
        },

        render: function() {
            this.$el.html(html);

            this.$saveInput = this.$('.save-input .find-input');
            this.$confirmButton = this.$('.save-confirm-button');
            this.$cancelButton = this.$('.save-cancel-button');
            this.$saveErrorMessage = this.$('.save-error-message');
        },

        disable: function(disable) {
            this.$saveInput.prop('disabled', disable);
            this.$confirmButton.prop('disabled', disable);
            this.$cancelButton.prop('disabled', disable);
        },

        saveSearch: function() {
            // TODO: What if the title is empty?
            var attributes = _.extend({
                title: this.$saveInput.val()
            }, SavedSearchModel.attributesFromQueryState(this.queryState));

            this.disable(true);

            this.savedSearchModel
                .save(attributes, {wait: true})
                .done(_.bind(function() {
                    this.savedSearchControlModel.set('showSaveAs', false);
                    this.$saveInput.val('');
                    this.$saveErrorMessage.text('');
                }, this))
                .fail(_.bind(function() {
                    this.$saveErrorMessage.text(i18n['search.savedSearchControl.error']);
                }, this))
                .always(_.bind(function() {
                    this.disable(false);
                }, this));
        }
    });

});
