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
    'underscore',
    'moment',
    'backbone',
    'find/idol/app/page/search/snapshots/snapshot-restrictions',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'i18n!find/idol/nls/snapshots'
], function(_, moment, Backbone, snapshotRestrictions, i18n, indexesI18n, snapshotsI18n) {
    'use strict';

    function runProcessAttributes(input) {
        const model = new Backbone.Model(input);
        model.toDocumentSelectionModelAttributes = function () {
            return {
                isWhitelist: input.documentSelectionIsWhitelist,
                references: _.pluck(input.documentSelection, 'reference')
            };
        };
        // Only pick the target attributes to reflect how processAttributes is called in the DataPanelView
        return _.compact(snapshotRestrictions.processAttributes(
            model, _.pick(input, snapshotRestrictions.targetAttributes)));
    }

    describe('Snapshot restrictions panel', function() {
        it('returns query text and indexes', function() {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [],
                documentSelectionIsWhitelist: false,
                documentSelection: []
            });

            expect(output).toHaveLength(1);
            expect(output[0].title).toBe(indexesI18n['search.indexes']);
            expect(output[0].content).toContain('Wikipedia');
            expect(output[0].content).toContain('Admissions');
        });

        it('returns related concepts if they are present in the attributes', function() {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: ['Copenhagen', 'Quantum'],
                parametricValues: [],
                documentSelectionIsWhitelist: false,
                documentSelection: []
            });

            expect(output).toHaveLength(2);
            expect(output[0].title).toBe(snapshotsI18n['restrictions.relatedConcepts']);
            expect(output[0].content).toContain('Copenhagen');
            expect(output[0].content).toContain('Quantum');
        });

        it('formats and returns a minimum date if present in the attributes', function() {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [],
                minDate: moment(1455026659454),
                documentSelectionIsWhitelist: false,
                documentSelection: []
            });

            expect(output).toHaveLength(2);
            expect(output[1].title).toBe(snapshotsI18n['restrictions.minDate']);
            expect(output[1].content).toContain('2016/02/09');
        });

        it('formats and returns a maximum date if present in the attributes', function() {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [],
                maxDate: moment(1455026659454),
                documentSelectionIsWhitelist: false,
                documentSelection: []
            });

            expect(output).toHaveLength(2);
            expect(output[1].title).toBe(snapshotsI18n['restrictions.maxDate']);
            expect(output[1].content).toContain('2016/02/09');
        });

        it('groups, prettifies and returns parametric values present in the attributes', function() {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [
                    {field: 'animal', displayName: 'Animal', value: 'cat', displayValue: 'Cat', type: 'Parametric'},
                    {
                        field: 'PRIMARY_COLOUR',
                        displayName: 'Primary Colour',
                        value: 'ginger',
                        displayValue: 'Ginger',
                        type: 'Parametric'
                    },
                    {
                        field: 'PRIMARY_COLOUR',
                        displayName: 'Primary Colour',
                        value: 'black',
                        displayValue: 'Black',
                        type: 'Parametric'
                    }
                ],
                documentSelectionIsWhitelist: false,
                documentSelection: []
            });

            expect(output).toHaveLength(3);
            expect(output[1].title).toBe('Animal');
            expect(output[1].content).toContain('Cat');
            expect(output[2].title).toBe('Primary Colour');
            expect(output[2].content).toContain('Ginger');
            expect(output[2].content).toContain('Black');
        });

        it('returns document selection', function() {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [],
                documentSelectionIsWhitelist: false,
                documentSelection: [{ reference: 'first' }, { reference: 'second' }]
            });

            expect(output).toHaveLength(2);
            expect(output[1].title).toBe('Document Selection');
            expect(output[1].content).toBe('Documents excluded: 2');
        });

    });
});
