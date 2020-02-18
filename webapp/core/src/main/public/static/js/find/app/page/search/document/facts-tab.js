/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone'
], function(_, Backbone) {
    'use strict';

    return Backbone.View.extend({
        initialize: function (options) {
            this.documentRenderer = options.documentRenderer
        },

        render: function () {
            this.$el.html(this.documentRenderer.renderDocumentFacts(this.model));
        }
    });

});
