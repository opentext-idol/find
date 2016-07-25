define([
    'js-whatever/js/modal',
    'jquery',
    'find/idol/app/page/search/comparison/search-to-compare-view',
    'find/idol/app/model/comparison/comparison-model',
    'find/app/model/saved-searches/saved-search-model',
    'text!find/idol/templates/comparison/compare-modal-footer.html',
    'text!find/templates/app/page/loading-spinner.html',
    'i18n!find/idol/nls/comparisons',
    'i18n!find/nls/bundle'
], function(Modal, $, SearchToCompare, ComparisonModel, SavedSearchModel, compareModalFooter, loadingSpinnerTemplate, comparisonsI18n, i18n) {

    function getSearchModelWithDefault(savedSearchCollection, queryStates) {
        return function(cid) {
            var search = savedSearchCollection.get(cid);

            if (search.isNew()) {
                search = new SavedSearchModel(_.extend({title: search.get('title')}, SavedSearchModel.attributesFromQueryState(queryStates.get(cid))));
            }

            return search;
        };
    }

    return Modal.extend({
        footerTemplate: _.template(compareModalFooter),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),

        initialize: function(options) {
            this.comparisonSuccessCallback = options.comparisonSuccessCallback;
            var savedSearchCollection = options.savedSearchCollection;
            var queryStates = options.queryStates;
            var getSearchModel = getSearchModelWithDefault(savedSearchCollection, queryStates);

            this.selectedId = null;

            var initialSearch = getSearchModel(options.cid);

            this.searchToCompare = new SearchToCompare({
                savedSearchCollection: savedSearchCollection,
                selectedSearch: initialSearch
            });

            Modal.prototype.initialize.call(this, {
                actionButtonClass: 'button-primary disabled not-clickable',
                actionButtonText: comparisonsI18n['compare'],
                secondaryButtonText: i18n['app.cancel'],
                contentView: this.searchToCompare,
                title: comparisonsI18n['search.compare.compareSaved'],
                actionButtonCallback: _.bind(function() {
                    this.$errorMessage.text('');
                    this.$loadingSpinner.removeClass('hide');
                    this.$confirmButton.prop('disabled', true);

                    var secondSearch = getSearchModel(this.selectedId);
                    
                    var searchModels = {
                        first: initialSearch,
                        second: secondSearch
                    };

                    var comparisonModel = ComparisonModel.fromModels(searchModels.first, searchModels.second);

                    this.xhr = comparisonModel.save({}, {
                        success: _.bind(function() {
                            this.comparisonSuccessCallback(comparisonModel, searchModels);
                            this.hide();
                        }, this),
                        error: _.bind(function() {
                            this.$errorMessage.text(comparisonsI18n['error.default']);
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