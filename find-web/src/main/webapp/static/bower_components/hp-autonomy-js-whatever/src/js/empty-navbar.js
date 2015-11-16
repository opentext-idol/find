/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/empty-navbar
 */
define([
    '../../../backbone/backbone',
    'text!js-whatever/templates/empty-navbar.html'
], function(Backbone, template) {

    var defaultOptions = {
        showLogout: true
    };

    /**
     * @typedef EmptyNavbarStrings
     * @type {object}
     * @property {string} appName The name of the application
     * @property {string} [logout] The label for the logout link. Must be defined if showLogout is true
     */
    /**
     * @typedef EmptyNavbarOptions
     * @type {object}
     * @property {boolean} [showLogout=true] Set to true if a logout button should be displayed
     * @property {string} [logoutUri] Href for the logout button. Must be defined if showLogout is true
     * @property {EmptyNavbarStrings} strings
     */
    /**
     * @name module:js-whatever/js/empty-navbar.EmptyNavbar
     * @desc Renders an empty top navbar that may contain a logout button
     * @constructor
     * @param {EmptyNavbarOptions} [options={showLogout: true}]
     * @extends Backbone.View
     */
    return Backbone.View.extend(/** @lends module:js-whatever/js/empty-navbar.EmptyNavbar.prototype */{

        template: _.template(template),

        initialize: function (options) {
            this.options = _.extend({}, defaultOptions, options || {});
        },

        /**
         * @desc Renders the view with the provided {@link EmptyNavbarOptions}
         */
        render: function() {
            this.$el.html(this.template(this.options));
        }

    });

});