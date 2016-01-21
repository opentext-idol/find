define([
    'backbone',
    'find/app/model/saved-searches/saved-search-collection',
    'find/app/model/saved-searches/saved-search-model',
    'text!find/templates/app/page/search/saved-search-control/saved-search-control-view.html',
    'i18n!find/nls/bundle'
], function(Backbone, SavedSearchCollection, SavedSearchModel, template, i18n) {

    return Backbone.View.extend({
        template: _.template(template),

        save: function(name) {
            this.savedSearchCollection.add(new SavedSearchModel({
                title: name,
                queryText: this.queryModel.get('queryText'),
                indexes: this.queryModel.get('indexes'),
                parametricValues: this.queryModel.get('parametricValues')
            }));
        },

        events: {
            'submit .find-form': function (event) {
                event.preventDefault();
                this.save(this.$input.val());
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.savedSearchCollection = new SavedSearchCollection();
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$input = this.$('.find-input');
        }
    });

});