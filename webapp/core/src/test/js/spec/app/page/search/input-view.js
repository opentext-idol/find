/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'find/app/page/search/input-view',
    'find/app/page/search/input-view-concept-strategy',
    'find/app/page/search/input-view-query-text-strategy'
], function(Backbone, $, InputView, conceptStrategy, queryTextStrategy) {
    "use strict";

    describe('Input view', function() {
        const model = new Backbone.Model({inputText: 'cat'});
        const collection = new Backbone.Collection([{concepts: ['cat']}]);
        const configurations = [{
            description: 'using query text strategy',
            options: {
                strategy: queryTextStrategy(model)
            },
            expectations: {
                initialText: 'cat',
                changedModel: 'dog',
                onModelUpdate: $.noop
            },
            changeModel: function () {
                model.set('inputText', 'dog');
            },
            getFirstValue: function () {
                return model.get('inputText');
            }
        }, {
            description: 'using concept strategy',
            options: {
                strategy: conceptStrategy(collection)
            },
            expectations: {
                initialText: '',
                changedModel: '',
                onModelUpdate: function () {
                    expect(collection.length).toBeGreaterThan(1);
                }
            },
            changeModel: function () {
                collection.unshift({concepts: ['dog']});
            },
            getFirstValue: function () {
                return collection.first().get('concepts')[0];
            }
        }];
        
        configurations.forEach(function (configuration) {
            describe(configuration.description, function () {
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
