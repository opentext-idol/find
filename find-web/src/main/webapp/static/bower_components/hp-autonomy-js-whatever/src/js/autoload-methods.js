/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/autoload-methods
 * @desc Methods for Backbone models and collections which fetch themselves upon instantiation
 * @abstract
 */
define([
    '../../../jquery/jquery',
    'underscore'
],function($, _) {

    return {
        /**
         * @desc Determines if the model should fetch when instantiated
         * @type {boolean}
         */
        autoload: true,
        loaded: false,

        /**
         * @typedef {Object} AutoloadOptions
         * @property {boolean} autoload If the model should autoload
         * @property {boolean} loaded If the model has already loaded
         */
        /**
         * @desc backbone
         * @param {Object} attributes initial model attributes
         * @param {AutoloadOptions} options
         */
        initialize: function(attributes, options) {
            // We need our .loaded flag to be set true in a change listener since it's fired before the fetch()
            // completion handler fires; prevents loss of the first change event if we call this.onLoad from within
            // this.onLoad e.g. in #1018992. We still keep the fetch completion handler since it's not explicitly
            // mentioned in docs whether the handler or change() events fire first () so it might change in future.
            options = options || {};

            var onLoaded = _.bind(function() {
                this.off(this.eventName, onLoaded);
                this.loaded = true;
            }, this);

            if (options.ports) {
                this.ports = options.ports;
            }

            if (options.autoload !== undefined) {
                this.autoload = options.autoload
            }

            if (options.loaded !== undefined) {
                this.loaded = options.loaded
            }

            this.on(this.eventName, onLoaded);

            if (this.autoload) {
                this.fetch({
                    success: onLoaded
                });
            }
        },

        /**
         * @desc the url that data should be fetched from
         * @abstract
         * @method
         */
        url: $.noop,

        /**
         * @desc Register a callback to be called when there is data available.  If the model has loaded, the callback
         * will be called immediately.  Otherwise the callback will be called when the model has finished loading.
         * @param callback The callback to be called
         * @param ctx The context for callback
         */
        onLoad: function(callback, ctx) {
            if (ctx) {
                callback = _.bind(callback, ctx);
            }

            if (this.loaded) {
                callback(this);
            }
            else {
                this.on(this.eventName, function listener() {
                    this.off(this.eventName, listener);
                    callback(this);
                });
            }
        }
    }
});