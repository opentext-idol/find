/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'backbone'
], function(_, Backbone) {
    'use strict';

    function createFetch(prototype) {
        // Fetch tracks in-flight requests and cancels them when a new one is run
        return function(options) {
            if(this.currentRequest) {
                this.currentRequest.abort();
            }

            this.fetching = true;
            this.error = false;

            const error = options.error;
            const success = options.success;

            this.currentRequest = prototype.fetch.call(this, _.extend(options || {}, {
                reset: _.isUndefined(options.reset) || options.reset,
                error: _.bind(function() {
                    this.currentRequest = null;
                    this.error = true;
                    this.fetching = false;

                    if(error) {
                        error.apply(options, arguments);
                    }
                }, this),
                success: _.bind(function() {
                    this.currentRequest = null;
                    this.error = false;
                    this.fetching = false;

                    if(success) {
                        success.apply(options, arguments);
                    }
                }, this)
            }));

            return this.currentRequest;
        };
    }

    const baseProperties = {
        currentRequest: null,
        error: false,
        fetching: false,

        sync: function(method, model, options) {
            // Force "traditional" serialization of query parameters, e.g.
            // index=foo&index=bar, for IOD multi-index support.
            return Backbone.sync.call(this, method, model, _.extend(options, {
                traditional: true
            }));
        }
    };

    const Model = Backbone.Model.extend(_.extend({
        fetch: createFetch(Backbone.Model.prototype)
    }, baseProperties));

    return Backbone.Collection.extend(_.extend({
        fetch: createFetch(Backbone.Collection.prototype),
        model: Model
    }, baseProperties), {
        Model: Model
    });
});
