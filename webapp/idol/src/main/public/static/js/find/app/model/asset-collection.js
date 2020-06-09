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

    return Backbone.Collection.extend({
        comparator: 'id',

        url: function() {
            return 'api/admin/customization/assets/' + this.type
        },

        initialize: function(models, options) {
            this.type = options.type;
        },

        parse: function(data) {
            return _.map(data, function(datum) {
                return {id: datum};
            });
        },

        model: Backbone.Model.extend({
            defaults: {
                deletable: true
            }
        })
    });
});
