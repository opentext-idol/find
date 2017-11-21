/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(value, size) {
        return value && String(value).replace(/(\/wikipedia\/commons\/)([^/]+\/[^/]+\/([^/]+\.(?:jpg|jpeg|gif|png)))/, function(all, first, second, filename){
            return first + 'thumb/' + second + '/' + (size||300) + 'px-' + filename;
        });
    };

});
