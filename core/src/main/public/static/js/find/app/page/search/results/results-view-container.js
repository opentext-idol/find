define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/results-view-container.html',
    'text!find/templates/app/page/search/results/content-container.html'
], function (Backbone, _, $, i18n, viewHtml, contentContainerTemplate) {

    return Backbone.View.extend({
        contentContainerTemplate: _.template(contentContainerTemplate, {variable: 'data'}),

        initialize: function(options) {
            this.views = options.views;
            this.model = options.model;

            this.listenTo(this.model, 'change:selectedTab', this.selectTab);
        },

        render: function() {
            this.$el.html(viewHtml);

            this.$contentList = this.$('.content-list');

            var selectedTab = this.model.get('selectedTab');

            _.each(this.views, function(viewData) {
                var $viewElement = $(this.contentContainerTemplate(viewData))
                    .toggleClass('active', viewData.id === selectedTab)
                    .appendTo(this.$contentList);

                viewData.content.setElement($viewElement);
            }, this);

            this.selectTab();
        },

        selectTab: function() {
            var tabId = this.model.get('selectedTab');
            var viewData = _.findWhere(this.views, {id: tabId});

            // Deactivate all tabs and activate the selected tab
            this.$contentList.find('.tab-pane').removeClass('active');
            this.$contentList.find('#' + viewData.uniqueId).addClass('active');

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
