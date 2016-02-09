define([
    'backbone',
    'jquery',
    'underscore',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/search-to-compare-view.html'
], function(Backbone, $, _, i18n, template) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click [data-search-cid]': function(e) {
                var $target = $(e.currentTarget);

                this.$('[data-search-cid]').removeClass('selected-saved-search');
                $target.addClass('selected-saved-search');

                this.trigger('selected', $target.attr('data-search-cid'));
            }
        },

        initialize: function(options) {
            this.selectedSearch = options.selectedSearch;
            this.savedSearchCollection = options.savedSearchCollection;
        },

        render: function() {
            this.$el.html(this.template({
                i18n:i18n,
                selectedSearchTitle: this.selectedSearch.get('title'),
                searches: this.savedSearchCollection.reject(function(model){
                    return model.cid === this.selectedSearch.cid;
                }, this)
            }));
        }
    });
});
