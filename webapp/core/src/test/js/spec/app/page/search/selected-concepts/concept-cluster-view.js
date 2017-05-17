/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/selected-concepts/concept-cluster-view'
], function(Backbone, ConceptClusterView) {
    'use strict';

    describe('ConceptClusterView', function() {
        beforeEach(function() {
            this.conceptModel = new Backbone.Model({
                concepts: ['cat']
            });

            this.view = new ConceptClusterView({
                model: this.conceptModel
            });

            this.view.render();
        });

        it('displays the concept in the model', function() {
            expect(this.view.$el).toContainText('cat');
        });

        describe('when the model is changed and the view is rendered', function() {
            beforeEach(function() {
                this.conceptModel.set({
                    concepts: ['dog']
                });

                this.view.render();
            });

            it('displays the new concept', function() {
                expect(this.view.$el).not.toContainText('cat');
                expect(this.view.$el).toContainText('dog');
            });
        });

        describe('if multiple concepts are in a cluster', function() {
            beforeEach(function() {
                this.conceptModel.set({
                    concepts: ['dog', 'canine', 'wolf']
                });

                this.view.render();
            });

            it('displays the first concept in the model', function() {
                expect(this.view.$el).toContainText('dog');
            });
        });
    });
});
