/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/idol/app/page/search/snapshots/snapshot-restrictions',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'i18n!find/idol/nls/snapshots',
    'underscore',
    'moment'
], function (snapshotRestrictions, i18n, indexesI18n, snapshotsI18n, _, moment) {

    function runProcessAttributes(input) {
        // Only pick the target attributes to reflect how processAttributes is called in the DataPanelView
        return _.compact(snapshotRestrictions.processAttributes(_.pick(input, snapshotRestrictions.targetAttributes)));
    }

    describe('Snapshot restrictions panel', function () {
        it('returns query text and indexes', function () {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: []
            });

            expect(output.length).toBe(1);
            expect(output[0].title).toBe(indexesI18n['search.indexes']);
            expect(output[0].content).toContain('Wikipedia');
            expect(output[0].content).toContain('Admissions');
        });

        it('returns related concepts if they are present in the attributes', function () {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: ['Copenhagen', 'Quantum'],
                parametricValues: []
            });

            expect(output.length).toBe(2);
            expect(output[0].title).toBe(snapshotsI18n['restrictions.relatedConcepts']);
            expect(output[0].content).toContain('Copenhagen');
            expect(output[0].content).toContain('Quantum');
        });

        it('formats and returns a minimum date if present in the attributes', function () {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [],
                minDate: moment(1455026659454)
            });

            expect(output.length).toBe(2);
            expect(output[1].title).toBe(snapshotsI18n['restrictions.minDate']);
            expect(output[1].content).toContain('2016/02/09');
        });

        it('formats and returns a maximum date if present in the attributes', function () {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [],
                maxDate: moment(1455026659454)
            });

            expect(output.length).toBe(2);
            expect(output[1].title).toBe(snapshotsI18n['restrictions.maxDate']);
            expect(output[1].content).toContain('2016/02/09');
        });

        it('groups, prettifies and returns parametric values present in the attributes', function () {
            const output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [
                    {field: 'animal', displayName: 'Animal', value: 'cat', displayValue: 'Cat', type: 'Parametric'},
                    {field: 'PRIMARY_COLOUR', displayName: 'Primary Colour', value: 'ginger', displayValue: 'Ginger', type: 'Parametric'},
                    {field: 'PRIMARY_COLOUR', displayName: 'Primary Colour', value: 'black', displayValue: 'Black', type: 'Parametric'}
                ]
            });

            expect(output.length).toBe(3);
            expect(output[1].title).toBe('Animal');
            expect(output[1].content).toContain('Cat');
            expect(output[2].title).toBe('Primary Colour');
            expect(output[2].content).toContain('Ginger');
            expect(output[2].content).toContain('Black');
        });
    });

});
