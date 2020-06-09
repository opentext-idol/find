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
        { fact: { source: 'fact1' } },
        { fact: { source: 'fact2' } },
        { fact: { source: 'fact3' } }
    ]

    const FACTS_HTML = '' +
        '<a class="fact-sentence" data-factid="fact1">a fact</a>' +
        '<a class="fact-sentence" data-factid="fact2">a fact</a>';

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
            this.previewModeModel = new Backbone.Model({ mode: null });

            this.view = new FactsView({
                documentRenderer: this.documentRenderer,
                queryModel: this.queryModel,
                parametricCollection: this.parametricCollection,
                previewModeModel: this.previewModeModel,
            });

            spyOn(this.view.factsParametricCollection, 'fetchFromQueryModel');
            spyOn(this.view.entityFactCollection, 'fetch');
            spyOn(this.view.previewDocModel, 'fetch');

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

        expectLoading();

        it('should fetch parametric values', function () {
            const calls = this.view.factsParametricCollection.fetchFromQueryModel.calls;
            expect(calls.count()).toBe(1);
            expect(calls.mostRecent().args[0]).toBe(this.queryModel);
        });

        it('should not fetch facts', function () {
            expect(this.view.entityFactCollection.fetch.calls.count()).toBe(0);
        });

        describe('then the parametric collection syncs', function () {

            beforeEach(function () {
                this.parametricCollection.trigger('sync');
            });

            it('should fetch parametric values again', function () {
                const calls = this.view.factsParametricCollection.fetchFromQueryModel.calls;
                expect(calls.count()).toBe(2);
            });

        });

        describe('then the facts parametric collection syncs with entities', function () {

            beforeEach(function () {
                this.parametricCollection.trigger('sync');
                this.view.factsParametricCollection.add([ENTITIES_FIELD]);
                this.view.factsParametricCollection.trigger('sync');
            });

            it('entity selection should include all values', function () {
                const options = this.view.$('.facts-entity-selector option');
                expect(options.length).toBe(3);
                expect(options.eq(0).attr('value')).toBe('planet-jupiter');
                expect(options.eq(0).text()).toBe('Jupiter');
                expect(options.eq(1).attr('value')).toBe('planet-mars');
                expect(options.eq(1).text()).toBe('Mars');
                expect(options.eq(2).attr('value')).toBe('planet-saturn');
                expect(options.eq(2).text()).toBe('Saturn');
            });

            expectLoading();

            it('should fetch facts for the first entity', function () {
                const calls = this.view.entityFactCollection.fetch.calls;
                expect(calls.count()).toBe(1);
                expect(calls.mostRecent().args).toEqual([{
                    reset: true,
                    data: {
                        entity: 'planet-jupiter',
                        indexes: ['index1', 'index2'],
                        maxResults: 30
                    }
                }]);
            });

            // test transition: loading -> blank
            describe('then the facts parametric collection syncs with no entities', function () {

                beforeEach(function () {
                    this.view.factsParametricCollection.reset();
                    this.view.factsParametricCollection.trigger('sync');
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
                            data: {
                                entity: 'planet-mars',
                                indexes: ['index1', 'index2'],
                                maxResults: 30
                            }
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

                describe('then a fact is clicked', function () {

                    beforeEach(function () {
                        this.view.$('.fact-sentence').eq(1).click();
                    });

                    expectFacts();

                    it('should show the fact details', function () {
                        expect(this.previewModeModel.get('mode')).toBe('fact');
                        expect(this.previewModeModel.get('fact').toJSON())
                            .toEqual({ fact: { source: 'fact2' } });
                        expect(this.previewModeModel.get('factsView')).toBe(this.view);
                    });

                    it('should highlight the clicked fact', function () {
                        const facts = this.view.$('.fact-sentence');
                        expect(facts.eq(0).hasClass('selected-fact')).toBe(false);
                        expect(facts.eq(1).hasClass('selected-fact')).toBe(true);
                    });

                    describe('then a different fact is clicked', function () {

                        beforeEach(function () {
                            this.view.$('.fact-sentence').eq(0).click();
                        });

                        expectFacts();

                        it('should show the fact details', function () {
                            expect(this.previewModeModel.get('fact').toJSON())
                                .toEqual({ fact: { source: 'fact1' } });
                        });

                        it('should highlight the clicked fact', function () {
                            const facts = this.view.$('.fact-sentence');
                            expect(facts.eq(0).hasClass('selected-fact')).toBe(true);
                            expect(facts.eq(1).hasClass('selected-fact')).toBe(false);
                        });

                    });

                    describe('then the fact is clicked again', function () {

                        beforeEach(function () {
                            this.view.$('.fact-sentence').eq(1).click();
                        });

                        expectFacts();

                        it('should hide the fact details', function () {
                            expect(this.previewModeModel.get('mode')).toBe(null);
                        });

                        it('should de-highlight the clicked fact', function () {
                            const facts = this.view.$('.fact-sentence');
                            expect(facts.eq(0).hasClass('selected-fact')).toBe(false);
                            expect(facts.eq(1).hasClass('selected-fact')).toBe(false);
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

        describe('then the facts parametric collection sync fails', function () {

            beforeEach(function () {
                this.view.factsParametricCollection.trigger('error', null, {
                    status: 1,
                    responseJSON: { message: 'bad things', uuid: '123', isUserError: false }
                });
            });

            expectError();

        });

        describe('previewDoc', function () {

            beforeEach(function () {
                this.view.previewDoc('db', 'ref');
            });

            it('should fetch the document', function () {
                const calls = this.view.previewDocModel.fetch.calls;
                expect(calls.count()).toBe(1);
                expect(calls.mostRecent().args).toEqual([{
                    data: { database: 'db', reference: 'ref' }
                }]);
            });

            describe('then the preview model syncs', function () {

                beforeEach(function () {
                    this.view.previewDocModel.set('doc', 'to preview');
                    this.view.previewDocModel.trigger('sync');
                });

                it('should show the document preview', function () {
                    expect(this.previewModeModel.get('document').get('doc')).toEqual('to preview');
                    expect(this.previewModeModel.get('mode')).toBe('summary');
                })

                expectLoading(); // no change

            });

            describe('then the preview model sync fails', function () {

                beforeEach(function () {
                    this.view.previewDocModel.trigger('error', null, {
                        status: 1,
                        responseJSON: {
                            message: 'bad things',
                            uuid: '123',
                            isUserError: true
                        }
                    });
                });

                it('should display loading and error', function () {
                    expect(this.view.$('.facts-loading')).not.toHaveClass('hide'); // no change
                    expect(this.view.$('.facts-error')).not.toHaveClass('hide');
                    expect(this.view.$('.facts-empty')).toHaveClass('hide');
                    expect(this.view.$('.facts-list')).toHaveClass('hide');

                    expect(this.view.$('.facts-error').html())
                        .toContain(i18n['search.resultsView.facts.error.preview']);
                })

                describe('then the preview model syncs', function () {

                    beforeEach(function () {
                        this.view.previewDocModel.set('doc', 'to preview');
                        this.view.previewDocModel.trigger('sync');
                    });

                    expectLoading(); // error hidden

                });

                // test transition: preview error -> loading
                describe('then an entity is selected', function () {

                    beforeEach(function () {
                        this.view.$('.facts-entity-selector select')
                            .val('planet-mars').change();
                    });

                    it('should display loading and error', function () {
                        // no change
                        expect(this.view.$('.facts-loading')).not.toHaveClass('hide');
                        expect(this.view.$('.facts-error')).not.toHaveClass('hide');
                        expect(this.view.$('.facts-empty')).toHaveClass('hide');
                        expect(this.view.$('.facts-list')).toHaveClass('hide');
                    });

                });

            });

        });

    });

});
