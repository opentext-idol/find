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
    'find/app/page/search/template-helpers/i18n-helper',
    'i18n!find/nls/bundle',
    'handlebars'
], function(helper, i18n, Handlebars) {

    describe('Internationalisation helper', function() {
        beforeEach(function() {
            this.handlebars = Handlebars.create();
            this.handlebars.registerHelper('i18n', helper);
        });

        it('reads strings from the bundle', function() {
            const output = this.handlebars.compile('<p>{{i18n "app.search"}}</p>')({});
            expect(output).toBe('<p>' + i18n['app.search'] + '</p>');
        });
    });
});
