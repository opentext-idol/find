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
], function (Backbone, _, $, i18n, ResultsView, TopicMapView, template, selectorTemplate, contentContainerTemplate) {

    return Backbone.View.extend({
        template: _.template(template),
        selectorTemplate: _.template(selectorTemplate, {variable: 'data'}),
        contentContainerTemplate: _.template(contentContainerTemplate, {variable: 'data'}),

        // Abstract
        ResultsView: null,

        initialize: function (options) {
            this.views = [{
                id: 'list',
                selector: {
                    displayNameKey: 'list',
                    icon: 'hp-list'
                },
                content: new this.ResultsView({
                    documentsCollection: options.documentsCollection,
                    entityCollection: options.entityCollection,
                    indexesCollection: options.indexesCollection,
                    queryModel: options.queryModel
                })
            }, {
                id: 'topic-map',
                selector: {
                    displayNameKey: 'topic-map',
                    icon: 'hp-grid'
                },
                content: new TopicMapView({
                    entityCollection: options.entityCollection,
                    queryTextModel: options.queryTextModel
                })
            }];
        },

        render: function () {
            this.$el.html(this.template);

            var $selectorList = this.$('.selector-list');
            var $contentList = this.$('.content-list');

            _.each(this.views, function (view, index) {
                $(this.selectorTemplate({
                    i18n: i18n,
                    id: view.id,
                    selector: view.selector
                })).toggleClass('active', index === 0).appendTo($selectorList);

                var $viewElement = $(this.contentContainerTemplate(view)).toggleClass('active', index === 0).appendTo($contentList);
                view.content.setElement($viewElement);
            }, this);

            // Triggers a "selected" event with the id results view type
            this.$('a[data-toggle="tab"]').on('hide.bs.tab', _.bind(function (e) {
                this.trigger('selected', $(e.relatedTarget).attr('data-id'));
            }, this));

            this.views[0].content.render();
            this.views[0].rendered = true;

            this.$el.find('a[data-id="topic-map"]').on('show.bs.tab', _.bind(function (event) {
                var view = _.find(this.views, function (view) {
                    return view.id === $(event.currentTarget).attr('data-id');
                });

                if (!view.rendered) {
                    view.content.render();
                    view.rendered = true;
                }

                if (view.content.update) {
                    view.content.update();
                }

            }, this));
        }
    });

});
