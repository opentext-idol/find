define([
    'backbone',
    'find/app/util/array-equality',
    'find/app/page/search/saved-search-control/save-search-input',
    'text!find/templates/app/page/search/saved-search-control/saved-search-control-view.html',
    'i18n!find/nls/bundle'
], function(Backbone, arrayEquality, SaveSearchInput, template, i18n) {

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

            this.queryTextModel = options.queryTextModel;
            this.selectedParametricValues = options.selectedParametricValues;
            this.selectedIndexesCollection = options.selectedIndexesCollection;

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

            this.listenTo(this.queryModel, 'change', function() {
                this.$updateButton.toggleClass('hide', this.searchChanged());
            });

            this.saveSearchInput = new SaveSearchInput({
                savedSearchModel: this.savedSearchModel,
                queryModel: this.queryModel,
                savedSearchControlModel: this.model
            });
        },

        searchChanged: function() {
            return this.savedSearchModel.get('inputText') !== this.queryTextModel.get('inputText')
                && arrayEquality(this.savedSearchModel.get('relatedConcepts'), this.queryTextModel.get('relatedConcepts'))
                && arrayEquality(this.savedSearchModel.get('indexes'), this.selectedIndexesCollection.toResourceIdentifiers(), _.isEqual)
                && arrayEquality(this.savedSearchModel.get('parametricValues'), _.map(this.selectedParametricValues, function(model) {
                    return model.pick('field', 'value');
                }), _.isEqual);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                openEditDisplayNameKey: this.createMode ? 'create': 'edit'
            }));

            this.saveSearchInput.setElement(this.$('.save-search-input-container')).render();

            this.$updateButton = this.$('.update-button');
        }
    });

});