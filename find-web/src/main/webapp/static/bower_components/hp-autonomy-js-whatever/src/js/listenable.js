/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/listenable
 */
define([
    '../../../jquery/jquery',
    'underscore'
], function() {

    var DATA_KEY = 'listenable';

    /**
     * @alias module:js-whatever/js/listenable
     * @desc Allows jQuery wrapped objects to be used with backbone style events. e.g. listenTo and stopListening
     * @param {string|jQuery} el The element to be wrapped
     * @returns {{on: Function, off: Function}} A object which can be consumed by Backbone's listenTo and stopListening
     * methods
     */
    var listenable = function(el) {
        var $el = $(el);

        var me = {
            on: function(event, fn) {
                var id = me._listenerId || (me._listenerId = _.uniqueId('l'));
                var data = $el.data(DATA_KEY) || {};
                var pairs = data[id] || [];
                pairs.push({ event: event, fn: fn });

                $el.on(event, fn);

                data[id] = pairs;
                $el.data(DATA_KEY, data);
            },

            off: function(event, fn) {
                var data = $el.data(DATA_KEY);
                if (!data) {
                    return;
                }

                var pairs = data[me._listenerId];
                if (!pairs) {
                    return;
                }

                if (!event && !fn) {
                    _.each(pairs, function(data) {
                        $el.off(data.event, data.fn);
                    });
                }
                else {
                    for (var ii = pairs.length - 1; ii >= 0; ii--) {
                        if (pairs[ii].event === event && (!fn || pairs[ii].fn === fn)) {
                            pairs.splice(ii, 1);
                            $el.off(event, fn);
                        }
                    }
                }

                data[me._listenerId] = pairs;
            }
        };

        return me;
    };

    return listenable;
});