/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/results/result-rendering/result-renderer',
    'backbone'
], function (ResultRenderer, Backbone) {

    /*
     * This config array represents how to configure the results renderer to render document models
     * into a particular template for the specific example where they present with a particular type of
     * field in the model's fields attribute.
     */
    var organismsConfig = [
        {
            template: _.template('<div>This is a document about trees</div>'),
            data: _.constant({}),
            predicate: function(model) {
                return Boolean(_.findWhere(model.get('fields'), {id: 'trees'}))
            }
        }, {
            template: _.template('<div>I have some <%-data.organismType%>: <%-data.organismValues%></div>'),
            data: function(model) {
                var organismField = _.find(model.get('fields'), function(field) {
                    return field.id === 'plants' || field.id === 'fungi'
                });

                return {
                    organismType: organismField.id,
                    organismValues: organismField.values.join(', ')
                }
            },
            predicate: function(model) {
                return Boolean(_.find(model.get('fields'), function(field) {
                    return field.id === 'plants' || field.id === 'fungi'
                }))
            }
        }, {
            template: _.template('<div>I got nothing</div>'),
            data: _.constant({}),
            predicate: _.constant(true)
        }

    ];

    describe('Result Renderer', function() {
        beforeEach(function() {
            this.resultRenderer = new ResultRenderer({
                config: organismsConfig
            });
        });

        it('should render the correct template for a given model', function() {
            var defaultTemplateResult = this.resultRenderer.getResult(new Backbone.Model({
                fields: [{
                    id: 'planets',
                    values: ['mars', 'venus']
                }, {
                    id: 'location',
                    values: ['solar system']
                }]
            }));

            expect(defaultTemplateResult).toBe('<div>I got nothing</div>');

            var treesTemplateResult = this.resultRenderer.getResult(new Backbone.Model({
                fields: [{
                    id: 'plants',
                    values: ['oak', 'willow', 'redwood']
                }, {
                    id: 'trees',
                    values: ['oak', 'redwood', 'willow']
                }]
            }));

            expect(treesTemplateResult).toBe('<div>This is a document about trees</div>');
        });

        it('should call the correct template with the correct additional data', function() {
            var additionalDataResult = this.resultRenderer.getResult(new Backbone.Model({
                fields: [{
                    id: 'plants',
                    values: ['oak', 'poppy', 'onion']
                }, {
                    id: 'author',
                    values: ['Entropy']
                }]
            }));

            expect(additionalDataResult).toBe('<div>I have some plants: oak, poppy, onion</div>');
        })
    })
});