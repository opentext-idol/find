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
    'find/app/model/dependent-parametric-collection',
    'resources/sunburst_test_data/dependent-parametric-collection-input-data-1',
    'resources/sunburst_test_data/dependent-parametric-collection-output-data-1',
    'resources/sunburst_test_data/dependent-parametric-collection-input-data-2',
    'resources/sunburst_test_data/dependent-parametric-collection-output-data-2'
], function(DependentParametricCollection, inputData1, outputData1, inputData2, outputData2) {
    'use strict';

    describe('DependentParametricCollection', function () {
        describe('parse method', function () {
            it('should split values into segments with correct padding', function () {
                const output = DependentParametricCollection.prototype.parse(inputData1);
                expect(output).toEqual(outputData1);
            });

            it('should split values and children into segments with correct padding', function () {
                const output = DependentParametricCollection.prototype.parse(inputData2);
                expect(output).toEqual(outputData2);
            });

            it('returns an empty array when given an empty array', function () {
                expect(DependentParametricCollection.prototype.parse([])).toEqual([]);
            });
        });
    });
});
