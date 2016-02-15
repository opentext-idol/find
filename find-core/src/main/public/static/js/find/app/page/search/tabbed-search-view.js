define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/page/search/search-tab-item-view',
    'js-whatever/js/list-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/tabbed-search-view.html'
], function(Backbone, _, $, TabItemView, ListView, i18n, template) {

    'use strict';

    var html = _.template(template)({i18n: i18n});
    var startNewSearchHtml = _.template('<li class="start-new-search m-t-md m-l-sm m-r-sm"><a><i class="hp-icon hp-add"></i></a></li>');

    return Backbone.View.extend({
        events: {
            'click .search-tab': function(event) {
                this.model.set('selectedSearchCid', $(event.currentTarget).find('[data-search-cid]').attr('data-search-cid'));
            },
            'click .start-new-search': function() {
                this.trigger('startNewSearch');
            }
        },

        initialize: function(options) {
            this.savedSearchCollection = options.savedSearchCollection;

            this.tabListView = new ListView({
                collection: this.savedSearchCollection,
                ItemView: TabItemView,
                headerHtml: startNewSearchHtml,
                itemOptions: {
                    queryStates: options.queryStates
                }
            });

            this.listenTo(this.model, 'change:selectedSearchCid', this.updateSelectedTab);
        },

        render: function() {
            this.$el.html(html);

            this.tabListView.setElement(this.$('.search-tabs-list')).render();
            this.updateSelectedTab();
        },

        updateSelectedTab: function() {
            var cid = this.model.get('selectedSearchCid');
            this.$('.search-tab').removeClass('active');

            if (cid) {
                this.$('[data-search-cid="' + cid + '"]').closest('.search-tab').addClass('active');
            }
        }
    });

});
