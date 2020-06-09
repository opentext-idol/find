/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'jquery',
    'js-testing/backbone-mock-factory'
], function(_, $, backboneMockFactory) {
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
