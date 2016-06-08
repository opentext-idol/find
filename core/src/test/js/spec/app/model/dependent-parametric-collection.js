/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/dependent-parametric-collection',
    'resources/sunburst_test_data/dependent-parametric-collection-input-data-1',
    'resources/sunburst_test_data/dependent-parametric-collection-output-data-1',
    'resources/sunburst_test_data/dependent-parametric-collection-input-data-2',
    'resources/sunburst_test_data/dependent-parametric-collection-output-data-2'
], function (DependentParametricCollection, inputData1, outputData1, inputData2, outputData2) {

    describe('DependentParametricCollection', function () {
        describe('parse method', function () {
            it('should split values into segments with correct padding', function () {
                var output = DependentParametricCollection.prototype.parse(inputData1);
                var expected = outputData1;

                expect(output).toEqual(expected);
            });

            it('should split values and children into segments with correct padding', function () {
                var output = DependentParametricCollection.prototype.parse(inputData2);
                var expected = outputData2;

                expect(output).toEqual(expected);
            });

            it('returns an empty array when given an empty array', function () {
                expect(DependentParametricCollection.prototype.parse([])).toEqual([]);
            });
        });
    });

});
