define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/results-view-container.html',
    'text!find/templates/app/page/search/results/selector.html'
], function (Backbone, _, $, i18n, viewHtml, selectorTemplate) {

    return Backbone.View.extend({
        selectorTemplate: _.template(selectorTemplate, {variable: 'data'}),

        events: {
            'shown.bs.tab [data-tab-id]': function(event) {
                var selectedTab = $(event.target).attr('data-tab-id');
                this.model.set('selectedTab', selectedTab);
            }
        },

        initialize: function(options) {
            this.views = options.views;
            this.model = options.model;
        },

        render: function() {
            this.$el.html(viewHtml);

            var $selectorList = this.$('.selector-list');
            var selectedTab = this.model.get('selectedTab');

            _.each(this.views, function(viewData) {
                $(this.selectorTemplate({
                    i18n: i18n,
                    id: viewData.id,
                    uniqueId: viewData.uniqueId,
                    selector: viewData.selector
                })).toggleClass('active', viewData.id === selectedTab)
                    .appendTo($selectorList);
            }, this);
        }
    });

});
