/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'find/idol/app/page/dashboard/legend-color-collection'
], function(_, Backbone, LegendColorCollection) {
    'use strict';

    const expectedOutputOneTier = [{
        hidden: false,
        text: 't00',
        underlyingValue: 't00',
        count: 11
    }, {
        hidden: false,
        text: 't01',
        underlyingValue: 't01',
        count: 111
    }, {
        hidden: false,
        text: 't02',
        underlyingValue: 't02',
        count: 22
    }, {
        hidden: false,
        text: 't03',
        underlyingValue: 't03',
        count: 202
    }, {
        hidden: false,
        text: 't04',
        underlyingValue: 't04',
        count: 322
    }, {
        hidden: false,
        text: 't05',
        underlyingValue: 't05',
        count: 2
    }, {
        hidden: false,
        text: 't06',
        underlyingValue: 't06',
        count: 10
    }];

    function prepareInputs(expectedOutput) {
        return _.map(expectedOutput, function(d) {
            return {count: d.count, value: d.text, displayValue: d.text, subFields: []};
        });
    }

    const defaultPalette = ['c00', 'c01', 'c02', 'c03', 'c04'];
    const HIDDEN_COLOR = 'HIDDEN_COLOR';

    function prepareExpected(array) {
        const defaultPaletteLength = defaultPalette.length;
        return _.map(_.sortBy(array, function(d) {
            return -d.count;
        }), function(d, i) {
            if(i < defaultPaletteLength) {
                return _.extend(d, {color: defaultPalette[i]});
            } else {
                return _.extend(d, {color: HIDDEN_COLOR});
            }
        });
    }

    describe('Legend Color Collection', function() {
        beforeEach(function() {
            this.collection = new LegendColorCollection(null, {
                maxLegendEntries: 200,
                palette: defaultPalette,
                hiddenColor: HIDDEN_COLOR
            });

            spyOn(this.collection, 'sync');

            this.queryModel = _.extend(new Backbone.Model(), {getIsoDate: _.noop});

            this.collection.fetchDependentFields(this.queryModel, 'field1');
        });

        it('adds a colour from the queue to an added model', function() {
            const args = expectedOutputOneTier.slice(0, 1);
            const expected = prepareExpected.call(this, args);
            this.collection.sync.calls.mostRecent().args[2].success(prepareInputs(args));
            const actual = this.collection.toJSON();

            expect(actual).toEqual(expected);
        });

        it('adds colours from the queue to multiple models, sorts descending by counts', function() {
            const args = expectedOutputOneTier.slice(0, 3);
            const expected = prepareExpected.call(this, args);
            this.collection.sync.calls.mostRecent().args[2].success(prepareInputs(args));
            const actual = this.collection.toJSON();

            expect(actual).toEqual(expected);
        });

        it('adds a default colour to excess models when it runs out of palette, sorts descending by counts', function() {
            const args = expectedOutputOneTier.slice(0, 7);
            const expected = prepareExpected.call(this, args);
            this.collection.sync.calls.mostRecent().args[2].success(prepareInputs(args));
            const actual = this.collection.toJSON();

            expect(actual).toEqual(expected);
        });

        describe('when colours are added, removed, and added again', function() {
            beforeEach(function() {
                this.collection.sync.calls.mostRecent().args[2]
                    .success(prepareInputs(expectedOutputOneTier.slice(0, 3)));

                this.collection.fetchDependentFields(this.queryModel, 'field1');
            });

            it('reuses a colour for a previously seen datum', function() {
                const args = expectedOutputOneTier.slice(1, 2);
                const expected = prepareExpected.call(this, args);
                this.collection.sync.calls.mostRecent().args[2].success(prepareInputs(args));
                const actual = this.collection.toJSON();

                expect(actual).toEqual(expected);
            });

            it('reuses a colour for previously seen data, and assigns new ones to new data', function() {
                const args = expectedOutputOneTier.slice(1, 2).push(expectedOutputOneTier[6]);
                const expected = prepareExpected.call(this, args);
                this.collection.sync.calls.mostRecent().args[2].success(prepareInputs(args));
                const actual = this.collection.toJSON();

                expect(actual).toEqual(expected);
            });
        });
    });
});
