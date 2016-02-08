define([
    'js-whatever/js/modal',
    'find/app/page/search/search-to-compare-view',
    'i18n!find/nls/bundle'
], function(Modal, SearchToCompare, i18n) {

    return Modal.extend({
        initialize: function(options) {
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
                    options.callback(this.selectedId);
                    this.hide();
                }, this)
            });

            this.listenTo(this.searchToCompare, 'selected', function(selectedId){
                this.selectedId = selectedId;
                this.$('.modal-action-button').toggleClass('disabled not-clickable', !this.selectedId);
            });
        }
    });

});