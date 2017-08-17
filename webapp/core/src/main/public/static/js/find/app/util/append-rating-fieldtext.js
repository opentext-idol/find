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
            extraFieldText = new parser.ExpressionNode('BIAS', fieldNames, [3, 2, 100]);
        }
    }

    return function(origFieldText){
        if (extraFieldText) {
            return origFieldText ? origFieldText.AND(extraFieldText) : extraFieldText;
        }
        return origFieldText;
    }
});