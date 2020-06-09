/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'find/app/page/search/selected-concepts/concept-view'
], function(Backbone, ConceptView) {
    'use strict';

    describe('Concept View', function() {
        const configurations = [
            {
                description: 'with User configuration',
                options: {}
            },
            {
                description: 'with BI configuration',
                options: {
                    hasBiRole: true
                }
            }
        ];

        configurations.forEach(function(configuration) {
            describe(configuration.description, function() {
                beforeEach(function() {
                    this.conceptGroups = new Backbone.Collection([
                        {concepts: ['cat']},
                        {concepts: ['monkey', 'ape']}
                    ]);

                    this.view = new ConceptView({
                        configuration: configuration.options,
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

                it('does not display the empty message when there are concepts', function() {
                    expect(this.view.$('.concept-view-empty-message')).toHaveClass('hide');
                });

                describe('when a remove button is clicked', function() {
                    beforeEach(function() {
                        this.view.$('[data-cluster-cid="' + this.conceptGroups.at(0).cid + '"] .concept-remove-icon').click();
                    });

                    it('removes the corresponding cluster model from the collection', function() {
                        expect(this.conceptGroups).toHaveLength(1);
                        expect(this.conceptGroups.at(0).get('concepts')).toEqual(['monkey', 'ape']);
                    });
                });

                describe('when the collection is reset', function() {
                    beforeEach(function() {
                        this.conceptGroups.reset();
                    });

                    it('displays the empty message', function() {
                        expect(this.view.$('.concept-view-empty-message')).not.toHaveClass('hide');
                    });

                    describe('then a new hidden concept is added', function() {
                        beforeEach(function() {
                            this.conceptGroups.add({
                                concepts: ['animals'],
                                hidden: true
                            });
                        });

                        it('displays the empty message', function() {
                            expect(this.view.$('.concept-view-empty-message')).not.toHaveClass('hide');
                        });

                        describe('then a new cluster is added', function() {
                            beforeEach(function() {
                                this.conceptGroups.add({
                                    concepts: ['cat', 'kitten', 'lion']
                                });
                            });

                            it('hides the empty message', function() {
                                expect(this.view.$('.concept-view-empty-message')).toHaveClass('hide');
                            });

                            it('does not show the hidden concept', function() {
                                const $selectedConcepts = this.view.$('.selected-related-concept');
                                expect($selectedConcepts).toHaveLength(1);
                            });
                        });
                    });
                });
            });
        });
    });
});
