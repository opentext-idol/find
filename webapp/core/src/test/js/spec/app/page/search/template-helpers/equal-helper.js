/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
