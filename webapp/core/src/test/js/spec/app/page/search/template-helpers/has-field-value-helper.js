/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/template-helpers/has-field-value-helper',
    'handlebars'
], function(helper, handlebars) {

    describe('Has field with value helper', function() {
        beforeEach(function() {
            this.handlebars = handlebars.create();
            this.handlebars.registerHelper('hasFieldValue', helper);
        });

        it('renders the containing block if the document has the required value', function() {
            const templateSource = '<div>{{#hasFieldValue "animal" "DOG"}}<h1>{{title}}</h1>{{/hasFieldValue}}</div>';
            const templateFunction = this.handlebars.compile(templateSource);

            const output = templateFunction({
                title: 'Rover',
                fields: [
                    {id: 'animal', displayName: 'Animal', values: ['DOG'], advanced: true}
                ]
            });

            expect(output).toEqual('<div><h1>Rover</h1></div>');
        });

        it('does not render the containing block if the document does not have the field', function() {
            const templateSource = '<div>{{#hasFieldValue "animal" "DOG"}}<h1>{{title}}</h1>{{/hasFieldValue}}</div>';
            const templateFunction = this.handlebars.compile(templateSource);

            const output = templateFunction({
                title: 'Harry Potter',
                fields: [
                    {id: 'category', displayName: 'Category', values: ['DOG'], advanced: true}
                ]
            });

            expect(output).toEqual('<div></div>');
        });

        it('does not render the containing block if the document does not have the value', function() {
            const templateSource = '<div>{{#hasFieldValue "animal" "DOG"}}<h1>{{title}}</h1>{{/hasFieldValue}}</div>';
            const templateFunction = this.handlebars.compile(templateSource);

            const output = templateFunction({
                title: 'Harry Potter',
                fields: [
                    {id: 'category', displayName: 'Category', values: ['BOOK'], advanced: true},
                    {id: 'animal', displayName: 'Animal', values: ['CAT'], advanced: true}
                ]
            });

            expect(output).toEqual('<div></div>');
        });

        it('renders the inverse if the document does not have the value', function() {
            const templateSource = '<div>{{#hasFieldValue "animal" "CAT"}}<h1>{{title}}</h1>{{else}}<h1>{{reference}}</h1>{{/hasFieldValue}}</div>';
            const templateFunction = this.handlebars.compile(templateSource);

            const output = templateFunction({
                title: 'Harry Potter',
                reference: 'harry-potter',
                fields: [
                    {id: 'animal', displayName: 'Animal', values: ['DOG'], advanced: true}
                ]
            });

            expect(output).toEqual('<div><h1>harry-potter</h1></div>');
        });
    });

});
