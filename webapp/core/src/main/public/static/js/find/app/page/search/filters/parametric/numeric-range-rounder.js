/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([], function() {
    'use strict';

    const SIGNIFICANT_FIGURES = 3;

    return function(numberOfSignificantFigures) {
        const significantFigures = numberOfSignificantFigures || SIGNIFICANT_FIGURES;
        return {
            round: function(value, min, max) {
                const diff = max - min;
                const scientificDiff = diff.toExponential();
                const exponent = +scientificDiff.substring(scientificDiff.indexOf('e') + 1);
                return diff === 0
                    ? +value.toPrecision(significantFigures)
                    : Math.round(value * Math.pow(10, significantFigures - exponent - 1)) / Math.pow(10, significantFigures - exponent - 1);
            }
        }
    }
});
