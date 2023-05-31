/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
