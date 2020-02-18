/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the &quot;License&quot;); you may not use this file except in compliance with the License.
 */

define([
    'handlebars',
    'find/app/page/search/template-helpers/json-stringify-helper'
], function (Handlebars, jsonStringifyHelper) {

    describe('Json Stringify helper', function() {

        beforeEach(function() {
            const handlebars = Handlebars.create();
            handlebars.registerHelper('jsonStringify', jsonStringifyHelper);
            this.template = handlebars.compile('<div>{{jsonStringify value}}</div>')
        });

        it('produces JSON', function() {
            expect(this.template({ value: { some: 'object', num: 123 } }))
                .toBe('<div>{&quot;some&quot;:&quot;object&quot;,&quot;num&quot;:123}</div>');
        });

    });

});
