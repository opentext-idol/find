/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore'
], function(Backbone, _) {

    function createFetch(prototype) {
        // Fetch tracks in-flight requests and cancels them when a new one is run
        return function(options) {
            if (this.currentRequest) {
                this.currentRequest.abort();
            }

            this.fetching = true;
            this.error = false;

            var error = options.error;
            var success = options.success;

            this.currentRequest = prototype.fetch.call(this, _.extend(options || {}, {
                reset: _.isUndefined(options.reset) ? true : options.reset,
                error: _.bind(function() {
                    this.currentRequest = null;
                    this.error = true;
                    this.fetching = false;

                    if (error) {
                        error.apply(options, arguments);
                    }
                }, this),
                success: _.bind(function() {
                    this.currentRequest = null;
                    this.error = false;
                    this.fetching = false;

                    if (success) {
                        success.apply(options, arguments);
                    }
                }, this)
            }));

            return this.currentRequest;
        };
    }

    var baseProperties = {
        currentRequest: null,
        error: false,
        fetching: false,

        sync: function(method, model, options) {
            // Force "traditional" serialization of query parameters, e.g. index=foo&index=bar, for IOD multi-index support.
            return Backbone.sync.call(this, method, model, _.extend(options, {
                traditional: true
            }));
        }
    };

    var Model = Backbone.Model.extend(_.extend({
        fetch: createFetch(Backbone.Model.prototype)
    }, baseProperties));

    return Backbone.Collection.extend(_.extend({
        fetch: createFetch(Backbone.Collection.prototype),
        model: Model
    }, baseProperties), {
        Model: Model
    });

});
