/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/related-concepts/related-concepts-view',
    'find/app/model/indexes-collection',
    'find/app/model/entity-collection',
    'jasmine-jquery'
], function(Backbone, RelatedConceptsView, IndexesCollection, EntityCollection) {

    describe('Related concepts view', function() {
        function createView() {
            this.clickHandler = jasmine.createSpy('clickHandler');

            this.view = new RelatedConceptsView({
                entityCollection: this.entityCollection,
                indexesCollection: this.indexesCollection,
                queryModel: this.queryModel,
                queryTextModel: this.queryTextModel,
                clickHandler: this.clickHandler
            });

            this.view.render();
        }

        beforeEach(function() {
            this.indexesCollection = new IndexesCollection();
            this.entityCollection = new EntityCollection;
            this.queryModel = new Backbone.Model();

            this.queryTextModel = new Backbone.Model({
                inputText: 'orange',
                relatedConcepts: [['blood']]
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
                ], { parse: true });

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
                        {cluster: -1, text: 'bromine'},
                        {cluster: 0, text: 'juice'},
                        {cluster: 1, text: 'red'},
                        {cluster: 0, text: 'squeeze'}
                    ], { parse: true });
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

                it('does not render items with a negative cluster', function() {
                    expect(this.view.$('.related-concepts-list')).not.toContainText('bromine');
                });

                it('renders each item in a non-negative cluster with other members of the cluster, taking the first member as the heading', function() {
                    var $clusterLis = this.view.$('.related-concepts-list > li');
                    expect($clusterLis).toHaveLength(2);

                    var $fruitCluster = $clusterLis.eq(0);
                    expect($fruitCluster.find('h4')).toHaveText('fruit');
                    expect($fruitCluster.find('li')).toHaveLength(3);
                    expect($fruitCluster.find('li:nth-child(1)')).toHaveText('fruit');
                    expect($fruitCluster.find('li:nth-child(2)')).toHaveText('juice');
                    expect($fruitCluster.find('li:nth-child(3)')).toHaveText('squeeze');

                    var $colourCluster = $clusterLis.eq(1);
                    expect($colourCluster.find('h4')).toHaveText('red');
                    expect($colourCluster.find('li')).toHaveLength(0);
                });

                describe('when a cluster heading is clicked', function() {
                    beforeEach(function() {
                        // Click a child of the heading to check that click events propagate correctly (a previous bug)
                        this.view.$('.related-concepts-list > li:nth-child(1) h4 > a *').click();
                    });

                    it('calls the clickHandler with every item in the cluster', function() {
                        expect(this.clickHandler).toHaveBeenCalledWith([
                            'fruit',
                            'juice',
                            'squeeze'
                        ]);
                    });
                });

                describe('when a cluster child is clicked', function() {
                    beforeEach(function() {
                        this.view.$('.related-concepts-list > li:nth-child(1) li:nth-child(1) .entity-text').click();
                    });

                    it('calls the click handler with an array containing the clicked item', function() {
                        expect(this.clickHandler).toHaveBeenCalledWith([
                            'fruit'
                        ]);
                    });
                });
            });
        });
    });

});
