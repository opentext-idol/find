/*
 * (c) Copyright 2014-2017 Micro Focus or one of its affiliates.
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
    'jquery',
    'backbone'
], function($, Backbone) {
    'use strict';

    return Backbone.Router.extend({
        // Abstract function which can be overridden in the children.
        parseEncodedDatabases: undefined,

        routes: {
            'search(/databases/:searchDatabase)/query(/:text)': 'search',
            ':page': 'page',
            'search/splash': 'searchSplash'
        },

        navigate: function() {
            $('.modal').not('.undismissable-modal').modal('hide');

            return Backbone.Router.prototype.navigate.apply(this, arguments);
        },

        search: function(optionalEncodedDatabases, optionalEncodedQuery) {
            let query = optionalEncodedQuery;
            if (query) {
                try {
                    query = decodeURIComponent(optionalEncodedQuery);
                }
                catch(e) {
                    // we'll proceed without a query
                }
            }

            let databases = undefined;

            if (this.parseEncodedDatabases && optionalEncodedDatabases) {
                databases = this.parseEncodedDatabases(optionalEncodedDatabases);
            }

            this.trigger('route:page', 'search', query, databases);
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
