/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/selected-concepts/concept-view',
    'backbone'
], function(ConceptView, Backbone) {

    describe('Concept View', function() {
        beforeEach(function() {
            this.conceptGroups = new Backbone.Collection([
                {concepts: ['cat']},
                {concepts: ['monkey', 'ape']}
            ]);

            this.view = new ConceptView({
                queryState: {
                    conceptGroups: this.conceptGroups
                }
            });

            this.view.render();
        });

        it('displays the first concept in each group in the conceptGroups collection', function() {
            const $selectedConcepts = this.view.$('.selected-related-concept');
            expect($selectedConcepts).toHaveLength(2);

            expect($selectedConcepts).toContainText('cat');
            expect($selectedConcepts).toContainText('monkey');
        });
    });

});
