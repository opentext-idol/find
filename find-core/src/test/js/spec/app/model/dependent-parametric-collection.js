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
                    {children: [], count: 32, text: 'THE ORANGE COUNTY REGISTER'},
                    {children: [], count: 131, text: 'USA TODAY'},
                    {children: [], count: 389, text: 'ARIZONA DAILY STAR'},
                    {children: [], count: 738, text: 'AP WIRE'},
                    {children: [], count: 933, text: 'WONDER WALL'},
                    {children: [], count: 2122, text: 'ALBUQUERQUE JOURNAL'},
                    {children: [], count: 2893, text: 'THE MIAMI HERALD'},
                    {children: [], count: 5396, text: 'THE WASHINGTON POST'},
                    {children: [], count: 8082, text: 'BBC'},
                    {children: [], count: 8803, text: 'ABC NEWS'}
                ]);
            });

            it('parses the response for two field names', function() {
                expect(DependentParametricCollection.prototype.parse([
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
                    {count: '933', value: 'WONDER WALL', field: [
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
                ])).toEqual([
                    {text: 'THE ORANGE COUNTY REGISTER', count: 32, children: [
                        {text: 'BUSINESS', count: 32}
                    ]},
                    {text: 'USA TODAY', count: 131, children: [
                        {text: 'BUSINESS', count: 131}
                    ]},
                    {text: 'ARIZONA DAILY STAR', count: 389, children: [
                        {text: 'BUSINESS', count: 389}
                    ]},
                    {text: 'AP WIRE', count: 738, children: [
                        {text: 'BUSINESS', count: 738}
                    ]},
                    {text: 'WONDER WALL', count: 933, children: [
                        {text: 'UK', count: 35},
                        {text: 'FOOD', count: 51},
                        {text: 'DRINK',count: 52},
                        {text: 'TECHNOLOGY', count: 57},
                        {text: 'WORLD', count: 58},
                        {text: 'AMERICAS', count: 75},
                        {text: 'CARS ', count: 112},
                        {text: 'ANIMALS', count: 125},
                        {text: 'SCIENCE', count: 180},
                        {text: 'EUROPE', count: 225}
                    ]},
                    {text: 'ALBUQUERQUE JOURNAL', count: 2122, children: [
                        {text: 'BUSINESS', count: 22}, {text: 'LIVING PEOPLE', count: 2100}
                    ]},
                    {text: 'THE MIAMI HERALD', count: 2893, children: [
                        {text: 'BUSINESS', count: 2893}
                    ]},
                    {text: 'THE WASHINGTON POST', count: 5396, children: [
                        {text: 'BUSINESS', count: 5396}
                    ]},
                    {text: 'BBC', count: 8082, children: [
                        {text: 'BUSINESS', count: 8082}
                    ]},
                    {text: 'ABC NEWS', count: 8803, children: [
                        {text: 'BUSINESS', count: 8803}
                    ]}
                ]);
            });

            it('returns an empty array when given an empty array', function() {
                expect(DependentParametricCollection.prototype.parse([])).toEqual([]);
            });
        });
    });

});