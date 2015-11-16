/*
 * Copyright 2013-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module js-whatever/js/tab-view
 */
define([
    '../../../backbone/backbone',
    'jqueryui'
], function(Backbone) {

    /**
     * @typedef TabData
     * @property {string} href The id of the tab
     * @property {label} label The display name of the tab
     * @property {Backbone.View} view The view representing the tab
     */
    /**
     * @typedef TabViewOptions
     * @property {Array<TabData>} tabs The tabs that comprise the view
     * @property {Backbone.Router} router The router to use for navigation
     * @property {module:js-whatever/js/vent-constructor.Vent} vent The vent used for navigation
     * @property {string} [appPrefix=page] The initial part of routes for the application
     * @property {string} routePrefix The prefix for routes leading up to the tabView
     */
    /**
     * @name module:js-whatever/js/tab-view.TabView
     * @desc Wrapper around the jQuery UI tabs widget
     * @constructor
     * @param {TabViewOptions} options
     * @extends Backbone.View
     */
    return Backbone.View.extend(/** @lends module:js-whatever/js/tab-view.TabView.prototype */{

        initialize: function(options) {
            _.bindAll(this, 'showTab', 'selectTab');

            var routePrefix = options.routePrefix;

            this.tabs = options.tabs;
            this.router = options.router;
            this.vent = options.vent;
            this.appPrefix = options.appPrefix || 'page';
            this.routePrefix = routePrefix;

            this.router.on('route:' + _.last(routePrefix.split('/')), this.selectTab);

            this.$el.tabs({
                activate: this.showTab,
                active: -1
            });

            // this implicitly calls showTab, and will set this.selectedId
            this.$el.tabs('option', 'active', 0);
        },

        /**
         * @desc Shows a tab in response to an event from the jQuery plugin.
         * @param {object} e jQuery event object
         * @param {object} ui jQuery UI tabs data
         */
        showTab: function(e, ui) {
            var id = ui.newPanel.attr('id');

            // if the tab has just been created, we don't want to update the routes, otherwise when you
            // refresh the page you'll get spurious 'back' entries for the first tab in the tab view.
            var tabNewlyCreated = !this.selectedId;
            this.selectedId = id;

            var tab = this.find(id);
            if (!tabNewlyCreated && (!tab.view || !_.result(tab.view, 'suppressTabHistory'))) {
                var newRoute = this.appPrefix + '/' + this.getSelectedRoute(id);

                var isSubTabRoute = newRoute.indexOf(Backbone.history.fragment.replace(new RegExp('^.*?' + this.appPrefix), this.appPrefix ) + '/') === 0;

                // if the new route is a subtab of the old route, we should replace the old route
                // e.g. when refreshing a page on #page/performance/performanceStatistics/statstab-ACI we
                // briefly go to #page/performance/performanceStatistics.
                this.vent.navigate(newRoute, {
                    trigger: false,
                    replace: isSubTabRoute
                });
            }
        },

        /**
         * @desc Activates a tab by id
         * @param {string} [reqId=this.selectedId] The id of the tab
         */
        selectTab: function(reqId) {
            var id = reqId || this.selectedId;

            this.$el.tabs('option', 'active', this.indexOf(id));
            this.selectedId = id;
        },

        /**
         * @desc Finds the tab data for a given id
         * @param {String} id The id of the tab
         * @returns {TabData} The tab data for the id
         */
        find: function(id) {
            return _.findWhere(this.tabs, {href: id});
        },

        /**
         * @desc Obtains the index of the tab with given id
         * @param {String} id The id of the tab
         * @returns {number} The tab data for the id
         */
        indexOf: function(id) {
            return _.indexOf(this.tabs, this.find(id));
        },

        /**
         * @desc Gets the selected route for the tab view
         * @param [id=this.selectedId] The id of the tab to use
         * @returns {string}
         */
        getSelectedRoute: function(id) {
            id = id || this.selectedId;
            return [this.routePrefix, id].join('/');
        }

    });

});
