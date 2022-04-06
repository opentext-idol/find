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
    'find/app/page/search/template-helpers/get-field-value-helper',
    'handlebars'
], function(helper, Handlebars) {

    describe('Get field value helper', function() {
        beforeEach(function() {
            const handlebars = Handlebars.create();
            handlebars.registerHelper('getFieldValue', helper);
            this.template = handlebars.compile('<p>{{getFieldValue "animal"}}</p>');
        });

        it('renders the first field value if the field is present', function() {
            const output = this.template({
                fields: [
                    {id: 'animal', displayName: 'Animal', values: ['Cat', 'Dog']}
                ]
            });

            expect(output).toBe('<p>Cat</p>');
        });

        it('renders nothing if the field is not present', function() {
            const output = this.template({
                fields: [
                    {id: 'category', displayName: 'Category', values: ['animal']}
                ]
            });

            expect(output).toBe('<p></p>');
        });
    });

});
