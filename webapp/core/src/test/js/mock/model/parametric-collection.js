/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'js-testing/backbone-mock-factory'
], function($, backboneMockFactory) {

    const collectionFetchSpy = jasmine.createSpy('fetch');
    const Collection = backboneMockFactory.getCollection([], {fetch: collectionFetchSpy});
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