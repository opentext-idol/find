/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/parametric/numeric-parametric-view'
], function(NumericParametricView) {
    //noinspection JSUnusedGlobalSymbols
    return NumericParametricView.extend({

        getBucketingRequestData: function(fieldNames, targetNumberOfBuckets) {
            return {
                fieldNames: fieldNames,
                queryText: '*',
                targetNumberOfBuckets: targetNumberOfBuckets
            };
        }
    });
});