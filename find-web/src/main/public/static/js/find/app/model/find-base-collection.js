/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore'
], function(Backbone, _) {

    return Backbone.Collection.extend({

        /**
         * Fetch tracks in-flight requests and cancels them when a new one is run
         * @param options
         * @returns {*|null}
         */
        fetch: function(options) {
            if (this.currentRequest) {
                this.currentRequest.abort();
            }

            var success = options.success;

            this.currentRequest = Backbone.Collection.prototype.fetch.call(this, _.extend(options || {}, {
                reset: _.isUndefined(options.reset) ? true : options.reset,
                success: _.bind(function() {
                    this.currentRequest = null;

                    if (success) {
                        success.apply(options, arguments);
                    }
                }, this)
            }));

            return this.currentRequest;
        },

        /**
         * Sync uses "traditional" options serialization
         * @param method
         * @param model
         * @param options
         * @returns {*}
         */
        sync: function(method, model, options) {
            options = options || {};
            options.traditional = true; // Force "traditional" serialization of query parameters, e.g. index=foo&index=bar, for IOD multi-index support.

            return Backbone.Collection.prototype.sync.call(this, method, model, options);
        }
    });

});
