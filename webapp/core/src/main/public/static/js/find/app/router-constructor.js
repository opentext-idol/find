/*
 * Copyright 2014-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone'
], function($, Backbone) {
    'use strict';

    return Backbone.Router.extend({
        routes: {
            'search/query(/:text)': 'search',
            ':page': 'page',
            'search/splash': 'searchSplash'
        },

        navigate: function() {
            $('.modal').not('.undismissable-modal').modal('hide');

            return Backbone.Router.prototype.navigate.apply(this, arguments);
        },

        search: function(optionalEncodedQuery) {
            let query = optionalEncodedQuery;
            if (query) {
                try {
                    query = decodeURIComponent(optionalEncodedQuery);
                }
                catch(e) {
                    // we'll proceed without a query
                }
            }

            this.trigger('route:page', 'search', query);
        },

        documentDetail: function() {
            this.trigger('route:page', 'search');
        },

        suggest: function() {
            this.trigger('route:page', 'search');
        },

        searchSplash: function() {
            this.trigger('route:page', 'search');
        },

        dashboards: function(dashboardName) {
            this.trigger('route:page', 'dashboards/' + encodeURIComponent(dashboardName));
        },

        savedSearch: function() {
            this.trigger('route:page', 'search');
        }
    });
});
