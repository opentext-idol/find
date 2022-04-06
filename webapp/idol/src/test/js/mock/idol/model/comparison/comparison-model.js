/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'js-testing/backbone-mock-factory'
], function(backboneMockFactory) {
    'use strict';

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
