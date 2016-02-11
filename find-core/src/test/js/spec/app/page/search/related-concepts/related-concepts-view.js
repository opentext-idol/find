/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/model/indexes-collection',
    'jasmine-jquery'
], function(Backbone, RelatedConceptsView, IndexesCollection) {

    describe('Related concepts view', function() {
        function createView() {
            this.view = new RelatedConceptsView({
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel,
                queryTextModel: this.queryTextModel
            });

            this.view.render();
        }

        beforeEach(function() {
            this.indexesCollection = new IndexesCollection();
            this.entityCollection = new Backbone.Collection();
            this.queryModel = new Backbone.Model();

            this.queryTextModel = new Backbone.Model({
                inputText: 'orange',
                relatedConcepts: ['blood']
            });
        });

        describe('created before the indexes collection has fetched', function() {
            beforeEach(createView);

            it('displays the "not loading" text', function() {
                expect(this.view.$('.related-concepts-not-loading')).not.toHaveClass('hide');
            });

            it('does not display the related concepts list', function() {
                expect(this.view.$('.related-concepts-list')).toHaveClass('hide');
            });

            it('does not show the loading indicator', function() {
                expect(this.view.$('.related-concepts-processing')).toHaveClass('hide');
            });

            it('does not show the error message', function() {
                expect(this.view.$('.related-concepts-error')).toHaveClass('hide');
            });

            it('does not show the empty message', function() {
                expect(this.view.$('.related-concepts-none')).toHaveClass('hide');
            });
        });

        describe('created after the indexes collection and the related concept have fetched', function() {
            beforeEach(function() {
                this.indexesCollection.set([{name: 'Wikipedia'}]);

                this.entityCollection.set([
                    {cluster: 0, text: 'fruit'},
                    {cluster: 0, text: 'juice'},
                    {cluster: 1, text: 'red'}
                ]);

                createView.call(this);
            });

            it('displays the related concepts list', function() {
                expect(this.view.$('.related-concepts-list')).not.toHaveClass('hide');
            });

            it('does not show the loading indicator', function() {
                expect(this.view.$('.related-concepts-processing')).toHaveClass('hide');
            });

            it('does not show the "not loading" text', function() {
                expect(this.view.$('.related-concepts-not-loading')).toHaveClass('hide');
            });

            it('does not show the error message', function() {
                expect(this.view.$('.related-concepts-error')).toHaveClass('hide');
            });

            it('does not show the empty message', function() {
                expect(this.view.$('.related-concepts-none')).toHaveClass('hide');
            });

            it('only renders one loading spinner when multiple request events are triggered', function() {
                this.entityCollection.trigger('request');
                this.entityCollection.trigger('request');

                expect(this.view.$('.loading-spinner')).toHaveLength(1);
            });
        });

        describe('created after the indexes collection has fetched but before the related concepts have fetched', function() {
            beforeEach(function() {
                this.indexesCollection.set([{name: 'Wikipedia'}]);

                createView.call(this);
            });

            it('displays the loading indicator', function() {
                expect(this.view.$('.related-concepts-processing')).not.toHaveClass('hide');
            });

            it('does not display the related concepts list', function() {
                expect(this.view.$('.related-concepts-list')).toHaveClass('hide');
            });

            it('does not show the "not loading" text', function() {
                expect(this.view.$('.related-concepts-not-loading')).toHaveClass('hide');
            });

            it('does not show the error message', function() {
                expect(this.view.$('.related-concepts-error')).toHaveClass('hide');
            });

            it('does not show the empty message', function() {
                expect(this.view.$('.related-concepts-none')).toHaveClass('hide');
            });

            describe('after the related concepts have fetched', function() {
                beforeEach(function() {
                    this.entityCollection.reset([
                        {cluster: 0, text: 'fruit'},
                        {cluster: 0, text: 'juice'},
                        {cluster: 1, text: 'red'}
                    ]);
                });

                it('displays the related concepts list', function() {
                    expect(this.view.$('.related-concepts-list')).not.toHaveClass('hide');
                });

                it('hides the loading indicator', function() {
                    expect(this.view.$('.related-concepts-processing')).toHaveClass('hide');
                });

                it('does not show the "not loading" text', function() {
                    expect(this.view.$('.related-concepts-not-loading')).toHaveClass('hide');
                });

                it('does not show the error message', function() {
                    expect(this.view.$('.related-concepts-error')).toHaveClass('hide');
                });

                it('does not show the empty message', function() {
                    expect(this.view.$('.related-concepts-none')).toHaveClass('hide');
                });

                it('renders one li for each related concept', function() {
                    var $lis = this.view.$('.related-concepts-list li');
                    expect($lis).toHaveLength(3);
                    expect($lis.eq(0)).toHaveText('fruit');
                    expect($lis.eq(1)).toHaveText('juice');
                    expect($lis.eq(2)).toHaveText('red');
                });

                describe('when a related concept is clicked', function() {
                    beforeEach(function() {
                        this.view.$('.related-concepts-list li:nth-child(2) .entity-text').click();
                    });

                    it('appends the clicked concept to the query text model related concepts', function() {
                        expect(this.queryTextModel.get('relatedConcepts')).toEqual([
                            'blood',
                            'juice'
                        ]);
                    });
                });
            });
        });
    });

});
