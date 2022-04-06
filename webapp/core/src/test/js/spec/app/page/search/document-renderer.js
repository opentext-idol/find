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
    'find/app/page/search/document-renderer',
    'backbone',
    'moment',
    'jquery',
    'test-util/promise-spy',
    'i18n!find/nls/bundle'
], function(DocumentRenderer, Backbone, moment, $, promiseSpy, i18n) {

    const originalGet = $.get;

    function buildDocument(attributeOverrides) {
        return new Backbone.Model(_.extend({
            contentType: 'text/plain',
            date: moment(1495708119912),
            index: 'Docs',
            reference: '20ea90fb-9c50-4032-a6bc-04bebd81d70e',
            promotionName: null,
            summary: 'summary',
            thumbnail: null,
            thumbnailUrl: null,
            title: 'Title',
            url: null,
            fields: []
        }, attributeOverrides));
    }

    describe('DocumentRenderer', function() {
        beforeEach(function() {
            $.get = promiseSpy('$.get');

            this.documentRenderer = new DocumentRenderer({
                searchResult: [
                    {
                        file: 'army-doctor.html',
                        triggers: [
                            {field: 'CATEGORY', values: ['PERSON']},
                            {field: 'PROFESSION', values: ['DOCTOR', 'PSYCHIATRIST']}
                        ]
                    },
                    {
                        file: 'scientist-lords.html',
                        triggers: [
                            {indexes: ['KNIGHTS', 'LORDS'], field: 'PRESIDENT', values: ['true']}
                        ]
                    },
                    {
                        file: 'lords.html',
                        triggers: [
                            {indexes: ['KNIGHTS', 'LORDS']}
                        ]
                    },
                    {
                        file: 'person.html',
                        triggers: [
                            {field: 'CATEGORY', values: ['PERSON']}
                        ]
                    }
                ],
                promotion: [
                    {
                        file: 'promotion.html',
                        triggers: []
                    }
                ],
                previewPanel: [],
                entitySearch: []
            });
        });

        afterEach(function() {
            $.get = originalGet;
        });

        it('fetches configured templates on construction', function() {
            expect($.get).toHaveBeenCalledWith(jasmine.any(String));
        });

        it('exposes an unresolved load promise', function() {
            expect(this.documentRenderer.loadPromise.state()).toBe('pending');
        });

        describe('when the template fetch fails', function() {
            beforeEach(function() {
                $.get.promises[0].reject({
                    message: 'Something went wrong'
                });
            });

            it('the load promise is rejected', function() {
                expect(this.documentRenderer.loadPromise.state()).toBe('rejected');
            });
        });

        describe('when the template fetch succeeds', function() {
            beforeEach(function() {
                $.get.promises[0].resolve({
                    'army-doctor.html': '<h1>Dr {{title}}</h1>',
                    'person.html': '<h1>{{getFieldValue "NAME_TITLE"}} {{title}}</h1>',
                    'lords.html': '<h1>Sir {{title}}</h1>',
                    'scientist-lords.html': '<h1>Sir {{title}} PRS</h1>',
                    'promotion.html': '<h1 class="shiny">{{title}}</h1>'
                });
            });

            it('the load promise is resolved', function() {
                expect(this.documentRenderer.loadPromise.state()).toBe('resolved');
            });

            it('renders a search result matching multiple triggers', function() {
                const document = buildDocument({
                    title: 'Bob',
                    index: 'people',
                    fields: [
                        {id: 'CATEGORY', values: ['PERSON', 'CELEBRITIES']},
                        {id: 'PROFESSION', values: ['DOCTOR', 'ARMY']}
                    ]
                });

                const output = this.documentRenderer.renderResult(document);
                expect(output).toBe('<h1>Dr Bob</h1>');
            });

            it('renders a search result matching one trigger', function() {
                const document = buildDocument({
                    title: 'Julie',
                    index: 'people',
                    fields: [
                        {id: 'CATEGORY', values: ['PERSON']},
                        {id: 'PROFESSION', values: ['FLORIST']},
                        {id: 'NAME_TITLE', values: ['Mrs']}
                    ]
                });

                const output = this.documentRenderer.renderResult(document);
                expect(output).toBe('<h1>Mrs Julie</h1>');
            });

            it('renders a search result matching a database and field trigger', function() {
                const document = buildDocument({
                    title: 'Isaac Newton',
                    index: 'lords',
                    fields: [
                        {id: 'CATEGORY', values: ['PERSON']},
                        {id: 'PRESIDENT', values: ['true']}
                    ]
                });

                const output = this.documentRenderer.renderResult(document);
                expect(output).toBe('<h1>Sir Isaac Newton PRS</h1>');
            });

            it('renders a search result matching a database trigger', function() {
                const document = buildDocument({
                    title: 'Walter Raleigh',
                    index: 'lords',
                    fields: [
                        {id: 'CATEGORY', values: ['PERSON']}
                    ]
                });

                const output = this.documentRenderer.renderResult(document);
                expect(output).toBe('<h1>Sir Walter Raleigh</h1>');
            });

            it('falls back to the default template if no configured search result templates match', function () {
                const reference = '6dffe31c-c12d-49fd-bd20-f7e1c47ab4ea';

                const document = buildDocument({
                    reference: reference,
                    summary: 'Drugs make people better',
                    title: 'Drugs',
                    fields: [
                        {id: 'CATEGORY', values: ['FACTS']}
                    ]
                });

                const output = this.documentRenderer.renderResult(document);
                expect(output).toContain(reference);
                expect(output).toContain('Drugs make people better');
            });

            it('renders a promotion result matching the empty array of triggers', function() {
                const document = buildDocument({
                    title: 'Drugs',
                    fields: [
                        {id: 'CATEGORY', values: ['FACTS']}
                    ]
                });

                const output = this.documentRenderer.renderPromotion(document);
                expect(output).toBe('<h1 class="shiny">Drugs</h1>');
            });

            describe('renderPreviewMetadata', function () {

                it('renders the default template if not overridden', function() {
                    const document = buildDocument({
                        title: 'Drugs',
                        fields: [
                            {id: 'CATEGORY', values: ['FACTS'], advanced: false},
                            {id: 'AUTHOR', values: ['GOVERNMENT'], advanced: false}
                        ]
                    });

                    const output = this.documentRenderer.renderPreviewMetadata(document);
                    expect(output).toContain('GOVERNMENT');
                    expect(output).toContain('FACTS');
                });

                it('displays record fields as JSON', function() {
                    const document = buildDocument({
                        title: 'With Record',
                        fields: [
                            { id: 'RECORD', values: [{ the: 'record value' }], advanced: false }
                        ]
                    });

                    const output = this.documentRenderer.renderPreviewMetadata(document);
                    expect(output).toContain('{&quot;the&quot;:&quot;record value&quot;}');
                });

            });

            describe('renderDocumentFacts', function () {

                it('renders the default template if not overridden', function() {
                    const fact1 = { fact_extract_: [{
                        date: ['2001-02-03'],
                        entities: [
                            { '@type': ['weather'], value: ['raining', 'snowing'] },
                            { '@type': ['planet'], value: ['Mars'] }
                        ],
                        property: ['spokesperson'],
                        sentence: ['This is a fact'],
                        type: ['position'],
                        value: ['Some Guy']
                    }] }

                    const fact2 = { fact_extract_: [{
                            date: ['2003-02-01'],
                            entities: [{ '@type': ['altitude'], value: ['high'] }],
                            property: ['representative'],
                            sentence: ['Another fact'],
                            type: ['position'],
                            value: ['Same Person']
                        }] }

                    const document = buildDocument({
                        fields: [{ id: 'facts', values: [fact1, fact2], advanced: false }]
                    });

                    const output = this.documentRenderer.renderDocumentFacts(document);

                    expect(output).toContain('<p>This is a fact</p>');
                    expect(output).toContain('<tr><td>Spokesperson</td><td>Some Guy</td></tr>');
                    expect(output).toContain('<tr><td>Weather</td><td>raining</td></tr>');
                    expect(output).toContain('<tr><td>Weather</td><td>snowing</td></tr>');
                    expect(output).toContain('<tr><td>Planet</td><td>Mars</td></tr>');
                    expect(output).toContain('<tr><td>Date</td><td>2001-02-03</td></tr>');

                    expect(output).toContain('<p>Another fact</p>');
                    expect(output).toContain('<tr><td>Representative</td><td>Same Person</td></tr>');
                    expect(output).toContain('<tr><td>Altitude</td><td>high</td></tr>');
                    expect(output).toContain('<tr><td>Date</td><td>2003-02-01</td></tr>');
                });

            });

            const ENTITY_FACTS = [
                {
                    fact: {
                        entityName: 'Some Guy',
                        source: 'fact1',
                        property: [
                            { name: 'position', value: 'representative', qualifier: [
                                    { name: 'date', value: '2003-02-01' },
                                    { name: 'weather', value: 'raining' }
                                ] },
                            { name: 'position', value: 'spokesperson', qualifier: [
                                    { name: 'date', value: '2001-02-03' },
                                    { name: 'weather', value: 'snowing' }
                                ] }
                        ]
                    },
                    documents: [
                        { index: 'db1', reference: 'doc-ref1', sentence: 'source 1' },
                        { index: 'db2', reference: 'doc-ref2', sentence: 'source 2' }
                    ]
                },

                {
                    fact: {
                        entityName: 'Same Person',
                        source: 'fact2',
                        property: [
                            { name: 'position', value: 'nowhere', qualifier: [
                                    { name: 'date', value: '2004-05-06' },
                                    { name: 'altitude', value: 'high' }
                                ] }
                        ]
                    },
                    documents: [
                        { index: 'db1', reference: 'doc-ref3', sentence: 'source 3' }
                    ]
                }
            ]

            describe('renderEntityFacts', function () {

                it('renders the default template if not overridden', function() {
                    const document = buildDocument({ facts: ENTITY_FACTS });
                    const output = this.documentRenderer.renderEntityFacts(document);

                    expect(output).toContain('data-factid="fact1">source 1</li>');
                    expect(output).toContain('data-factid="fact2">source 3</li>');
                });

            });

            describe('renderEntityFactsDetail', function () {

                it('renders the default template if not overridden', function() {
                    const document = buildDocument({ facts: ENTITY_FACTS });
                    const output = this.documentRenderer.renderEntityFactsDetail(document);

                    expect(output).toContain('<tr><td><strong>' +
                        i18n['search.resultsView.facts.facts.entityName'] +
                        '</strong></td><td>Some Guy</td></tr>');
                    expect(output).toContain('<tr><td><strong>Position</strong></td><td>spokesperson</td></tr>');
                    expect(output).toContain('<tr><td>Date</td><td>2001-02-03</td></tr>');
                    expect(output).toContain('<tr><td>Weather</td><td>raining</td></tr>');
                    expect(output).toContain('<tr><td><strong>Position</strong></td><td>representative</td></tr>');
                    expect(output).toContain('<tr><td>Date</td><td>2003-02-01</td></tr>');
                    expect(output).toContain('<tr><td>Weather</td><td>snowing</td></tr>');
                    expect(output).toContain('>source 1</p>');
                    expect(output).toContain('<a data-docindex="db1" data-docref="doc-ref1" target="_blank">doc-ref1</a>');
                    expect(output).toContain('>source 2</p>');
                    expect(output).toContain('<a data-docindex="db2" data-docref="doc-ref2" target="_blank">doc-ref2</a>');

                    expect(output).toContain('<tr><td><strong>' +
                        i18n['search.resultsView.facts.facts.entityName'] +
                        '</strong></td><td>Same Person</td></tr>');
                    expect(output).toContain('<tr><td><strong>Position</strong></td><td>nowhere</td></tr>');
                    expect(output).toContain('<tr><td>Date</td><td>2004-05-06</td></tr>');
                    expect(output).toContain('<tr><td>Altitude</td><td>high</td></tr>');
                    expect(output).toContain('>source 3</p>');
                    expect(output).toContain('<a data-docindex="db1" data-docref="doc-ref3" target="_blank">doc-ref3</a>');
                });

            });

        });
    });

});
