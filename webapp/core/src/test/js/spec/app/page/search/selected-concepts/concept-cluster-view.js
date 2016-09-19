/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/selected-concepts/concept-cluster-view',
    'backbone'
], function(ConceptClusterView, Backbone) {

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
    });

});
