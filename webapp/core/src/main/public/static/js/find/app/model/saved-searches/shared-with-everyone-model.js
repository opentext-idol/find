/*
 * Copyright 2017 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone'
], function(_, Backbone) {
    'use strict';

    return Backbone.Model.extend({

        initialize: function(models, options) {
            this.searchId = options.searchId;
        },

        url: function() {
            return 'api/public/search/shared-searches/everyone/permissions/' + this.searchId
        },

        isNew: function() {
            return !this.get('searchId');
        },

        parse: function(response) {
            return {
                searchId: response.id.searchId
            }
        }
    });
});
