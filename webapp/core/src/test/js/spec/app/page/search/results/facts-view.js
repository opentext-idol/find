/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'find/app/page/search/results/facts-view',
    'jasmine-ajax'
], function($, Backbone, configuration, i18n, FactsView) {
    'use strict';

    const ENTITIES_FIELD = {
        type: 'Parametric',
        id: 'FACTS/FACT_EXTRACT_/ENTITIES/VALUE',
        values: [
            { value: 'planet-jupiter', displayValue: 'Jupiter', count: 14 },
            { value: 'planet-mars', displayValue: 'Mars', count: 3 },
            { value: 'planet-saturn', displayValue: 'Saturn', count: null }
        ]
    }

    const REPORT_FACTS = [
        {
            entityName: 'Entity 1',
            source: "doc-ref1",
            property: [{ name: 'prop 1', value: 'prop val 1', qualifier: [] }]
        },
        {
            entityName: 'Entity 2',
            source: "doc-ref2",
            property: [{ name: 'prop 3', value: 'prop val 2', qualifier: [] }]
        }
    ]

    const FACTS_HTML = '<a data-docref="clicked-docref">open document</a>';

    const expectBlank = function () {
        it('should not display anything', function () {
            expect(this.view.$('.facts-loading')).toHaveClass('hide');
            expect(this.view.$('.facts-error')).toHaveClass('hide');
            expect(this.view.$('.facts-empty')).toHaveClass('hide');
            expect(this.view.$('.facts-list')).toHaveClass('hide');
        });
    }

    const expectLoading = function () {
        it('should display loading spinner', function () {
            expect(this.view.$('.facts-loading')).not.toHaveClass('hide');
            expect(this.view.$('.facts-error')).toHaveClass('hide');
            expect(this.view.$('.facts-empty')).toHaveClass('hide');
            expect(this.view.$('.facts-list')).toHaveClass('hide');
        });
    }

    const expectError = function () {
        it('should display error', function () {
            expect(this.view.$('.facts-loading')).toHaveClass('hide');
            expect(this.view.$('.facts-error')).not.toHaveClass('hide');
            expect(this.view.$('.facts-empty')).toHaveClass('hide');
            expect(this.view.$('.facts-list')).toHaveClass('hide');
        });
    }

    const expectEmpty = function () {
        it('should display empty message', function () {
            expect(this.view.$('.facts-loading')).toHaveClass('hide');
            expect(this.view.$('.facts-error')).toHaveClass('hide');
            expect(this.view.$('.facts-empty')).not.toHaveClass('hide');
            expect(this.view.$('.facts-list')).toHaveClass('hide');
        });
    }

    const expectFacts = function () {
        it('should display facts', function () {
            expect(this.view.$('.facts-loading')).toHaveClass('hide');
            expect(this.view.$('.facts-error')).toHaveClass('hide');
            expect(this.view.$('.facts-empty')).toHaveClass('hide');
            expect(this.view.$('.facts-list')).not.toHaveClass('hide');
        });
    }

    describe('Facts View', function () {

        beforeEach(function () {
            configuration.and.returnValue({
                referenceField: 'docref'
            });

            this.documentRenderer = {
                renderEntityFacts: jasmine.createSpy().and.returnValue(FACTS_HTML)
            };
            this.queryModel = new Backbone.Model({ indexes: ['index1', 'index2'] });
            this.parametricCollection = new Backbone.Collection();
            this.previewModeModel = new Backbone.Model({ document: null });

            this.view = new FactsView({
                documentRenderer: this.documentRenderer,
                queryModel: this.queryModel,
                parametricCollection: this.parametricCollection,
                previewModeModel: this.previewModeModel,
            });

            spyOn(this.view.entityFactCollection, 'fetch');
            spyOn(this.view.previewDocCollection, 'fetch');

            $('body').append(this.view.$el);
            this.view.render();
        });

        afterEach(function () {
            this.view.remove();
        })

        it('entity selection should be empty', function () {
            const options = this.view.$('.facts-entity-selector option');
            expect(options.length).toBe(0);
        });

        expectBlank();

        it('should not fetch facts', function () {
            expect(this.view.entityFactCollection.fetch.calls.count()).toBe(0);
        });

        describe('then the parametric collection syncs with entities', function () {

            beforeEach(function () {
                this.parametricCollection.add([ENTITIES_FIELD]);
                this.parametricCollection.trigger('sync');
            });

            it('entity selection should include all values', function () {
                const options = this.view.$('.facts-entity-selector option');
                expect(options.length).toBe(3);
                expect(options.eq(0).attr('value')).toBe('planet-jupiter');
                expect(options.eq(0).text()).toBe('Jupiter (14)');
                expect(options.eq(1).attr('value')).toBe('planet-mars');
                expect(options.eq(1).text()).toBe('Mars (3)');
                expect(options.eq(2).attr('value')).toBe('planet-saturn');
                expect(options.eq(2).text()).toBe('Saturn');
            });

            expectLoading();

            it('should fetch facts for the first entity', function () {
                const calls = this.view.entityFactCollection.fetch.calls;
                expect(calls.count()).toBe(1);
                expect(calls.mostRecent().args).toEqual([{
                    reset: true,
                    data: { entity: 'planet-jupiter', indexes: ['index1', 'index2'] }
                }]);
            });

            // test transition: loading -> blank
            describe('then the parametric collection syncs with no entities', function () {

                beforeEach(function () {
                    this.parametricCollection.reset();
                    this.parametricCollection.trigger('sync');
                });

                it('entity selection should be empty', function () {
                    const options = this.view.$('.facts-entity-selector option');
                    expect(options.length).toBe(0);
                });

                expectBlank();

                it('should not fetch facts again', function () {
                    expect(this.view.entityFactCollection.fetch.calls.count()).toBe(1);
                });

            });

            describe('then the fact collection syncs with no facts', function () {

                beforeEach(function () {
                    this.view.entityFactCollection.trigger('sync');
                });

                expectEmpty();

                // test transition: empty -> loading
                describe('then an entity is selected', function () {

                    beforeEach(function () {
                        this.view.$('.facts-entity-selector select').val('planet-mars').change();
                    });

                    expectLoading();

                    it('should fetch facts for the selected entity', function () {
                        const calls = this.view.entityFactCollection.fetch.calls;
                        expect(calls.count()).toBe(2);
                        expect(calls.mostRecent().args).toEqual([{
                            reset: true,
                            data: { entity: 'planet-mars', indexes: ['index1', 'index2'] }
                        }]);
                    });

                });

            });

            describe('then the fact collection syncs with facts', function () {

                beforeEach(function () {
                    this.view.entityFactCollection.add(REPORT_FACTS);
                    this.view.entityFactCollection.trigger('sync');
                });

                expectFacts();

                it('facts list should contain facts', function () {
                    expect(this.view.$('.facts-list').html()).toBe(FACTS_HTML);
                });

                // test transition: facts -> loading
                describe('then an entity is selected', function () {

                    beforeEach(function () {
                        this.view.$('.facts-entity-selector select').val('planet-mars').change();
                    });

                    expectLoading();

                });

                describe('then the Open Document button is clicked', function () {

                    beforeEach(function () {
                        this.view.$('.facts-list [data-docref]').eq(0).click();
                    });

                    expectFacts();

                    it('should fetch the document', function () {
                        const calls = this.view.previewDocCollection.fetch.calls;
                        expect(calls.count()).toBe(1);
                        expect(calls.mostRecent().args).toEqual([{
                            reset: true,
                            data: {
                                indexes: ['index1', 'index2'],
                                max_results: 1,
                                text: '*',
                                field_text: 'MATCH{clicked-docref}:docref',
                                queryType: 'RAW',
                                summary: 'context',
                            }
                        }]);
                    });

                    describe('then the preview collection syncs with a document', function () {

                        beforeEach(function () {
                            this.view.previewDocCollection.add([{ doc: 'to preview' }]);
                            this.docModel = this.view.previewDocCollection.models[0];
                            this.view.previewDocCollection.trigger('sync');
                        });

                        it('should show the document preview', function () {
                            expect(this.previewModeModel.get('document')).toBe(this.docModel);
                            expect(this.previewModeModel.get('mode')).toBe('summary');
                        })

                        expectFacts();

                    });

                    describe('then the preview collection syncs with no document', function () {

                        beforeEach(function () {
                            this.view.previewDocCollection.trigger('sync');
                        });

                        it('should not show the document preview', function () {
                            expect(this.previewModeModel.get('document')).toBe(null);
                        })

                        expectFacts();

                    });

                    describe('then the preview collection sync fails', function () {

                        beforeEach(function () {
                            this.view.previewDocCollection.trigger('error', null, {
                                status: 1,
                                responseJSON: {
                                    message: 'bad things',
                                    uuid: '123',
                                    isUserError: true
                                }
                            });
                        });

                        it('should display facts and error', function () {
                            expect(this.view.$('.facts-loading')).toHaveClass('hide');
                            expect(this.view.$('.facts-error')).not.toHaveClass('hide');
                            expect(this.view.$('.facts-empty')).toHaveClass('hide');
                            expect(this.view.$('.facts-list')).not.toHaveClass('hide');

                            expect(this.view.$('.facts-error').html())
                                .toContain(i18n['search.resultsView.facts.error.preview']);
                        })

                        // test transition: preview error -> loading
                        describe('then an entity is selected', function () {

                            beforeEach(function () {
                                this.view.$('.facts-entity-selector select')
                                    .val('planet-mars').change();
                            });

                            expectLoading();

                        });

                        describe('then the Open Document button is clicked again', function () {

                            beforeEach(function () {
                                this.view.$('.facts-list [data-docref]').eq(0).click();
                                this.view.previewDocCollection.add([{ doc: 'to preview' }]);
                                this.docModel = this.view.previewDocCollection.models[0];
                                this.view.previewDocCollection.trigger('sync');
                            });

                            it('should show the document preview', function () {
                                expect(this.previewModeModel.get('document')).toBe(this.docModel);
                                expect(this.previewModeModel.get('mode')).toBe('summary');
                            })

                            expectFacts();

                        });

                    });

                });

            });

            describe('then the facts collection sync fails', function () {

                beforeEach(function () {
                    this.view.entityFactCollection.trigger('error', null, {
                        status: 1,
                        responseJSON: { message: 'bad things', uuid: '123', isUserError: false }
                    });
                });

                expectError();

                it('should show the error message', function () {
                    expect(this.view.$('.facts-error').html())
                        .toContain(i18n['search.resultsView.facts.error.facts']);
                })

                // test transition: error -> loading
                describe('then an entity is selected', function () {

                    beforeEach(function () {
                        this.view.$('.facts-entity-selector select').val('planet-mars').change();
                    });

                    expectLoading();

                });

            })

            // eg. new fetch started
            describe('then the facts collection sync is aborted', function () {

                beforeEach(function () {
                    this.view.entityFactCollection.trigger('error', null, {
                        status: 0,
                        statusText: 'abort'
                    });
                });

                expectLoading();

            })

            // eg. backend inaccessible
            describe('then the facts collection sync fails with no response', function () {

                beforeEach(function () {
                    this.view.entityFactCollection.trigger('error', null, { status: 1 });
                });

                expectError();

                it('should show the error message', function () {
                    expect(this.view.$('.facts-error').html())
                        .toContain(i18n['search.resultsView.facts.error.facts']);
                })

            })

        });

    });

});
