/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/dependent-parametric-collection'
], function (DependentParametricCollection) {

    describe('DependentParametricCollection', function () {
        describe('parse method', function () {
            it('parses the response for one field name', function () {
                var output = DependentParametricCollection.prototype.parse([
                    {field: [], count: '2', value: 'THE AUSTRALIAN'},
                    {field: [], count: '1', value: 'THE DAILY HERALD'},
                    {field: [], count: '2122', value: 'ALBUQUERQUE JOURNAL'},
                    {field: [], count: '8803', value: 'ABC NEWS'},
                    {field: [], count: '2893', value: 'THE MIAMI HERALD'},
                    {field: [], count: '738', value: 'AP WIRE'},
                    {field: [], count: '389', value: 'ARIZONA DAILY STAR'},
                    {field: [], count: '32', value: 'THE ORANGE COUNTY REGISTER'},
                    {field: [], count: '8082', value: 'BBC'},
                    {field: [], count: '5396', value: 'THE WASHINGTON POST'},
                    {field: [], count: '131', value: 'USA TODAY'},
                    {field: [], count: '933', value: 'WONDER WALL'}
                ]);
                var expected = [
                    {
                        text: "",
                        hidden: true,
                        count: 750,
                        hiddenFilterCount: 7
                    },
                    {
                        text: "",
                        hidden: true,
                        count: 1476,
                        hiddenFilterCount: 7
                    },
                    {
                        hidden: false,
                        text: "ALBUQUERQUE JOURNAL",
                        count: 2122
                    },
                    {
                        hidden: false,
                        text: "THE MIAMI HERALD",
                        count: 2893
                    },
                    {
                        hidden: false,
                        text: "THE WASHINGTON POST",
                        count: 5396
                    },
                    {
                        hidden: false,
                        text: "BBC",
                        count: 8082
                    },
                    {
                        hidden: false,
                        text: "ABC NEWS",
                        count: 8803
                    }
                ];

                expect(output).toEqual(expected);
            });

            it('parses the response for two field names', function () {
                var output = DependentParametricCollection.prototype.parse([
                    {
                        count: '2', value: 'THE AUSTRALIAN', field: [
                        {field: [], count: '2', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '1', value: 'THE DAILY HERALD', field: [
                        {field: [], count: '1', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '2122', value: 'ALBUQUERQUE JOURNAL', field: [
                        {field: [], count: '22', value: 'BUSINESS'},
                        {field: [], count: '2100', value: 'LIVING PEOPLE'}
                    ]
                    },
                    {
                        count: '8803', value: 'ABC NEWS', field: [
                        {field: [], count: '8803', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '2893', value: 'THE MIAMI HERALD', field: [
                        {field: [], count: '2893', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '738', value: 'AP WIRE', field: [
                        {field: [], count: '738', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '389', value: 'ARIZONA DAILY STAR', field: [
                        {field: [], count: '389', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '32', value: 'THE ORANGE COUNTY REGISTER', field: [
                        {field: [], count: '32', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '8082', value: 'BBC', field: [
                        {field: [], count: '8082', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '5396', value: 'THE WASHINGTON POST', field: [
                        {field: [], count: '5396', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '131', value: 'USA TODAY', field: [
                        {field: [], count: '131', value: 'BUSINESS'}
                    ]
                    },
                    {
                        count: '970', value: 'WONDER WALL', field: [
                        {field: [], count: '23', value: 'ACTORS'},
                        {field: [], count: '51', value: 'FOOD'},
                        {field: [], count: '52', value: 'DRINK'},
                        {field: [], count: '125', value: 'ANIMALS'},
                        {field: [], count: '180', value: 'SCIENCE'},
                        {field: [], count: '35', value: 'UK'},
                        {field: [], count: '58', value: 'WORLD'},
                        {field: [], count: '57', value: 'TECHNOLOGY'},
                        {field: [], count: '225', value: 'EUROPE'},
                        {field: [], count: '75', value: 'AMERICAS'},
                        {field: [], count: '112', value: 'CARS '}
                    ]
                    }
                ]);

                var expected = [
                    {
                        text: "",
                        hidden: true,
                        count: 785,
                        hiddenFilterCount: 7
                    },
                    {
                        text: "",
                        hidden: true,
                        count: 1478,
                        hiddenFilterCount: 7
                    },
                    {
                        hidden: false,
                        text: "ALBUQUERQUE JOURNAL",
                        count: 2122,
                        children: [
                            {
                                text: "",
                                hidden: true,
                                count: 22,
                                hiddenFilterCount: 1
                            },
                            {
                                hidden: false,
                                text: "LIVING PEOPLE",
                                count: 2100
                            }
                        ]
                    },
                    {
                        hidden: false,
                        text: "THE MIAMI HERALD",
                        count: 2893,
                        children: [
                            {
                                hidden: false,
                                text: "BUSINESS",
                                count: 2893
                            }
                        ]
                    },
                    {
                        hidden: false,
                        text: "THE WASHINGTON POST",
                        count: 5396,
                        children: [
                            {
                                hidden: false,
                                text: "BUSINESS",
                                count: 5396
                            }
                        ]
                    },
                    {
                        hidden: false,
                        text: "BBC",
                        count: 8082,
                        children: [
                            {
                                hidden: false,
                                text: "BUSINESS",
                                count: 8082
                            }
                        ]
                    },
                    {
                        hidden: false,
                        text: "ABC NEWS",
                        count: 8803,
                        children: [
                            {
                                hidden: false,
                                text: "BUSINESS",
                                count: 8803
                            }
                        ]
                    }
                ];

                expect(output).toEqual(expected);
            });

            it('returns an empty array when given an empty array', function () {
                expect(DependentParametricCollection.prototype.parse([])).toEqual([]);
            });
        });
    });

});