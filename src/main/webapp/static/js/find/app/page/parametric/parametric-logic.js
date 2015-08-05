define([
    'backbone',
    'underscore',
    'find/app/model/parametric-collection',
    'fieldtext/js/field-text-parser'
], function(Backbone, _, ParametricCollection, parser) {

    return Backbone.Model.extend({

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.collection = options.parametricCollection;

            this.listenTo(this.queryModel, 'change', function() {
                if(!_.isEmpty(this.queryModel.get('indexes'))) {
                    this.collection.fetch({
                        data: {
                            databases: this.queryModel.get('indexes'),
                            queryText: this.queryModel.get('queryText'),
                            fieldText: this.queryModel.getFieldTextString() || null
                        }
                    });
                }
            }, this)
        },

        setRequestParametricValues: function(parametricValues) {
            var expressionNodes = parametricValues.map(function(data) {
                return new parser.ExpressionNode("MATCH", [data.field], data.values);
            }, this).value();

            if(!_.isEmpty(expressionNodes)) {
                this.requestFieldText = _.reduce(expressionNodes, function(memo, expression) {
                    return memo.AND(expression);
                })
            }
            else {
                this.requestFieldText = null;
            }

            this.queryModel.setParametricFieldText(this.requestFieldText);
        }

    })
});
