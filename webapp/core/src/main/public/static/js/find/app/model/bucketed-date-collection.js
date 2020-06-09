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
    'moment',
    'find/app/model/find-base-collection'
], function(_, moment, FindBaseCollection) {
    'use strict';

    const URL_ROOT = 'api/public/parametric/date/buckets';

    const Model = FindBaseCollection.Model.extend({
        urlRoot: URL_ROOT,

        url: function() {
            const base = this.collection
                ? this.collection.url()
                : URL_ROOT;
            // Double encode since Spring doesn't like %2F in URLs
            return this.isNew()
                ? base
                : base.replace(/[^\/]$/, '$&/') + encodeURIComponent(encodeURIComponent(this.id));
        },

        set: function() {
            FindBaseCollection.Model.prototype.set.apply(this, arguments);
        },

        parse: function(response) {
            return _.extend(response, {
                min: moment(response.min),
                max: moment(response.max),
                values: _.map(response.values, function(value) {
                    return _.extend(value, {
                        min: moment(value.min),
                        max: moment(value.max)
                    });
                })
            });
        },

        defaults: {
            values: []
        }
    });

    return FindBaseCollection.extend({
        url: URL_ROOT,
        model: Model
    }, {
        Model: Model
    });
});
