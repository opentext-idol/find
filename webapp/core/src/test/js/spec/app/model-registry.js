/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'find/app/model-registry',
    'backbone'
], function(ModelRegistry, Backbone) {
    'use strict';

    describe('ModelRegistry', function() {
        beforeEach(function() {
            this.syncSpy = jasmine.createSpy('personModelSync');

            this.PersonModel = Backbone.Model.extend({
                defaults: {age: 32},
                sync: this.syncSpy
            });

            this.registry = new ModelRegistry({
                personModel: {
                    Constructor: this.PersonModel,
                    fetchOptions: {myOption: 'my-value'}
                }
            });
        });

        it('constructs and returns an instance of the given constructor', function() {
            expect(this.registry.get('personModel') instanceof this.PersonModel).toBeTruthy();
        });

        it('returns the same instance every time', function() {
            var model1 = this.registry.get('personModel');
            var model2 = this.registry.get('personModel');

            expect(model1).toBe(model2);
        });

        it('calls fetch on the model, passing the configured fetch options', function() {
            var model = this.registry.get('personModel');

            expect(this.syncSpy).toHaveBeenCalledWith('read', model, jasmine.any(Object));
            expect(this.syncSpy.calls.argsFor(0)[2].myOption).toBe('my-value');
        });
    });
});
