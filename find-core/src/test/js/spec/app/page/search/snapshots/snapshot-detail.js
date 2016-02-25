/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/snapshots/snapshot-detail',
    'i18n!find/nls/bundle',
    'underscore',
    'moment'
], function(snapshotDetail, i18n, _, moment) {

    function runProcessAttributes(input) {
        // Only pick the target attributes to reflect how processAttributes is called in the DataPanelView
        return snapshotDetail.processAttributes(_.pick(input, snapshotDetail.targetAttributes));
    }

    describe('Snapshot detail panel', function() {
        it('returns the formatted date created and the result count', function() {
            var output = runProcessAttributes({
                dateCreated: moment(1455026659454),
                resultCount: 25
            });

            expect(output.length).toBe(2);
            expect(output[0].title).toBe(i18n['search.snapshot.detail.dateCreated']);
            expect(output[0].content).toContain('2016/02/09');
            expect(output[1].title).toBe(i18n['search.snapshot.detail.resultCount']);
            expect(output[1].content).toEqual(25);
        });

        it('returns "Unknown" when the result count is not present in the attributes', function() {
            var output = runProcessAttributes({
                dateCreated: moment(1455026659454)
            });

            expect(output[1].title).toBe(i18n['search.snapshot.detail.resultCount']);
            expect(output[1].content).toBe(i18n['app.unknown']);
        });

        it('returns 0 when the result count is 0', function() {
            var output = runProcessAttributes({
                dateCreated: moment(1455026659454),
                resultCount: 0
            });

            expect(output[1].title).toBe(i18n['search.snapshot.detail.resultCount']);
            expect(output[1].content).toEqual(0);
        });
    });

});
