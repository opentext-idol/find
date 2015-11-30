/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/tab-page
 */
define([
    'js-whatever/js/base-page',
    'js-whatever/js/lazy-tab-view',
    'text!js-whatever/templates/tab-page.html',
    'underscore'
], function(BasePage, LazyTabView, template, _) {

    /**
     * @name module:js-whatever/js/tab-page.TabPage
     * @desc Wrapper around {@link module:js-whatever/js/lazy-tab-view.LazyTabView|LazyTabView} which allows it to be
     * used as a page. The constructor calls initializeTabs, followed by filterTabs, then constructs an instance
     * of each tab
     * @constructor
     * @extends module:js-whatever/js/base-page.BasePage
     * @abstract
     */
    return BasePage.extend(/** @lends module:js-whatever/js/tab-page.TabPage.prototype */{

        /**
         * @desc The base route of your application
         * @type {string}
         * @default page
         */
        appPrefix: 'page',

        /**
         * @desc Instance of Vent used for navigation
         * @type {module:js-whatever/js/vent-constructor.Vent}
         * @abstract
         */
        vent: null, // You need to set this

        /**
         * @type {Backbone.Router}
         * @abstract
         */
        router: null, // You need to set this

        /**
         * @typedef TabPageTabData
         * @property {string} href The id of the tab
         * @property {label} label The display name of the tab
         * @property {function} constructor A Backbone.View constructor function
         * @property {object} [constructorOptions] Options passed to the constructor
         */
        /**
         * @desc Tabs to be rendered. Initialize in initializeTabs
         * @type {Array<TabPageTabData>}
         * @abstract
         */
        tabs: [], //You need to set this

        /**
         * @desc String used in route construction. Set this to the name of the page
         * @type {string}
         * @default overrideMe
         * @abstract
         */
        routePrefix: "overrideMe", //You need to set this

        /**
         * @desc Template for page
         */
        template: _.template(template),

        initialize: function() {
            this.initializeTabs();

            this.filterTabs();

            _.each(this.tabs, function(tab) {
                if (tab.constructor) {
                    tab.view = new tab.constructor(tab.constructorOptions);
                }
            });
        },

        /**
         * @desc Override to initialize this.tabs
         * @type {function}
         */
        initializeTabs: $.noop, //Override this and create this.tabs

        /**
         * @desc Override to filter this.tabs
         * @type {function}
         */
        filterTabs: $.noop, // Override this to filter this.tabs before creating page

        /**
         * @desc Renders the view, creating an embedded {@link module:js-whatever/js/tab-view.TabView}
         */
        render: function() {
            this.$el.html(this.template({
                tabs: this.tabs
            }));

            this.tabView = new LazyTabView({
                router: this.router,
                vent: this.vent,
                appPrefix: this.appPrefix,
                routePrefix: this.routePrefix,
                el: this.el,
                tabs: this.tabs
            });
        },

        /**
         * @desc Selects the currently selected tab to update navigation
         */
        update: function() {
            this.tabView.selectTab();
        },

        /**
         * @desc Returns the selected route for the embedded tab view
         * @returns {string}
         */
        getSelectedRoute: function() {
            return this.tabView ? this.tabView.getSelectedRoute() : [this.routePrefix, this.tabs[0].href].join('/');
        }

    });

});
