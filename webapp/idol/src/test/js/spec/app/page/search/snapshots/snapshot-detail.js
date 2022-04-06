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
    'find/idol/app/page/search/snapshots/snapshot-detail',
    'i18n!find/nls/bundle',
    'i18n!find/idol/nls/snapshots'
], function(_, moment, Backbone, snapshotDetail, i18n, snapshotsI18n) {
    'use strict';

    function runProcessAttributes(input) {
        // Only pick the target attributes to reflect how processAttributes is called in the DataPanelView
        return snapshotDetail.processAttributes(
            new Backbone.Model(input), _.pick(input, snapshotDetail.targetAttributes));
    }

    describe('Snapshot detail panel', function() {
        it('returns the formatted date created and the result count', function() {
            const output = runProcessAttributes({
                dateCreated: moment(1455026659454),
                resultCount: 25
            });

            expect(output).toHaveLength(2);
            expect(output[0].title).toBe(snapshotsI18n['detail.dateCreated']);
            expect(output[0].content).toContain('2016/02/09');
            expect(output[1].title).toBe(snapshotsI18n['detail.resultCount']);
            expect(output[1].content).toEqual(25);
        });

        it('leaves out the result count when the result count is not present in the attributes', function() {
            const output = runProcessAttributes({
                dateCreated: moment(1455026659454)
            });

            expect(output[1]).toBe(undefined);
        });

        it('returns 0 when the result count is 0', function() {
            const output = runProcessAttributes({
                dateCreated: moment(1455026659454),
                resultCount: 0
            });

            expect(output[1].title).toBe(snapshotsI18n['detail.resultCount']);
            expect(output[1].content).toEqual(0);
        });
    });
});
