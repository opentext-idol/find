define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/page/search/search-tab-item-view',
    'find/app/model/query-text-model',
    'find/app/util/model-any-changed-attribute-listener',
    'js-whatever/js/list-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/tabbed-search-view.html',
    'bootstrap'
], function(Backbone, _, $, TabItemView, QueryTextModel, addChangeListener, ListView, i18n, template) {

    'use strict';

    var QUERY_TEXT_MODEL_ATTRIBUTES = ['inputText', 'relatedConcepts'];
    var html = _.template(template)({i18n: i18n});

    return Backbone.View.extend({
        events: {
            'shown.bs.tab [data-search-cid]': function(event) {
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

            this.serviceViews = {};

            this.listenTo(this.searchModel, 'change:selectedSearchCid', function() {
                this.selectContentView();
                this.updateSelectedTab();
            });

            addChangeListener(this, this.searchModel, QUERY_TEXT_MODEL_ATTRIBUTES, function() {
                var selectedSearchCid = this.searchModel.get('selectedSearchCid');

                if (selectedSearchCid) {
                    var queryTextModel = this.serviceViews[selectedSearchCid].queryTextModel;
                    queryTextModel.set(this.searchModel.pick('inputText', 'relatedConcepts'));
                }
            });

            this.listenTo(this.savedSearchCollection, 'remove', function(savedSearch) {
                var cid = savedSearch.cid;
                this.serviceViews[cid].view.remove();
                delete this.serviceViews[cid];
            });
        },

        render: function() {
            this.$el.html(html);

            this.tabListView.setElement(this.$('.saved-search-tabs-list')).render();

            _.each(this.serviceViews, function(data) {
                this.$('.search-tabs-content').append(data.view.$el);
            }, this);

            this.updateSelectedTab();
            this.selectContentView();
        },

        selectContentView: function() {
            var cid = this.searchModel.get('selectedSearchCid');

            _.each(this.serviceViews, function(data) {
                data.view.$el.addClass('hide');
                this.stopListening(data.queryTextModel);
            }, this);

            if (cid) {
                var viewData;
                var savedSearchModel = this.savedSearchCollection.get(cid);

                if (this.serviceViews[cid]) {
                    viewData = this.serviceViews[cid];
                } else {
                    var queryTextModel = new QueryTextModel(savedSearchModel.toQueryTextModelAttributes());

                    this.serviceViews[cid] = viewData = {
                        queryTextModel: queryTextModel,
                        view: new this.ServiceView({
                            indexesCollection: this.indexesCollection,
                            searchModel: this.searchModel,
                            queryTextModel: queryTextModel,
                            savedSearchModel: savedSearchModel
                        })
                    };

                    viewData.view.render();
                    this.$('.search-tabs-content').append(viewData.view.$el);
                }

                addChangeListener(this, viewData.queryTextModel, QUERY_TEXT_MODEL_ATTRIBUTES, function() {
                    this.searchModel.set(viewData.queryTextModel.pick(QUERY_TEXT_MODEL_ATTRIBUTES));
                });

                this.searchModel.set(viewData.queryTextModel.pick(QUERY_TEXT_MODEL_ATTRIBUTES));
                viewData.view.$el.removeClass('hide');
            }
        },

        updateSelectedTab: function() {
            var cid = this.searchModel.get('selectedSearchCid');
            this.$('[data-search-cid="' + cid + '"]').tab('show');
        }
    });

});
