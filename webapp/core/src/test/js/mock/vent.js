/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore'
], function(Backbone, _) {
    'use strict';

    return _.extend(jasmine.createSpyObj('vent', ['navigate', 'navigateToDetailRoute']), Backbone.Events);
});
