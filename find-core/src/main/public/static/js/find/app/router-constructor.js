/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {
    return Backbone.Router.extend({

        routes: {
            'find/search/query(/:text)': 'search',
            'find/:page': 'find',
            'find/search/splash': 'searchSplash'
        },

        navigate: function() {
            $('.modal').not('.undismissable-modal').modal('hide');

            return Backbone.Router.prototype.navigate.apply(this, arguments);
        },

        search: function() {
            this.trigger('route:find', 'search');
        },

        documentDetail: function() {
            this.trigger('route:find', 'search');
        },

        searchSplash: function() {
            this.trigger('route:find', 'search');
        }
    });
});
