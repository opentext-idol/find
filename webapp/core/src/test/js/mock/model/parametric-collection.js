/*
 * Copyright 2016 Open Text.
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

    const collectionFetchSpy = jasmine.createSpy('fetch');
    const Collection = backboneMockFactory.getCollection([], {
        fetch: collectionFetchSpy,
        fetchFromQueryModel: function () {}
    });
    Collection.fetchPromises = [];

    collectionFetchSpy.and.callFake(function() {
        const promise = $.Deferred();

        Collection.fetchPromises.push(promise);
        return promise;
    });

    const originalReset = Collection.reset;

    Collection.reset = function() {
        originalReset();
        collectionFetchSpy.calls.reset();
        Collection.fetchPromises = [];
    };

    return Collection;
});
