/*
 * Copyright 2014-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'jquery',
    'underscore',
    'backbone',
    'qs',
], function($, _, Backbone, qs) {
    'use strict';

    return Backbone.Router.extend({
        // Abstract function which can be overridden in the children.
        parseEncodedDatabases: undefined,

        routes: {
            'search(/databases/:searchDatabase)/query(/:text)(?*querystring)': 'search',
            ':page': 'page',
            'search/splash': 'searchSplash'
        },

        navigate: function() {
            $('.modal').not('.undismissable-modal').modal('hide');

            return Backbone.Router.prototype.navigate.apply(this, arguments);
        },

        search: function(optionalEncodedDatabases, optionalEncodedQuery, querystring) {
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

            let options = {};
            if (querystring) {
                _.each(qs.parse(querystring), (value, name) => {
                    let group;
                    let propName;
                    const i = name.search(/:/);
                    if (i === -1) {
                        group = options;
                        propName = name;
                    } else {
                        const groupName = name.slice(0, i);
                        propName = name.slice(i + 1);
                        group = options[groupName] = options[groupName] || {};
                    }

                    group[propName] = (group[propName] || [])
                        .concat(Array.isArray(value) ? value : [value]);
                });
            }

            this.trigger('route:page', 'search', query, databases, options);
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
