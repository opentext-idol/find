/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/related-users-collection'
], function (RelatedUsersCollection) {
    'use strict';

    describe('Related Users Collection', function () {

        describe('parse', function () {

            it('should parse response correctly', function () {
                const collection = new RelatedUsersCollection([
                    { expert: true, user: { uid: 123, username: 'user A' } },
                    { expert: false, user: { uid: 7, username: 'user B' } },
                    { expert: true, user: { uid: 13, username: 'user C' } }
                ], { parse: true });

                expect(collection.size()).toBe(3);
                expect(collection.models[0].get('expert')).toBe(true);
                expect(collection.models[0].get('uid')).toBe(123);
                expect(collection.models[0].get('username')).toBe('user A');
                expect(collection.models[1].get('expert')).toBe(false);
                expect(collection.models[1].get('uid')).toBe(7);
                expect(collection.models[1].get('username')).toBe('user B');
                expect(collection.models[2].get('expert')).toBe(true);
                expect(collection.models[2].get('uid')).toBe(13);
                expect(collection.models[2].get('username')).toBe('user C');
            });

        });

    });

});
