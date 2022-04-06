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
    'backbone',
    'find/app/util/search-data-util',
    'parametric-refinement/to-field-text-node',
    'fieldtext/js/field-text-parser'
], function(Backbone, searchDataUtil, toFieldTextNode, fieldTextParser) {
    'use strict';

    const createFieldText = function (field, value) {
        return new fieldTextParser.ExpressionNode('MATCH', [field], [value]);
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

        describe('buildMergedFieldText', function () {

            it('combines fieldtext from provided sources', function () {
                const fieldText = searchDataUtil.buildMergedFieldText(
                    [
                        new Backbone.Model({ field: 'fielda1', value: 'a1' }),
                        new Backbone.Model({ field: 'fielda2', value: 'a2' })
                    ],
                    { toFieldText: () => createFieldText('fieldb', 'b') },
                    { toFieldText: () => createFieldText('fieldc', 'c') }
                );
                expect(fieldText.toString()).toBe(
                    'MATCH{a1}:fielda1 AND MATCH{a2}:fielda2 AND ' +
                        'MATCH{b}:fieldb AND MATCH{c}:fieldc');
            });

            it('omits null fieldtext', function () {
                const fieldText = searchDataUtil.buildMergedFieldText(
                    [new Backbone.Model({ field: 'fielda', value: 'a' })],
                    { toFieldText: () => null },
                    { toFieldText: () => createFieldText('fieldc', 'c') }
                );
                expect(fieldText.toString()).toBe('MATCH{a}:fielda AND MATCH{c}:fieldc');
            });

            it('returns null if all sources return null', function () {
                const fieldText = searchDataUtil.buildMergedFieldText(
                    [],
                    { toFieldText: () => null },
                    { toFieldText: () => null }
                );
                expect(fieldText).toBe(null);
            });

        });

        describe('buildMergedFieldTextWithoutDocumentSelection', function () {

            it('combines fieldtext from provided sources', function () {
                const fieldText = searchDataUtil.buildMergedFieldTextWithoutDocumentSelection(
                    { toFieldTextNode: () => createFieldText('fielda', 'a') },
                    { toFieldText: () => createFieldText('fieldb', 'b') }
                );
                expect(fieldText.toString())
                    .toBe('MATCH{a}:fielda AND MATCH{b}:fieldb');
            });

            it('omits null fieldtext', function () {
                const fieldText = searchDataUtil.buildMergedFieldTextWithoutDocumentSelection(
                    { toFieldTextNode: () => createFieldText('fielda', 'a') },
                    { toFieldText: () => null }
                );
                expect(fieldText.toString()).toBe('MATCH{a}:fielda');
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
