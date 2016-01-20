define([
    'backbone',
    'underscore',
    'find/app/page/search/results/results-view',
    'text!find/templates/app/page/search/results/results-view-container.html'
], function(Backbone, _, ResultsView, template) {

    return Backbone.View.extend({
        template: _.template(template),

        hideViews: function() {
            _.each(this.views, function(view) {
                view.$el.addClass('hide');
            });
        },

        selectView: function(name) {
            this.hideViews();
            this.views[name].$el.removeClass('hide');
        },

        events: {
            'click .result-view-type': function(e) {
                this.selectView(($(e.currentTarget).attr('data-type')));
            }
        },

        ResultsView: $.noop,

        initialize: function(options) {
            this.views = {
                list: new this.ResultsView({
                    documentsCollection: options.documentsCollection,
                    entityCollection: options.entityCollection,
                    indexesCollection: options.indexesCollection,
                    queryModel: options.queryModel
                }),
                map: new this.ResultsView({
                    documentsCollection: new Backbone.Collection(),
                    entityCollection: new Backbone.Collection(),
                    indexesCollection: new Backbone.Collection(),
                    queryModel: new Backbone.Model(),
                })
            };
        },

        render: function() {
            this.$el.html(this.template);

            this.views.list.setElement(this.$('.results-list-container')).render();
            this.views.map.setElement(this.$('.results-map-container')).render();

            this.selectView('list');
        }

    });

});
