/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/util/search-data-util',
    'parametric-refinement/to-field-text-node',
    'fieldtext/js/field-text-parser'
], function(searchDataUtil, toFieldTextNode, fieldTextParser) {
    'use strict';

    const createFieldText = function (id) {
        return new fieldTextParser.ExpressionNode('MATCH', ['field'], [id]);
    };

    describe('Search Data Utils', function() {
        describe('makeQueryText', function() {
            it('called with no concepts returns *', function() {
                expect(searchDataUtil.makeQueryText([])).toEqual('*');
            });

            it('called with one concepts returns that concept, preserving quotes', function() {
                expect(searchDataUtil.makeQueryText([['"chlorine fluoride"']])).toEqual('("chlorine fluoride")');
            });

            it('called with multiple concepts returns those concepts', function() {
                expect(searchDataUtil.makeQueryText([['chlorine fluoride'], ['activated carbon'], ['"mercury"', '"bromine"', '"ammonia"']]))
                    .toEqual('(chlorine fluoride) AND (activated carbon) AND ("mercury" "bromine" "ammonia")');
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
                const parametricValues = [
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

        describe('buildMergedFieldText', function () {

            it('combines fieldtext from provided sources', function () {
                const fieldText = searchDataUtil.buildMergedFieldText(
                    { toFieldTextNode: () => createFieldText('a') },
                    { toFieldText: () => createFieldText('b') },
                    { toFieldText: () => createFieldText('c') }
                );
                expect(fieldText.toString())
                    .toBe('MATCH{a}:field AND MATCH{b}:field AND MATCH{c}:field');
            });

            it('omits null fieldtext', function () {
                const fieldText = searchDataUtil.buildMergedFieldText(
                    { toFieldTextNode: () => createFieldText('a') },
                    { toFieldText: () => null },
                    { toFieldText: () => createFieldText('c') }
                );
                expect(fieldText.toString()).toBe('MATCH{a}:field AND MATCH{c}:field');
            });

            it('returns null if all sources return null', function () {
                const fieldText = searchDataUtil.buildMergedFieldText(
                    { toFieldTextNode: () => null },
                    { toFieldText: () => null },
                    { toFieldText: () => null }
                );
                expect(fieldText).toBe(null);
            });

        });

        describe('buildMergedFieldTextWithoutDocumentSelection', function () {

            it('combines fieldtext from provided sources', function () {
                const fieldText = searchDataUtil.buildMergedFieldTextWithoutDocumentSelection(
                    { toFieldTextNode: () => createFieldText('a') },
                    { toFieldText: () => createFieldText('b') }
                );
                expect(fieldText.toString())
                    .toBe('MATCH{a}:field AND MATCH{b}:field');
            });

            it('omits null fieldtext', function () {
                const fieldText = searchDataUtil.buildMergedFieldTextWithoutDocumentSelection(
                    { toFieldTextNode: () => createFieldText('a') },
                    { toFieldText: () => null }
                );
                expect(fieldText.toString()).toBe('MATCH{a}:field');
            });

            it('returns null if all sources return null', function () {
                const fieldText = searchDataUtil.buildMergedFieldTextWithoutDocumentSelection(
                    { toFieldTextNode: () => null },
                    { toFieldText: () => null }
                );
                expect(fieldText).toBe(null);
            });

        });

    });
});
