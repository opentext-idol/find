/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module settings/js/models/security-types
 */
define([
    'backbone'
], function(Backbone) {

    /**
     * @typedef SecurityTypesOptions
     * @property {string} url The url to fetch the security types from
     */
    /**
     * @name module:settings/js/models/security-types.SecurityTypes
     * @desc Model for fetching security types
     * @param {SecurityTypesAttributes} attributes Initial attributes
     * @param {SecurityTypesOptions} options Options for the model
     * @constructor
     * @extends Backbone.Model
     */
    return Backbone.Model.extend(/** @lends module:settings/js/models/security-types.SecurityTypes.prototype */ {
        initialize: function(attributes, options) {
            this.url = options.url;
        },

        /**
         * @desc Fetches the security types. If already fetching, the previous request is aborted
         * @param {object} options Backbone fetch options
         */
        fetch: function(options) {
            this.xhr && this.xhr.abort();
            options = options || {};
            var originalComplete = options.complete;

            options.complete = _.bind(function() {
                delete this.xhr;
                originalComplete && originalComplete();
            }, this);

            this.xhr = Backbone.Model.prototype.fetch.call(this, options);
        },

        /**
         * @typedef SecurityTypesAttributes
         * @property {Array<String>} securityTypes An array of strings representing security types
         */
        /**
         * Parse the list of security types to remove any type named "default"
         * @param {SecurityTypesAttributes} response
         * @returns {SecurityTypesAttributes} response
         */
        parse: function(response) {
            response.securityTypes = response.securityTypes && _.without(response.securityTypes, 'default');
            return response;
        }
    });

});