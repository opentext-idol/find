/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory',
    'jquery',
    'underscore'
], function(backboneMockFactory, $, _) {
    'use strict';

    const modelSyncSpy = jasmine.createSpy('sync');
    const modelFetchSpy = jasmine.createSpy('fetch');
    const Model = backboneMockFactory.getModel([], {sync: modelSyncSpy, fetch: modelFetchSpy});
    Model.syncPromises = [];
    Model.fetchPromises = [];

    modelSyncSpy.and.callFake(function() {
        const promise = _.extend({
            abort: jasmine.createSpy('abort')
        }, $.Deferred());

        Model.syncPromises.push(promise);
        return promise;
    });

    modelFetchSpy.and.callFake(function() {
        const promise = $.Deferred();
        promise.abort = jasmine.createSpy('abort');
        Model.fetchPromises.push(promise);
        return promise;
    });

    const originalReset = Model.reset;

    Model.reset = function() {
        originalReset();
        modelSyncSpy.calls.reset();
        modelFetchSpy.calls.reset();
        Model.syncPromises = [];
        Model.fetchPromises = [];
    };

    const Collection = backboneMockFactory.getCollection(['sync'], {model: Model});
    Collection.Model = Model;

    return Collection;
});
