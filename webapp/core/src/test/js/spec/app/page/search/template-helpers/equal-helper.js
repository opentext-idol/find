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
    'find/app/page/search/template-helpers/equal-helper',
    'handlebars'
], function(helper, Handlebars) {

    describe('Equal helper', function() {
        beforeEach(function() {
            const handlebars = Handlebars.create();
            handlebars.registerHelper('equal', helper);
            this.template = handlebars.compile('<div>{{#equal animal "cat"}}Cat!{{/equal}}</div>')
        });

        it('renders the block if the values are equal', function() {
            const output = this.template({animal: 'cat'});
            expect(output).toBe('<div>Cat!</div>');
        });

        it('does not render the block if the values are not equal', function() {
            const output = this.template({animal: 'dog'});
            expect(output).toBe('<div></div>');
        });
    });

});
