/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory',
    'test-util/promise-spy',
], function(backboneMockFactory, promiseSpy) {

    const Model = backboneMockFactory.getModel([], {
        fetch: promiseSpy('fetch'),
        isMedia: _.constant(false)
    });

    Model.reset = function() {
        Model.instances = [];
        Model.prototype.fetch.reset();
    };

    return Model;

});
