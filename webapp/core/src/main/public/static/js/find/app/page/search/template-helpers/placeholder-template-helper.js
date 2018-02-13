/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'js-whatever/js/escape-regex'
], function(_, escapeRegex) {

    // Utility to allow filling in a template containing placeholders with variables from a hash, array, or split from a string,
    // e.g. 'hello $username$, welcome to $site$' or 'hello $0$, welcome to $1$'.
    // We allow repeating the placeholder, so you can do e.g. '$$100 dollars' to represent '$100 dollars'.
    return function(value, template, options) {
        if (value) {
            const values = (value instanceof Array || value instanceof Object) ? value : String(value).split(options.hash.delimiter || ',');
            const placeholder = options.hash.placeholder || '$';
            const escapedPlaceholder = escapeRegex(placeholder);

            const regex = new RegExp(escapedPlaceholder + '([^'+ escapedPlaceholder +']*)' + escapedPlaceholder, 'g');

            return template.replace(regex, function(rawMatch, key){
                if (!key) {
                    // repeated placeholder, e.g. '$$100 dollars' to '$100 dollars'
                    return placeholder;
                }

                // fill in the placeholder, or if not found, retain original text
                return values[key] || rawMatch;
            });
        }

        return template;
    };

});
