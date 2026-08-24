/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const escapeRegex = require('js-whatever/js/escape-regex');

module.exports = function(value, template, options) {
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

