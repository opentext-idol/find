/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/base-page
 */
define([
    '../../../backbone/backbone'
], function(Backbone) {

    /**
     * @name module:js-whatever/js/base-page.BasePage
     * @desc Abstract base class representing a page. All application pages should extend from this or implement the
     * same methods.
     * Base pages will start hidden.
     * @constructor
     * @extends Backbone.View
     * @abstract
     */
    return Backbone.View.extend(/** @lends module:js-whatever/js/base-page.BasePage.prototype */{

        attributes: {
            style: 'display:none;'
        },

        /**
         * @desc Shows the page and calls the {@link module:js-whatever/js/base-page.BasePage#update|update} method
         */
        show: function() {
            this.$el.show();

            // for Selenium testing
            this.$el.attr('data-pagename', this.pageName);

            this.update();
        },

        /**
         * @desc Hides the page
         */
        hide: function() {
            this.$el.hide();
        },

        isVisible: function() {
            return this.$el.is(':visible');
        },

        /**
         * @desc Called when showing a page. The default implementation is a no-op.
         * @abstract
         * @method
         */
        update: $.noop,

        /**
         * @desc Called when navigating to a page.  If a page has state which can be represented by a route, this route
         * should be returned. The default implementation is a no-op.
         * @abstract
         * @method
         */
        getSelectedRoute: $.noop

    });

});