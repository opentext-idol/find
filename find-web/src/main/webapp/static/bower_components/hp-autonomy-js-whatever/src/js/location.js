/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/location
 * @desc Wrapper around window.location, allowing for easier testing
 */
define(function() {

    var host = window.location.host;
    var hostname = window.location.hostname;
    var pathname = window.location.pathname;
    var port = window.location.port;
    var protocol = window.location.protocol;
    var search = window.location.search;

    //noinspection UnnecessaryLocalVariableJS
    /**
     * @alias module:js-whatever/js/location
     */
    var location = {

        /**
         * @returns {string} window.location.host
         */
        host: function() {
            return host;
        },

        /**
         * @returns {string} window.location.hostname
         */
        hostname: function() {
            return hostname;
        },

        /**
         * @returns {string} window.location.pathname
         */
        pathname: function() {
            return pathname;
        },

        /**
         * @returns {string} window.location.port
         */
        port: function() {
            return port;
        },

        /**
         * @returns {string} window.location.protocol
         */
        protocol: function() {
            return protocol;
        },

        /**
         * @returns {string} window.location.search
         */
        search: function() {
            return search;
        }
    };

    return location;
});