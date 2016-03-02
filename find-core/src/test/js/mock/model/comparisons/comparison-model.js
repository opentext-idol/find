/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-testing/backbone-mock-factory'
], function(backboneMockFactory) {

    var ComparisonModel = backboneMockFactory.getModel(['save']);

    ComparisonModel.fromModels = function() {
        var comparisonModel = new ComparisonModel();

        // Model save method return values are tracked in this array
        comparisonModel.mockXhrs = [];

        comparisonModel.save.and.callFake(function() {
            var xhr = jasmine.createSpyObj('save', ['abort']);
            comparisonModel.mockXhrs.push(xhr);
            return xhr;
        });

        return comparisonModel;
    };

    return ComparisonModel;

});