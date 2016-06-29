/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory',
    'jquery',
    'underscore'
], function(backboneMockFactory, $, _) {

    var modelSyncSpy = jasmine.createSpy('sync');
    var Model = backboneMockFactory.getModel([], {sync: modelSyncSpy});
    Model.syncPromises = [];

    modelSyncSpy.and.callFake(function() {
        var promise = _.extend({
            abort: jasmine.createSpy('abort')
        }, $.Deferred());

        Model.syncPromises.push(promise);
        return promise;
    });

    var originalReset = Model.reset;

    Model.reset = function() {
        originalReset();
        modelSyncSpy.calls.reset();
        Model.syncPromises = [];
    };

    var Collection = backboneMockFactory.getCollection(['sync'], {model: Model});
    Collection.Model = Model;

    return Collection;

});