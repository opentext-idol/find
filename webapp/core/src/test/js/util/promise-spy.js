/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery'
], function($) {

    return function(name) {
        const spy = jasmine.createSpy(name);

        spy.reset = function() {
            spy.promises = [];
            spy.calls.reset();
        };

        spy.and.callFake(function() {
            const deferred = $.Deferred();
            spy.promises.push(deferred);
            return deferred;
        });

        spy.reset();
        return spy;
    };

});
