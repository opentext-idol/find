/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory',
    'jquery',
    'underscore'
], function(backboneMockFactory, $, _) {
    'use strict';

    var modelSyncSpy = jasmine.createSpy('sync');
    var modelFetchSpy = jasmine.createSpy('fetch');
    var Model = backboneMockFactory.getModel([], {sync: modelSyncSpy, fetch: modelFetchSpy});
    Model.syncPromises = [];
    Model.fetchPromises = [];

    modelSyncSpy.and.callFake(function() {
        var promise = _.extend({
            abort: jasmine.createSpy('abort')
        }, $.Deferred());

        Model.syncPromises.push(promise);
        return promise;
    });

    modelFetchSpy.and.callFake(function() {
        var promise = $.Deferred();

        Model.fetchPromises.push(promise);
        return promise;
    });

    var originalReset = Model.reset;

    Model.reset = function() {
        originalReset();
        modelSyncSpy.calls.reset();
        modelFetchSpy.calls.reset();
        Model.syncPromises = [];
        Model.fetchPromises = [];
    };

    var Collection = backboneMockFactory.getCollection(['sync'], {model: Model});
    Collection.Model = Model;

    return Collection;
});
