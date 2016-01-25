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

    return Backbone.View.extend({
        events: {
            'show.bs.tab': function(event) {
                this.searchModel.set('selectedSearchCid', $(event.target).attr('data-search-cid'));
            }
        },

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;
            this.searchModel = options.searchModel;
            this.savedSearchCollection = options.savedSearchCollection;
            this.ServiceView = options.ServiceView;

            this.tabListView = new ListView({
                collection: this.savedSearchCollection,
                ItemView: TabItemView
            });

            this.contentViews = {};

            this.listenTo(this.searchModel, 'change:selectedSearchCid', function() {
                this.selectContentView();
                this.updateSelectedTab();
            });

            this.listenTo(this.savedSearchCollection, 'remove', function(savedSearch) {
                var cid = savedSearch.cid;
                this.contentViews[cid].remove();
                delete this.contentViews[cid];
            });
        },

        render: function() {
            this.$el.html(html);

            this.tabListView.setElement(this.$('.saved-search-tabs-list')).render();

            _.each(this.contentViews, function(view) {
                this.$('.search-tabs-content').append(view.$el);
            }, this);

            this.updateSelectedTab();
            this.selectContentView();
        },

        selectContentView: function() {
            var cid = this.searchModel.get('selectedSearchCid');

            _.each(this.contentViews, function(view) {
                view.$el.addClass('hide');
            });

            if (cid) {
                var view = this.contentViews[cid];

                if (!view) {
                    view = new this.ServiceView({
                        indexesCollection: this.indexesCollection,
                        model: this.savedSearchCollection.get(cid),
                        searchModel: this.searchModel
                    });

                    this.contentViews[cid] = view;
                    view.render();
                    this.$('.search-tabs-content').append(view.$el);
                }

                view.$el.removeClass('hide');
            }
        },

        updateSelectedTab: function() {
            var cid = this.searchModel.get('selectedSearchCid');
            this.$('[data-search-cid="' + cid + '"]').tab('show');
        }
    });

});
