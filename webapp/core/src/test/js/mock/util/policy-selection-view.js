/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory'
], function (mockFactory) {

    const applyPolicySpy = jasmine.createSpy('applyPolicy', function (successCallback) {
        successCallback()
    });

    const View = mockFactory.getView([], {
        applyPolicy: applyPolicySpy
    });

    // mockFactory seems to have no way to configure a stub with default implementation and reset
    const reset = View.reset;
    View.reset = function () {
        reset();
        applyPolicySpy.calls.reset();
    }

    return View;

});
