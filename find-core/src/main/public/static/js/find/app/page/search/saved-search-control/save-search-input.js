define([
    'backbone',
    'text!find/templates/app/page/search/saved-search-control/save-search-input.html',
    'i18n!find/nls/bundle'
], function(Backbone, template, i18n) {

    var html = _.template(template)({i18n: i18n});

    return Backbone.View.extend({
        saveSuccess: function() {
            this.savedSearchControlModel.set('showSave', false);
            this.$saveInput.val('');
            this.$saveErrorMessage.text('');
        },

        saveFailure: function() {
            this.$saveErrorMessage.text(i18n['search.savedSearchControl.error']);
        },

        disable: function(disable) {
            this.$saveInput.prop('disabled', disable);
            this.$confirmButton.prop('disabled', disable);
            this.$cancelButton.prop('disabled', disable);
        },

        save: function() {
            var name = this.$saveInput.val();

            this.savedSearchModel.set('title', name);

            this.disable(true);

            this.savedSearchModel.save({
                success: _.bind(this.saveSuccess, this),
                error: _.bind(this.saveFailure, this),
                wait: true
            }).always(_.bind(function() {
                this.disable(false);
            }, this));
        },

        events: {
            'submit .find-form': function (event) {
                event.preventDefault();
                this.save();
            },
            'click .save-confirm-button': function() {
                event.preventDefault();
                this.save();
            },
            'click .save-cancel-button': function() {
                event.preventDefault();
                this.savedSearchControlModel.set('showSave', false);
            }
        },

        initialize: function(options) {
            this.savedSearchModel = options.savedSearchModel;
            this.queryModel = options.queryModel;
            this.savedSearchControlModel = options.savedSearchControlModel;
        },

        render: function() {
            this.$el.html(html);

            this.$saveInput = this.$('.save-input .find-input');
            this.$confirmButton = this.$('.save-confirm-button');
            this.$cancelButton = this.$('.save-cancel-button');
            this.$saveErrorMessage = this.$('.save-error-message');
        }
    });

});