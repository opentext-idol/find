/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory'
], function (mockFactory) {

    return mockFactory.getView([], {
        initialize: function (options) {
            this.actionButtonCallback = options.actionButtonCallback;
        }
    });

});
