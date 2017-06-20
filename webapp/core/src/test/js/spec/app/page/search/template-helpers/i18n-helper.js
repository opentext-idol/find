/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
