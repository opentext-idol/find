define([
    'backbone',
    'js-whatever/js/modal',
    'jquery',
    'find/app/page/search/filters/parametric/parametric-select-modal-view',
    'parametric-refinement/selected-values-collection',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle'
], function(Backbone, Modal, $, ParametricSelectView, SelectedValuesCollection, loadingSpinnerTemplate, i18n) {

    return Modal.extend({
        className: 'modal fade parametric-modal',

        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),

        initialize: function(options) {
            this.selectCollection = new SelectedValuesCollection();
            this.parametricDisplayCollection = options.parametricDisplayCollection;
            this.selectedParametricValues = options.selectedParametricValues;

            this.parametricSelectView = new ParametricSelectView({
                collection: options.collection,
                parametricDisplayCollection: this.parametricDisplayCollection,
                currentFieldGroup: options.currentFieldGroup,
                selectCollection: this.selectCollection
            });

            Modal.prototype.initialize.call(this, {
                actionButtonClass: 'button-primary',
                actionButtonText: i18n['app.apply'],
                secondaryButtonText: i18n['app.cancel'],
                contentView: this.parametricSelectView,
                title: i18n['search.parametricFilters.modal.title'],
                actionButtonCallback: _.bind(function() {
                    this.selectedParametricValues.set(this.selectCollection.where({selected: true}), {remove: false});
                    this.selectedParametricValues.remove(this.selectCollection.where({selected: false}));

                    this.hide();
                }, this)
            });

            this.$el.on('shown.bs.modal', _.bind(this.parametricSelectView.renderFields, this));
        }
    });

});