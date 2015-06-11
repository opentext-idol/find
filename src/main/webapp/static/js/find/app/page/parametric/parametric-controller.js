define([
    'backbone',
    'find/app/model/parametric-collection',
    'find/app/page/parametric/parametric-logic',
    'find/app/page/parametric/parametric-display'
], function(Backbone, ParametricCollection, ParametricLogic, ParametricDisplay) {

    return Backbone.Model.extend({
        initialize: function (options) {
            this.queryModel = options.queryModel;

            this.collection = new ParametricCollection();

            this.logic = new ParametricLogic({
                queryModel: this.queryModel,
                parametricCollection: this.collection
            });

            this.view = new ParametricDisplay({
                parametricCollection: this.collection
            });

            this.listenTo(this.view, 'change', function(parametricValues) {
                this.logic.setRequestParametricValues(parametricValues);
            });
        }
    })
});
