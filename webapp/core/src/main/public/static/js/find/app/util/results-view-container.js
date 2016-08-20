define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'text!find/templates/app/util/content-container.html'
], function (Backbone, _, $, i18n, contentContainerTemplate) {

    return Backbone.View.extend({
        contentContainerTemplate: _.template(contentContainerTemplate, {variable: 'data'}),

        initialize: function(options) {
            this.views = options.views;
            this.model = options.model;

            this.listenTo(this.model, 'change:selectedTab', this.selectTab);
        },

        render: function() {
            this.$tabContent = $('<div class="tab-content"></div>');

            var selectedTab = this.model.get('selectedTab');

            _.each(this.views, function(viewData) {
                var $viewElement = $(this.contentContainerTemplate(viewData))
                    .toggleClass('active', viewData.id === selectedTab)
                    .appendTo(this.$tabContent);

                viewData.content = new viewData.Constructor(viewData.constructorArguments);

                _.each(viewData.events, function(listener, eventName) {
                    this.listenTo(viewData.content, eventName, listener);
                }, this);

                viewData.content.setElement($viewElement);
            }, this);

            this.$el.empty().append(this.$tabContent);
            this.selectTab();
        },

        selectTab: function() {
            var tabId = this.model.get('selectedTab');
            var viewData = _.findWhere(this.views, {id: tabId});

            // Deactivate all tabs and activate the selected tab
            this.$tabContent.find('> .active').removeClass('active');
            this.$tabContent.find('#' + viewData.uniqueId).addClass('active');

            if (viewData) {
                if (!viewData.rendered) {
                    viewData.content.render();
                    viewData.rendered = true;
                }

                if (viewData.content.update) {
                    viewData.content.update();
                }
            }
        },

        remove: function() {
            _.chain(this.views)
                .pluck('content')
                .invoke('remove');

            Backbone.View.prototype.remove.call(this);
        }
    });

});
