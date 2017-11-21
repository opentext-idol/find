/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(value, defaultProtocol) {
        if (!value || /^\w+:\/\//.test(value)) {
            return value;
        }

        return (defaultProtocol || 'http://') + value;
    };

});
