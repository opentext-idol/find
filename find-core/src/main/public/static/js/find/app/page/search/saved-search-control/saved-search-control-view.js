define([
    'backbone',
    'find/app/page/search/saved-search-control/save-search-input',
    'text!find/templates/app/page/search/saved-search-control/saved-search-control-view.html',
    'i18n!find/nls/bundle'
], function(Backbone, SaveSearchInput, template, i18n) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click .show-save-button': function () {
                this.model.set('showSave', !this.model.get('showSave'));
            },
            'click .update-button': function() {
                this.model.save();
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.savedSearchCollection = options.savedSearchCollection;
            this.savedSearchModel = options.savedSearchModel;

            this.createMode = this.savedSearchModel.isNew();

            this.model = new Backbone.Model({
                showSave: false
            });

            this.listenTo(this.model, 'change:showSave', function (model, showSave) {
                this.saveSearchInput.$el.toggleClass('hide', !showSave);

                this.$('.show-save-button')
                    .toggleClass('active', showSave)
                    .attr('aria-pressed', showSave);
            });

            this.saveSearchInput = new SaveSearchInput({
                savedSearchModel: this.savedSearchModel,
                queryModel: this.queryModel,
                savedSearchCollection: this.savedSearchCollection,
                savedSearchControlModel: this.model
            })
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                openEditDisplayNameKey: this.createMode ? 'create': 'edit'
            }));

            this.saveSearchInput.setElement(this.$('.save-search-input-container')).render();
        }
    });

});