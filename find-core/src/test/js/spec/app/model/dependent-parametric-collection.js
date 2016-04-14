/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/dependent-parametric-collection'
], function(DependentParametricCollection) {

    describe('DependentParametricCollection', function() {
        describe('parse method', function() {
            it('parses the response for one field name', function() {
                expect(DependentParametricCollection.prototype.parse([
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
                ])).toEqual([
                    {children: [], count: 1, text: 'THE DAILY HERALD', hidden: true},
                    {children: [], count: 2, text: 'THE AUSTRALIAN', hidden: true},
                    {children: [], count: 32, text: 'THE ORANGE COUNTY REGISTER', hidden: true},
                    {children: [], count: 131, text: 'USA TODAY', hidden: true},
                    {children: [], count: 389, text: 'ARIZONA DAILY STAR', hidden: true},
                    {children: [], count: 738, text: 'AP WIRE', hidden: true},
                    {children: [], count: 933, text: 'WONDER WALL', hidden: true},
                    {children: [], count: 2122, text: 'ALBUQUERQUE JOURNAL', hidden: false},
                    {children: [], count: 2893, text: 'THE MIAMI HERALD', hidden: false},
                    {children: [], count: 5396, text: 'THE WASHINGTON POST', hidden: false},
                    {children: [], count: 8082, text: 'BBC', hidden: false},
                    {children: [], count: 8803, text: 'ABC NEWS', hidden: false}
                ]);
            });

            it('parses the response for two field names', function() {
                var output = DependentParametricCollection.prototype.parse([
                    {count: '2', value: 'THE AUSTRALIAN', field: [
                        {field: [], count: '2', value: 'BUSINESS'}
                    ]},
                    {count: '1', value: 'THE DAILY HERALD', field: [
                        {field: [], count: '1', value: 'BUSINESS'}
                    ]},
                    {count: '2122', value: 'ALBUQUERQUE JOURNAL', field: [
                        {field: [], count: '22', value: 'BUSINESS'},
                        {field: [], count: '2100', value: 'LIVING PEOPLE'}
                    ]},
                    {count: '8803', value: 'ABC NEWS', field: [
                        {field: [], count: '8803', value: 'BUSINESS'}
                    ]},
                    {count: '2893', value: 'THE MIAMI HERALD', field: [
                        {field: [], count: '2893', value: 'BUSINESS'}
                    ]},
                    {count: '738', value: 'AP WIRE', field: [
                        {field: [], count: '738', value: 'BUSINESS'}
                    ]},
                    {count: '389', value: 'ARIZONA DAILY STAR', field: [
                        {field: [], count: '389', value: 'BUSINESS'}
                    ]},
                    {count: '32', value: 'THE ORANGE COUNTY REGISTER', field: [
                        {field: [], count: '32', value: 'BUSINESS'}
                    ]},
                    {count: '8082', value: 'BBC', field: [
                        {field: [], count: '8082', value: 'BUSINESS'}
                    ]},
                    {count: '5396', value: 'THE WASHINGTON POST', field: [
                        {field: [], count: '5396', value: 'BUSINESS'}
                    ]},
                    {count: '131', value: 'USA TODAY', field: [
                        {field: [], count: '131', value: 'BUSINESS'}
                    ]},
                    {count: '970', value: 'WONDER WALL', field: [
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
                    ]}
                ]);

                var expected = [
                    {text: 'THE DAILY HERALD', count: 1, children: [
                        {hidden: false, count: 1, text: 'BUSINESS'}
                    ], hidden: true},
                    {text: 'THE AUSTRALIAN', count: 2, children: [
                        {hidden: false, count: 2, text: 'BUSINESS'}
                    ], hidden: true},
                    {text: 'THE ORANGE COUNTY REGISTER', count: 32, children: [
                        {hidden: false, text: 'BUSINESS', count: 32}
                    ], hidden: true},
                    {text: 'USA TODAY', count: 131, children: [
                        {hidden: false, text: 'BUSINESS', count: 131}
                    ], hidden: true},
                    {text: 'ARIZONA DAILY STAR', count: 389, children: [
                        {hidden: false, text: 'BUSINESS', count: 389}
                    ], hidden: true},
                    {text: 'AP WIRE', count: 738, children: [
                        {hidden: false, text: 'BUSINESS', count: 738}
                    ], hidden: true},
                    {text: 'WONDER WALL', count: 970, children: [
                        {hidden: true, text: 'ACTORS', count: 23},
                        {hidden: true, text: 'UK', count: 35},
                        {hidden: false, text: 'FOOD', count: 51},
                        {hidden: false, text: 'DRINK',count: 52},
                        {hidden: false, text: 'TECHNOLOGY', count: 57},
                        {hidden: false, text: 'WORLD', count: 58},
                        {hidden: false, text: 'AMERICAS', count: 75},
                        {hidden: false, text: 'CARS ', count: 112},
                        {hidden: false, text: 'ANIMALS', count: 125},
                        {hidden: false, text: 'SCIENCE', count: 180},
                        {hidden: false, text: 'EUROPE', count: 225}
                    ], hidden: true},
                    {text: 'ALBUQUERQUE JOURNAL', count: 2122, children: [
                        {hidden: true, text: 'BUSINESS', count: 22}, {hidden: false, text: 'LIVING PEOPLE', count: 2100}
                    ], hidden: false},
                    {text: 'THE MIAMI HERALD', count: 2893, children: [
                        {hidden: false, text: 'BUSINESS', count: 2893}
                    ], hidden: false},
                    {text: 'THE WASHINGTON POST', count: 5396, children: [
                        {hidden: false, text: 'BUSINESS', count: 5396}
                    ], hidden: false},
                    {text: 'BBC', count: 8082, children: [
                        {hidden: false, text: 'BUSINESS', count: 8082}
                    ], hidden: false},
                    {text: 'ABC NEWS', count: 8803, children: [
                        {hidden: false, text: 'BUSINESS', count: 8803}
                    ], hidden: false}
                ];

                expect(output).toEqual(expected);
            });

            it('returns an empty array when given an empty array', function() {
                expect(DependentParametricCollection.prototype.parse([])).toEqual([]);
            });
        });
    });

});