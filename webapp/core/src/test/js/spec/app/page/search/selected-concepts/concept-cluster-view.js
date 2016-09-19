/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/selected-concepts/concept-cluster-view',
    'backbone',
    'jquery'
], function(ConceptClusterView, Backbone, $) {

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

        describe('if the model contains multiple concepts', function() {
            beforeEach(function() {
                this.conceptModel.set({
                    concepts: ['dog', 'canine', 'wolf']
                });

                this.view.render();
            });

            it('displays the first concept in the model', function() {
                expect(this.view.$el).toContainText('dog');
            });

            it('displays every concept in a dropdown', function() {
                const dropdownConcepts = this.view.$('.selected-related-concept-dropdown > li > a')
                    .map(function(index, el) {
                        return $(el).text();
                    });

                expect(dropdownConcepts).toEqual(['dog', 'canine', 'wolf']);
            });
        });
    });

});
