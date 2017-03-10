/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory'
], function(backboneMockFactory) {
    'use strict';

    return backboneMockFactory.getCollection(['fetch', 'fetchDependentFields']);
});
