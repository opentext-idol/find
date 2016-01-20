define([
    'backbone',
    'underscore',
    'find/app/page/search/results/results-view',
    'text!find/templates/app/page/search/results/results-view-container.html',
    'text!find/templates/app/page/search/results/selector.html',
    'text!find/templates/app/page/search/results/content-container.html'
], function(Backbone, _, ResultsView, template, selectorTemplate, contentContainerTemplate) {

    return Backbone.View.extend({
        template: _.template(template),
        selectorTemplate: _.template(selectorTemplate, {variable: 'data'}),
        contentContainerTemplate: _.template(contentContainerTemplate, {variable: 'data'}),

        ResultsView: $.noop,

        initialize: function(options) {
            this.views = [{
                id: 'list',
                selector: {
                    className: 'results-list-container',
                    displayName: 'List',
                    icon: 'hp-list'
                },
                content: new this.ResultsView({
                    documentsCollection: options.documentsCollection,
                    entityCollection: options.entityCollection,
                    indexesCollection: options.indexesCollection,
                    queryModel: options.queryModel
                })
            }, {
                id: 'map',
                selector: {
                    className: 'results-map-container',
                    displayName: 'Map',
                    icon: 'hp-grid'
                },
                content: new (Backbone.View.extend({
                    render: function() {
                        this.$el.html('It works!')
                    }
                }))()
            }];
        },

        render: function() {
            this.$el.html(this.template);

            var $selectorList = this.$('.selector-list');
            var $contentList = this.$('.content-list');

            _.each(this.views, function(view, index) {
                $(this.selectorTemplate(view)).toggleClass('active', index === 0).appendTo($selectorList);

                var $viewElement = $(this.contentContainerTemplate(view)).toggleClass('active', index === 0).appendTo($contentList)
                view.content.setElement($viewElement).render();
            }, this);
        }

    });

});
