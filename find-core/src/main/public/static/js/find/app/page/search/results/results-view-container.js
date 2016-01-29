define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'find/app/page/search/results/results-view',
    'find/app/page/search/results/topic-map-view',
    'text!find/templates/app/page/search/results/results-view-container.html',
    'text!find/templates/app/page/search/results/selector.html',
    'text!find/templates/app/page/search/results/content-container.html'
], function (Backbone, _, $, i18n, ResultsView, TopicMapView, viewHtml, selectorTemplate, contentContainerTemplate) {

    return Backbone.View.extend({
        selectorTemplate: _.template(selectorTemplate, {variable: 'data'}),
        contentContainerTemplate: _.template(contentContainerTemplate, {variable: 'data'}),

        // Abstract
        ResultsView: null,

        events: {
            'shown.bs.tab [data-tab-id]': function(event) {
                var selectedTab = $(event.target).attr('data-tab-id');
                this.model.set('selectedTab', selectedTab);
            }
        },

        initialize: function(options) {
            this.views = _.map([{
                id: 'list',
                Constructor: this.ResultsView,
                selector: {
                    displayNameKey: 'list',
                    icon: 'hp-list'
                }
            }, {
                id: 'topic-map',
                Constructor: TopicMapView,
                selector: {
                    displayNameKey: 'topic-map',
                    icon: 'hp-grid'
                }
            }], function(viewData) {
                // Add a unique ID for DOM ids and construct the view
                return _.extend({
                    uniqueId: _.uniqueId('results-view-item-'),
                    content: new viewData.Constructor({
                        documentsCollection: options.documentsCollection,
                        entityCollection: options.entityCollection,
                        indexesCollection: options.indexesCollection,
                        queryModel: options.queryModel,
                        queryTextModel: options.queryTextModel
                    })
                }, viewData);
            });

            this.model = new Backbone.Model({
                // ID of the currently selected tab
                selectedTab: this.views[0].id
            });

            this.listenTo(this.model, 'change:selectedTab', this.selectTab);
        },

        render: function() {
            this.$el.html(viewHtml);

            var $selectorList = this.$('.selector-list');
            var $contentList = this.$('.content-list');

            var selectedTab = this.model.get('selectedTab');

            _.each(this.views, function(viewData) {
                var isSelectedTab = viewData.id === selectedTab;

                $(this.selectorTemplate({
                    i18n: i18n,
                    id: viewData.id,
                    uniqueId: viewData.uniqueId,
                    selector: viewData.selector
                })).toggleClass('active', isSelectedTab).appendTo($selectorList);

                var $viewElement = $(this.contentContainerTemplate(viewData)).toggleClass('active', isSelectedTab).appendTo($contentList);
                viewData.content.setElement($viewElement);
            }, this);

            this.selectTab();
        },

        selectTab: function() {
            var tabId = this.model.get('selectedTab');
            var viewData = _.findWhere(this.views, {id: tabId});

            if (viewData) {
                if (!viewData.rendered) {
                    viewData.content.render();
                    viewData.rendered = true;
                }

                if (viewData.content.update) {
                    viewData.content.update();
                }
            }
        }
    });

});
