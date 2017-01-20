/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {
    'use strict';

    return Backbone.View.extend({

        initialize: function(options) {
            this.html = options.html;
        },

        render: function() {
            this.$el.html(this.html);
        }
    });

});