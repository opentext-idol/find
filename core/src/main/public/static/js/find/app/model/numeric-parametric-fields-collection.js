/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection'
], function(FindBaseCollection) {

    function defaultCurrentRangeAttributes(absoluteRange) {
        if (absoluteRange.max === absoluteRange.min) {
            // The current max must always be greater than the current min for bars to be visible on the widgets. If there
            // is only one value for the field, the absolute max will equal the absolute min. In this case, default to a
            // range spanning 1 around this value.
            return {
                currentMin: absoluteRange.min - 0.5,
                currentMax: absoluteRange.min + 0.5
            };
        } else {
            // It is not possible to specify inclusive upper ranges when fetching parametric values from IDOL. To display the
            // extreme values, default to a range 1% larger than the absolute values.
            return {
                currentMin: absoluteRange.min,
                currentMax: absoluteRange.max + 0.01 * (absoluteRange.max - absoluteRange.min)
            };
        }
    }

    // Models represent numeric or date parametric fields. The currentMin and currentMax attributes are the current range
    // displayed on the numeric widget.
    return FindBaseCollection.extend({
        url: function() {
            return '../api/public/fields/parametric-' + this.dataType;
        },

        initialize: function(models, options) {
            this.dataType = options.dataType;
        },

        model: FindBaseCollection.Model.extend({
            defaults: _.extend({
                min: 0,
                max: 0,
                totalValues: 0
            }, defaultCurrentRangeAttributes({min: 0, max: 0})),

            parse: function(response) {
                return _.defaults(defaultCurrentRangeAttributes(response), response);
            },

            getDefaultCurrentRange: function() {
                return defaultCurrentRangeAttributes(this.pick('min', 'max'));
            }
        })
    });

});