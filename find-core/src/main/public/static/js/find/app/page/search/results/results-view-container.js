define([
    'backbone',
    'underscore',
    'find/app/page/search/results/results-view',
    'text!find/templates/app/page/search/results/results-view-container.html'
], function(Backbone, _, ResultsView, template) {

    return Backbone.View.extend({
        template: _.template(template),

        ResultsView: $.noop,

        initialize: function(options) {
            this.resultsView = new this.ResultsView({
                documentsCollection: options.documentsCollection,
                entityCollection: options.entityCollection,
                indexesCollection: options.indexesCollection,
                queryModel: options.queryModel
            });
        },

        render: function() {
            this.$el.html(this.template);

            this.resultsView.setElement(this.$('.results-list-container')).render();
        }

    });

});
