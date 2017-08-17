define([
    'underscore',
    'fieldtext/js/field-text-parser',
    'find/app/configuration'
], function(_, parser, configuration) {

    const config = configuration();

    let extraFieldText = null;

    if (config && config.fieldsInfo && config.fieldsInfo.rating) {
        const fieldNames = config.fieldsInfo.rating.names;

        if (fieldNames && fieldNames.length) {
            // BIAS takes the form (optimum, range, percentage)
            // We want to BIAS 1 or 2 to be less than usual, and 4 and 5 to be more than usual,
            //   so we're forced to do two BIAS operators.
            extraFieldText =
                new parser.ExpressionNode('BIAS', fieldNames, [5, 2, 100]).AND
                    (new parser.ExpressionNode('BIAS', fieldNames, [1, 2, -100])
                );
        }
    }

    return function(origFieldText){
        if (extraFieldText) {
            return origFieldText ? origFieldText.AND(extraFieldText) : extraFieldText;
        }
        return origFieldText;
    }
});