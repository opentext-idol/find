/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/snapshots/snapshot-restrictions',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'underscore',
    'moment'
], function(snapshotRestrictions, i18n, indexesI18n, _, moment) {

    function runProcessAttributes(input) {
        // Only pick the target attributes to reflect how processAttributes is called in the DataPanelView
        return _.compact(snapshotRestrictions.processAttributes(_.pick(input, snapshotRestrictions.targetAttributes)));
    }

    describe('Snapshot restrictions panel', function() {
        it('returns query text and indexes', function() {
            var output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: []
            });

            expect(output.length).toBe(2);
            expect(output[0].title).toBe(i18n['search.snapshot.restrictions.queryText']);
            expect(output[0].content).toBe('cat');
            expect(output[1].title).toBe(indexesI18n['search.indexes']);
            expect(output[1].content).toContain('Wikipedia');
            expect(output[1].content).toContain('Admissions');
        });

        it('returns related concepts if they are present in the attributes', function() {
            var output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: ['Copenhagen', 'Quantum'],
                parametricValues: []
            });

            expect(output.length).toBe(3);
            expect(output[1].title).toBe(i18n['search.snapshot.restrictions.relatedConcepts']);
            expect(output[1].content).toContain('Copenhagen');
            expect(output[1].content).toContain('Quantum');
        });

        it('formats and returns a minimum date if present in the attributes', function() {
            var output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [],
                minDate: moment(1455026659454)
            });

            expect(output.length).toBe(3);
            expect(output[2].title).toBe(i18n['search.snapshot.restrictions.minDate']);
            expect(output[2].content).toContain('2016/02/09');
        });

        it('formats and returns a maximum date if present in the attributes', function() {
            var output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [],
                maxDate: moment(1455026659454)
            });

            expect(output.length).toBe(3);
            expect(output[2].title).toBe(i18n['search.snapshot.restrictions.maxDate']);
            expect(output[2].content).toContain('2016/02/09');
        });

        it('groups, prettifies and returns parametric values present in the attributes', function() {
            var output = runProcessAttributes({
                indexes: [{name: 'Wikipedia', domain: null}, {name: 'Admissions', domain: null}],
                queryText: 'cat',
                relatedConcepts: [],
                parametricValues: [
                    {field: 'animal', value: 'cat'},
                    {field: 'PRIMARY_COLOUR', value: 'ginger'},
                    {field: 'PRIMARY_COLOUR', value: 'black'}
                ]
            });

            expect(output.length).toBe(4);
            expect(output[2].title).toBe('Animal');
            expect(output[2].content).toContain('cat');
            expect(output[3].title).toBe('Primary Colour');
            expect(output[3].content).toContain('ginger');
            expect(output[3].content).toContain('black');
        });
    });

});
