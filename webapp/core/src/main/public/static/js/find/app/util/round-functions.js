/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore'
], function(_) {

    return function(roundSettings) {
        return roundSettings === false ? _.identity
            : roundSettings in Math ? Math[roundSettings]
            : roundSettings >= 0 ? function(v){ return Number(v).toFixed(roundSettings) }
            : Math.round
    };

});