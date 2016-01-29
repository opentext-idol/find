/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    var Router = Backbone.Router.extend({
        routes: {
            'find/search(/:text(/*refinements))': 'search',
            'find/:page': 'find'
        },

        navigate: function() {
            $('.modal').not('.undismissable-modal').modal('hide');

            return Backbone.Router.prototype.navigate.apply(this, arguments);
        },

        search: function() {
            this.trigger('route:find', 'search');
        }
    });

    return new Router();

});
