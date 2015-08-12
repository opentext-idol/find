define([
    'underscore',
    'backbone',
    'fieldtext/js/field-text-parser',
    'find/app/model/parametric-collection',
    'find/app/page/parametric/parametric-display'
], function(_, Backbone, parser, ParametricCollection, ParametricDisplay) {

    function ParametricController(options) {
        this.queryModel = options.queryModel;
        this.collection = new ParametricCollection();

        this.view = new ParametricDisplay({
            queryModel: this.queryModel,
            parametricCollection: this.collection
        });

        this.listenTo(this.queryModel, 'change', function() {
            if(this.queryModel.hasAnyChangedAttributes(['queryText', 'indexes', 'fieldText'])) {
                this.collection.fetch({
                    data: {
                        databases: this.queryModel.get('indexes'),
                        queryText: this.queryModel.get('queryText'),
                        fieldText: this.queryModel.getFieldTextString() || null
                    }
                });
            }
        }, this);

        this.listenTo(this.view, 'change', function(parametricValues) {
            var expressionNodes = parametricValues.map(function(data) {
                var node = new parser.ExpressionNode("MATCH", [data.field], data.values);
                node.displayField = data.displayField;
                return node;
            }, this).value();

            if(!_.isEmpty(expressionNodes)) {
                this.requestFieldText = _.reduce(expressionNodes, function(memo, expression) {
                    return memo.AND(expression);
                });
            }
            else {
                this.requestFieldText = null;
            }

            this.queryModel.setParametricFieldText(this.requestFieldText);
        });
    }

    _.extend(ParametricController.prototype, Backbone.Events);
    return ParametricController;

});
