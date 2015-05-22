define([
    'backbone',
    'find/app/model/parametric-collection',
    'find/app/page/parametric/parametric-logic',
    'find/app/page/parametric/parametric-display'
], function(Backbone, ParametricCollection, ParametricLogic, ParametricDisplay) {

    return Backbone.Model.extend({
        initialize: function (options) {
            this.collection = options.parametricCollection;

            this.logic = new ParametricLogic(options);
            this.view = new ParametricDisplay(options);

            this.listenTo(this.view, 'change', function(parametricValues) {
                this.logic.setRequestParametricValues(parametricValues);
            });
        }
    })
})
