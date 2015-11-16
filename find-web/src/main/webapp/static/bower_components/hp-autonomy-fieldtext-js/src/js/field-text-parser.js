define([
    '../../../underscore/underscore',
    'peg',
    'text!fieldtext/js/field-text.pegjs'
], function(_, Peg, grammer) {

    var BooleanNode = function(operator, left, right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    };

    BooleanNode.build = function(node) {
        return new BooleanNode(node.boolean, convert(node.left), convert(node.right));
    };

    _.extend(BooleanNode.prototype, {
        toString: function() {
            return [this.left.toString(), this.operator, this.right.toString()].join(' ')
        }
    });

    var BracketedNode = function(node) {
        this.fieldText = convert(node.fieldtext);
    };

    BracketedNode.build = function(node) {
        return new BracketedNode(node);
    };

    _.extend(BracketedNode.prototype, {
        toString: function() {
            return '(' + this.fieldText.toString() + ')';
        }
    });

    var ExpressionNode = function(operator, fields, values) {
        this.operator = operator;
        this.fields = fields;
        this.values = values;
    };

    ExpressionNode.build = function(node) {
        return new ExpressionNode(node.operator, node.fields, node.values);
    };

    _.extend(ExpressionNode.prototype, {
        toString: function() {
            return this.operator + '{' +
                this.values.join(',') + '}' + ':' +
                this.fields.join(':');
        }
    });

    _.each([ExpressionNode.prototype, BracketedNode.prototype, BooleanNode.prototype], function(nodeType) {
        _.extend(nodeType, _.reduce(['AND', 'OR', 'XOR', 'BEFORE', 'AFTER'], function(methods, operator) {
            methods[operator] = function (right) {
                return new BooleanNode(operator, this, right);
            };

            return methods;
    }, {}))});

    var NegativeNode = function(node) {
        var newNode = _.clone(node);
        delete newNode.negative;

        this.fieldText = convert(newNode);
    };

    NegativeNode.build = function(node) {
        return new NegativeNode(node);
    };

    _.extend(NegativeNode.prototype, {
        negative: true,

        toString: function() {
            return 'NOT ' + this.fieldText.toString();
        }
    });

    var convert = function(node) {
        if (node.boolean) {
            return BooleanNode.build(node);
        }
        else if (node.negative) {
            return NegativeNode.build(node);
        }
        else if (node.fieldtext) {
            return BracketedNode.build(node);
        }
        else {
            return ExpressionNode.build(node);
        }
    };

    var parser = Peg.buildParser(grammer);

    var module = {
        ExpressionNode: ExpressionNode,
        Null: {toString: function() { return null }},

        parse: function(fieldText) {
            var tree = parser.parse(fieldText);

            return convert(tree);
        }
    };

    _.extend(module, _.reduce(['AND', 'OR', 'XOR', 'BEFORE', 'AFTER'], function(methods, operator) {
        methods[operator] = function (left, right) {
            if(left && right) {
                return left[operator](right);
            } else if(left) {
                return left;
            } else if(right) {
                return right;
            } else {
                return module.Null
            }
        };

        return methods;
    }, {}));

    return module;
});