/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/configuration',
    'find/app/model/document-selection-model'
], function (configuration, DocumentSelectionModel) {
    'use strict';

    describe('DocumentSelectionModel', function () {

        beforeEach(function () {
            configuration.and.callFake(function () {
                return {
                    referenceField: 'CUSTOMREF'
                };
            });
        });

        describe('constructor', function () {

            it('default should be an empty blacklist', function () {
                const model = new DocumentSelectionModel();
                expect(model.get('isWhitelist')).toBe(false);
                expect(model.get('references')).toEqual({});
                expect(model.changedReferences).toBeNull();
            });

            it('initial values should be used', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: true,
                    references: { a: true, b: true, c: true }
                });
                expect(model.get('isWhitelist')).toBe(true);
                expect(model.get('references')).toEqual({ a: true, b: true, c: true });
            });

            it('should initially indicate all documents changed', function () {
                const model = new DocumentSelectionModel();
            });

        });

        describe('getReferencesCount', function () {

            it('should return number of documents', function () {
                expect(new DocumentSelectionModel({
                    references: ['a', 'b', 'c']
                }).getReferencesCount()).toBe(3);
            });

            it('should return 0 with no documents', function () {
                expect(new DocumentSelectionModel().getReferencesCount()).toBe(0);
            });

        });

        describe('getReferences', function () {

            it('should return references array', function () {
                const refs = new DocumentSelectionModel({
                    references: ['a', 'b', 'c']
                }).getReferences();

                expect(refs.length).toEqual(3);
                expect(refs).toContain('a');
                expect(refs).toContain('b');
                expect(refs).toContain('c');
            });

        });

        describe('isDefault', function () {

            it('should return true with default value', function () {
                expect(new DocumentSelectionModel().isDefault()).toBe(true);
            });

            it('should return true with provided empty blacklist', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: false,
                    references: []
                }).isDefault()).toBe(true);
            });

            it('should return true with whitelist', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: true
                }).isDefault()).toBe(false);
            });

            it('should return true with documents', function () {
                expect(new DocumentSelectionModel({
                    references: ['a', 'b', 'c']
                }).isDefault()).toBe(false);
            });

        });

        describe('isSelected', function () {

            it('should return true with blacklist with document not listed', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: false,
                    references: ['a', 'b', 'c']
                }).isSelected('d')).toBe(true);
            });

            it('should return false with blacklist with document listed', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: false,
                    references: ['a', 'b', 'c']
                }).isSelected('c')).toBe(false);
            });

            it('should return false with whitelist with document not listed', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: true,
                    references: ['a', 'b', 'c']
                }).isSelected('d')).toBe(false);
            });

            it('should return true with whitelist with document listed', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: true,
                    references: ['a', 'b', 'c']
                }).isSelected('c')).toBe(true);
            });

        });

        describe('toFieldText', function () {

            it('should return null with empty blacklist', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: false,
                    references: []
                }).toFieldText()).toBeNull();
            });

            it('should return NOT MATCH expression with blacklist', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: false,
                    references: ['a', 'b,c', 'd']
                }).toFieldText().toString()).toBe('NOT MATCH{a,b%2Cc,d}:CUSTOMREF');
            });

            it('should return MATCH expression with empty whitelist', function () {
                const result1 = new DocumentSelectionModel({
                    isWhitelist: true,
                    references: []
                }).toFieldText().toString();
                const result2 = new DocumentSelectionModel({
                    isWhitelist: true,
                    references: []
                }).toFieldText().toString();

                expect(result1).toMatch(/MATCH\{.+\}:CUSTOMREF/);
                expect(result2).toMatch(/MATCH\{.+\}:CUSTOMREF/);
                expect(result1).not.toBe(result2);
            });

            it('should return MATCH expression with whitelist', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: true,
                    references: ['a', 'b,c', 'd']
                }).toFieldText().toString()).toBe('MATCH{a,b%2Cc,d}:CUSTOMREF');
            });

        });

        describe('describe', function () {

            it('should say all with empty blacklist', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: false,
                    references: []
                }).describe()).toBe('All documents selected');
            });

            it('should count documents with blacklist', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: false,
                    references: ['a', 'b', 'c']
                }).describe()).toBe('Documents excluded: 3');
            });

            it('should say none with empty whitelist', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: true,
                    references: []
                }).describe()).toBe('No documents selected');
            });

            it('should count documents with whitelist', function () {
                expect(new DocumentSelectionModel({
                    isWhitelist: true,
                    references: ['a', 'b', 'c']
                }).describe()).toBe('Documents selected: 3');
            });

        });

        describe('select', function () {

            it('should select with blacklist', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: false,
                    references: ['a']
                });
                model.select('a');
                expect(model.isSelected('a')).toBe(true);
                expect(model.changedReferences).toEqual({ a: true });
            });

            it('should do nothing with blacklist if already selected', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: false,
                    references: []
                });
                model.select('a');
                expect(model.isSelected('a')).toBe(true);
                expect(model.changedReferences).toEqual({ a: true });
            });

            it('should select with whitelist', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: true,
                    references: []
                });
                model.select('a');
                expect(model.isSelected('a')).toBe(true);
                expect(model.changedReferences).toEqual({ a: true });
            });

            it('should do nothing with whitelist if already selected', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: true,
                    references: ['a']
                });
                model.select('a');
                expect(model.isSelected('a')).toBe(true);
                expect(model.changedReferences).toEqual({ a: true });
            });

        });

        describe('exclude', function () {

            it('should exclude with blacklist', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: false,
                    references: []
                });
                model.exclude('a');
                expect(model.isSelected('a')).toBe(false);
                expect(model.changedReferences).toEqual({ a: true });
            });

            it('should do nothing with blacklist if already excluded', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: false,
                    references: ['a']
                });
                model.exclude('a');
                expect(model.isSelected('a')).toBe(false);
                expect(model.changedReferences).toEqual({ a: true });
            });

            it('should exclude with whitelist', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: true,
                    references: ['a']
                });
                model.exclude('a');
                expect(model.isSelected('a')).toBe(false);
                expect(model.changedReferences).toEqual({ a: true });
            });

            it('should do nothing with whitelist if already excluded', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: true,
                    references: []
                });
                model.exclude('a');
                expect(model.isSelected('a')).toBe(false);
                expect(model.changedReferences).toEqual({ a: true });
            });

        });

        describe('reset', function () {

            it('should become empty blacklist', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: true,
                    references: ['a', 'b', 'c']
                });
                model.reset();
                expect(model.get('isWhitelist')).toBe(false);
                expect(model.get('references')).toEqual({});
                expect(model.changedReferences).toBeNull();
            });

        });

        describe('selectAll', function () {

            it('should become empty blacklist', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: true,
                    references: ['a', 'b', 'c']
                });
                model.selectAll();
                expect(model.get('isWhitelist')).toBe(false);
                expect(model.get('references')).toEqual({});
                expect(model.changedReferences).toBeNull();
            });

        });

        describe('excludeAll', function () {

            it('should become empty whitelist', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: false,
                    references: ['a', 'b', 'c']
                });
                model.excludeAll();
                expect(model.get('isWhitelist')).toBe(true);
                expect(model.get('references')).toEqual({});
                expect(model.changedReferences).toBeNull();
            });

        });

        describe('setFromSavedSearch', function () {

            it('should use values from saved search', function () {
                const model = new DocumentSelectionModel({
                    isWhitelist: false,
                    references: ['a', 'b']
                });
                const savedSearchModel = {
                    toDocumentSelectionModelAttributes: () => ({
                        isWhitelist: true,
                        references: ['c', 'd']
                    })
                };

                model.setFromSavedSearch(savedSearchModel);
                expect(model.get('isWhitelist')).toBe(true);
                expect(model.get('references')).toEqual({ c: true, d: true });
                expect(model.changedReferences).toBeNull();
            });

        });

    });

});
