/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/util/search-data-util',
    'parametric-refinement/to-field-text-node'
], function(searchDataUtil, toFieldTextNode) {

    describe('Search Data Utils', function() {

        describe('makeQueryText', function() {
            it('called with inputText and empty relatedConcepts returns the inputText', function() {
                expect(searchDataUtil.makeQueryText('"chlorine fluoride"',[])).toEqual('"chlorine fluoride"');
            });

            it('called with no inputText and some relatedConcepts returns the empty string', function() {
                expect(searchDataUtil.makeQueryText(undefined, [['activated carbon'], ['mercury', 'bromine', 'ammonia']]))
                    .toEqual('')
            });

            it('called with inputText and some relatedConcepts returns the inputText wrapped in brackets ' +
                'followed by each word in the related concept array separated by spaces', function() {
                expect(searchDataUtil.makeQueryText('"chlorine fluoride"', [['activated carbon'], ['mercury', 'bromine', 'ammonia']]))
                    .toEqual('("chlorine fluoride") "activated carbon" "mercury" "bromine" "ammonia"');
            });
        });

        describe('buildIndexes', function() {
            it('called with indexes without domains returns an array of the index names as strings', function() {
                expect(searchDataUtil.buildIndexes([
                    {domain: null, name: 'smallData'},
                    {domain: null, name: 'corruptedData'}
                ])).toEqual(['smallData', 'corruptedData'])
            });

            it('called with domain qualified domains returns an array of the indexes represented as strings of the form "(domain):(name)"', function() {
                expect(searchDataUtil.buildIndexes([
                    {domain: 'earth', name: 'smallData'},
                    {domain: 'mars', name: 'corruptedData'}
                ])).toEqual(['earth:smallData', 'mars:corruptedData'])
            })
        });

        describe('buildFieldText', function() {
            it('called with the empty array returns null', function() {
                expect(searchDataUtil.buildFieldText([])).toBeNull();
            });

            it('called with the non-empty array returns the result of the toString method on the return value of the toFieldTextNode function', function() {
                var parametricValues = [
                    {
                        field: 'CATEGORY',
                        value: 'GENERAL'
                    },
                    {
                        field: 'SOURCE',
                        value: 'GOOGLE'
                    }
                ];

                expect(searchDataUtil.buildFieldText(parametricValues)).toEqual(toFieldTextNode(parametricValues).toString());
            })
        });
    });

});