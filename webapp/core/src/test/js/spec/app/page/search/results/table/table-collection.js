/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'find/app/page/search/results/table/table-collection'
], function(TableCollection) {
    'use strict';

    describe('Table view collection', function() {
        beforeEach(function() {
            this.collection = new TableCollection();
        });

        describe('parse method', function() {
            describe('when only one field is wanted', function() {
                beforeEach(function() {
                    this.result = this.collection.parse([
                        {
                            count: 42,
                            subFields: [],
                            value: 'wine',
                            displayValue: 'wine'
                        },
                        {
                            count: 36,
                            subFields: [],
                            value: 'cheese',
                            displayValue: 'cheese'
                        }
                    ])
                });

                it('should have an empty array of column names', function() {
                    expect(this.collection.columnNames).toEqual([]);
                });

                it('should parse models correctly', function() {
                    expect(this.result).toHaveLength(2);

                    expect(this.result[0]).toEqual({
                        count: 42,
                        text: 'wine'
                    });

                    expect(this.result[1]).toEqual({
                        count: 36,
                        text: 'cheese'
                    });
                });
            });

            describe('when two fields are wanted', function() {
                describe('and the empty string is not returned', function() {
                    beforeEach(function() {
                        this.result = this.collection.parse([
                            {
                                count: 42,
                                value: 'wine',
                                displayValue: 'wine',
                                subFields: [
                                    {
                                        count: 24,
                                        value: 'france',
                                        displayValue: 'france'
                                    },
                                    {
                                        count: 18,
                                        value: 'germany',
                                        displayValue: 'germany'
                                    }
                                ]
                            },
                            {
                                count: 36,
                                value: 'cheese',
                                displayValue: 'cheese',
                                subFields: [
                                    {
                                        count: 24,
                                        value: 'france',
                                        displayValue: 'france'
                                    },
                                    {
                                        count: 12,
                                        value: 'germany',
                                        displayValue: 'germany'
                                    }
                                ]
                            }
                        ])
                    });

                    it('should have the correct array of column names', function() {
                        expect(this.collection.columnNames).toEqual(['france', 'germany']);
                    });

                    it('should parse models correctly', function() {
                        expect(this.result).toHaveLength(2);

                        expect(this.result[0]).toEqual({
                            france: 24,
                            germany: 18,
                            text: 'wine'
                        });

                        expect(this.result[1]).toEqual({
                            france: 24,
                            germany: 12,
                            text: 'cheese'
                        });
                    });
                });

                describe('and the empty string is returned', function() {
                    beforeEach(function() {
                        this.result = this.collection.parse([
                            {
                                count: 47,
                                value: 'wine',
                                displayValue: 'wine',
                                subFields: [
                                    {
                                        count: 24,
                                        value: 'france',
                                        displayValue: 'france'
                                    },
                                    {
                                        count: 18,
                                        value: 'germany',
                                        displayValue: 'germany'
                                    },
                                    {
                                        count: 5,
                                        value: '',
                                        displayValue: ''
                                    }
                                ]
                            },
                            {
                                count: 40,
                                value: 'cheese',
                                displayValue: 'cheese',
                                subFields: [
                                    {
                                        count: 24,
                                        value: 'france',
                                        displayValue: 'france'
                                    },
                                    {
                                        count: 12,
                                        value: 'germany',
                                        displayValue: 'germany'
                                    },
                                    {
                                        count: 4,
                                        value: '',
                                        displayValue: ''
                                    }
                                ]
                            }
                        ])
                    });

                    it('should have the correct array of column names', function() {
                        expect(this.collection.columnNames).toEqual([TableCollection.noneColumn, 'france', 'germany']);
                    });

                    it('should parse models correctly', function() {
                        expect(this.result).toHaveLength(2);

                        expect(this.result[0]).toEqual(jasmine.objectContaining({
                            france: 24,
                            germany: 18,
                            text: 'wine'
                        }));

                        expect(this.result[0][TableCollection.noneColumn]).toBe(5);

                        expect(this.result[1]).toEqual(jasmine.objectContaining({
                            france: 24,
                            germany: 12,
                            text: 'cheese'
                        }));

                        expect(this.result[1][TableCollection.noneColumn]).toBe(4);
                    });
                });
            });
        });
    });
});
