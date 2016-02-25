define([
    'js-whatever/js/modal',
    'jquery',
    'find/app/page/search/search-to-compare-view',
    'find/app/model/comparisons/comparison-model',
    'text!find/templates/app/page/search/compare-modal-footer.html',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/nls/bundle'
], function(Modal, $, SearchToCompare, ComparisonModel, compareModalFooter, loadingSpinnerTemplate, i18n) {

    return Modal.extend({
        footerTemplate: _.template(compareModalFooter),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),

        initialize: function(options) {
            this.comparisonSuccessCallback = options.comparisonSuccessCallback;

            this.selectedId = null;

            this.searchToCompare = new SearchToCompare({
                savedSearchCollection: options.savedSearchCollection,
                selectedSearch: options.selectedSearch
            });

            Modal.prototype.initialize.call(this, {
                actionButtonClass: 'button-primary disabled not-clickable',
                actionButtonText: i18n['search.compare'],
                secondaryButtonText: i18n['app.cancel'],
                contentView: this.searchToCompare,
                title: i18n['search.compare.comparedSaved'],
                actionButtonCallback: _.bind(function() {
                    this.$errorMessage.text('');
                    this.$loadingSpinner.removeClass('hide');
                    this.$confirmButton.prop('disabled', true);

                    var searchModels = {
                        first: options.selectedSearch,
                        second: options.savedSearchCollection.get({cid: this.selectedId})
                    };

                    var comparisonModel = ComparisonModel.fromModels(searchModels.first, searchModels.second);

                    this.xhr = comparisonModel.save({}, {
                        success: _.bind(function() {
                            this.comparisonSuccessCallback(comparisonModel, searchModels);
                            this.hide()
                        }, this),
                        error: _.bind(function() {
                            this.$errorMessage.text(i18n['search.compare.error.default']);
                            this.$loadingSpinner.addClass('hide');
                            this.$confirmButton.prop('disabled', false);
                        }, this)
                    });
                }, this)
            });

            this.listenTo(this.searchToCompare, 'selected', function(selectedId){
                this.selectedId = selectedId;
                this.$('.modal-action-button').toggleClass('disabled not-clickable', !this.selectedId);
            });
        },

        render: function() {
            Modal.prototype.render.call(this);

            this.$('.modal-footer').prepend(this.footerTemplate);

            this.$confirmButton = this.$('.modal-action-button');
            this.$errorMessage = this.$('.comparison-create-error-message');
            this.$loadingSpinner = this.$('.compare-modal-error-spinner');

            $(this.loadingTemplate)
                .addClass('inline-block')
                .appendTo(this.$loadingSpinner);
        },

        remove: function () {
            Modal.prototype.remove.call(this);

            if (this.xhr) {
                this.xhr.abort();
            }
        }
    });

});