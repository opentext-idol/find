/*
 * Copyright 2020 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
