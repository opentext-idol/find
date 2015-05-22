define([
    'backbone',
    'underscore',
    'find/app/model/parametric-collection',
    'fieldtext/js/field-text-parser'
], function(Backbone, _, ParametricCollection, parser) {

    return Backbone.Model.extend({

        updateParametricFieldText: function() {
            var fieldTextString = null;

            if (this.requestFieldText) {
                fieldTextString = this.requestFieldText.toString();
            }

            this.collection.fetch({
                data: {
                    databases: this.indexes,
                    queryText: this.queryText,
                    fieldText: fieldTextString
                },
                error: _.bind(function (model, err) {
                    console.log(err);
                }, this)
            })
        },

        update: function () {
            if (this.queryText && !_.isEmpty(this.indexes)) {
                this.updateParametricFieldText();
            }
        },

        initialize: function (options) {
            this.collection = options.parametricCollection;

            this.indexes = options.indexes;
            this.queryText = options.queryText;
        },

        setIndexes: function (indexes) {
            if(this.indexes !== indexes) {
                this.indexes = indexes;
                this.update();
            }
        },

        setQueryText: function (queryText) {
            if(this.queryText !== queryText) {
                this.queryText = queryText;
                this.update();
            }
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

            this.trigger('change', this.requestFieldText);

            this.update();
        }

    })
});
