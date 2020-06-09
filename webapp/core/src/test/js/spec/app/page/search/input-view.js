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
    'backbone',
    'jquery',
    'find/app/page/search/input-view',
    'find/app/page/search/input-view-concept-strategy',
    'find/app/page/search/input-view-query-text-strategy'
], function(Backbone, $, InputView, conceptStrategy, queryTextStrategy) {
    'use strict';

    describe('Input view', function() {
        jasmine.getEnv().configure({ random: false });
        const model = new Backbone.Model({inputText: 'cat'});
        const collection = new Backbone.Collection([{concepts: ['cat']}]);

        const configurations = [{
            description: 'using query text strategy',
            options: {
                enableTypeAhead: true,
                strategy: queryTextStrategy(model)
            },
            expectations: {
                initialText: 'cat',
                changedModel: 'dog',
                onModelUpdate: $.noop
            },
            changeModel: function() {
                model.set('inputText', 'dog');
            },
            getFirstValue: function() {
                return model.get('inputText');
            }
        }, {
            description: 'using concept strategy',
            options: {
                enableTypeAhead: true,
                strategy: conceptStrategy(collection)
            },
            expectations: {
                initialText: '',
                changedModel: '',
                onModelUpdate: function() {
                    expect(collection.length).toBeGreaterThan(1);
                }
            },
            changeModel: function() {
                collection.unshift({concepts: ['dog']});
            },
            getFirstValue: function() {
                return collection.first().get('concepts')[0];
            }
        }];

        configurations.forEach(function(configuration) {
            describe(configuration.description, function() {
                beforeEach(function() {
                    this.view = new InputView(configuration.options);
                    this.view.render();
                });

                it('displays the initial search text', function() {
                    expect(this.view.$('.find-input').typeahead('val')).toBe(configuration.expectations.initialText);
                });

                it('updates the text when the model changes', function() {
                    configuration.changeModel();

                    expect(this.view.$('.find-input').typeahead('val')).toBe(configuration.expectations.changedModel);
                });

                it('updates the model when the input is changed', function() {
                    this.view.$('.find-input').typeahead('val', 'dog');
                    this.view.$('.find-form').submit();

                    expect(configuration.getFirstValue()).toBe('dog');
                    configuration.expectations.onModelUpdate();
                });
            });
        });
    });
});
