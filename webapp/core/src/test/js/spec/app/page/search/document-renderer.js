/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/document-renderer',
    'backbone',
    'moment',
    'jquery',
    'test-util/promise-spy'
], function(DocumentRenderer, Backbone, moment, $, promiseSpy) {

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

        });
    });

});
