define([
    'backbone',
    'find/app/model/saved-searches/saved-search-collection',
    'find/app/model/saved-searches/saved-search-model',
    'text!find/templates/app/page/search/saved-search-control/save-search-input.html',
    'i18n!find/nls/bundle'
], function(Backbone, SavedSearchCollection, SavedSearchModel, template, i18n) {

    return Backbone.View.extend({
        template: _.template(template),

        saveSuccess: function() {
            this.savedSearchControlModel.set('showSave', false);
        },

        save: function() {
            var name = this.$input.val();

            var saveArguments = {
                title: name,
                queryText: this.queryModel.get('queryText'),
                indexes: this.queryModel.get('indexes'),
                parametricValues: this.queryModel.get('parametricValues')
            };

            var savedSearch = new SavedSearchModel();

            this.savedSearchCollection.add(savedSearch);

            savedSearch.save(saveArguments, {
                success: _.bind(this.saveSuccess, this),
                error: function() {
                    console.log('failure to save model');
                },
                wait: true
            }).always(function() {

            });
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
            this.queryModel = options.queryModel;
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchControlModel = options.savedSearchControlModel;
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$input = this.$('.find-input');
        }
    });

});