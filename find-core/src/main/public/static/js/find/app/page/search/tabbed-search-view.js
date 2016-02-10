define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/page/search/search-tab-item-view',
    'js-whatever/js/list-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/tabbed-search-view.html',
    'bootstrap'
], function(Backbone, _, $, TabItemView, ListView, i18n, template) {

    'use strict';

    var html = _.template(template)({i18n: i18n});
    var startNewSearchHtml = _.template('<li class="start-new-search"><a><i class="hp-icon hp-add"></i></a></li>');

    return Backbone.View.extend({
        events: {
            'shown.bs.tab [data-search-cid]': function(event) {
                this.searchModel.set('selectedSearchCid', $(event.target).attr('data-search-cid'));
            },
            'click .start-new-search': function() {
                this.trigger('startNewSearch');
            }
        },

        initialize: function(options) {
            this.searchModel = options.searchModel;
            this.savedSearchCollection = options.savedSearchCollection;

            this.tabListView = new ListView({
                collection: this.savedSearchCollection,
                ItemView: TabItemView,
                headerHtml: startNewSearchHtml,
                itemOptions: {
                    queryStates: options.queryStates
                }
            });

            this.listenTo(this.searchModel, 'change:selectedSearchCid', this.updateSelectedTab);
        },

        render: function() {
            this.$el.html(html);

            this.tabListView.setElement(this.$('.search-tabs-list')).render();
            this.updateSelectedTab();
        },

        updateSelectedTab: function() {
            var cid = this.searchModel.get('selectedSearchCid');
            this.$('[data-search-cid="' + cid + '"]').tab('show');
        }
    });

});
