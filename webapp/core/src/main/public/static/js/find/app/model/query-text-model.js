/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {
    'use strict';

    return Backbone.Model.extend({
        defaults: {
            inputText: ''
        }
    });
});
