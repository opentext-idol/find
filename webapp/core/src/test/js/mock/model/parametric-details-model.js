/*
 * Copyright 2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'js-testing/backbone-mock-factory'
], function($, backboneMockFactory) {
    'use strict';

    const modelFetchSpy = jasmine.createSpy('fetch');
    const Model = backboneMockFactory.getModel([], {fetch: modelFetchSpy});
    Model.fetchPromises = [];

    modelFetchSpy.and.callFake(function() {
        const promise = $.Deferred();

        Model.fetchPromises.push(promise);
        return promise;
    });

    const originalReset = Model.reset;

    Model.reset = function() {
        originalReset();
        modelFetchSpy.calls.reset();
        Model.fetchPromises = [];
    };

    return Model;
});