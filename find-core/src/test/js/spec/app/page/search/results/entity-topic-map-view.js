/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'find/app/page/search/results/entity-topic-map-view',
    'find/app/util/topic-map-view'
], function(Backbone, EntityTopicMapView, TopicMapView) {

    describe('EntityTopicMapView', function() {
        beforeEach(function() {
            this.clickHandler = jasmine.createSpy('clickHandler');
            this.entityCollection = new Backbone.Collection();

            this.createView = function() {
                this.view = new EntityTopicMapView({
                    clickHandler: this.clickHandler,
                    entityCollection: this.entityCollection
                });

                // The view only updates when visible
                this.view.$el.appendTo(document.body);

                this.view.render();
                this.view.update();

                this.topicMap = TopicMapView.instances[0];
            };
        });

        afterEach(function() {
            this.view.remove();
            TopicMapView.reset();
        });

        describe('rendered with no entities in the collection', function() {
            beforeEach(function() {
                this.createView();
            });

            it('shows the empty message', function() {
                expect(this.view.$('.entity-topic-map-empty')).not.toHaveClass('hide');
            });

            it('does not show the loading indicator', function() {
                expect(this.view.$('.entity-topic-map-loading')).toHaveClass('hide');
            });

            it('does not show the error message', function() {
                expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
            });

            it('does not show the topic map', function() {
                expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
            });
        });

        describe('rendered with a request in flight', function() {
            beforeEach(function() {
                this.entityCollection.currentRequest = true;

                // Populate the collection to test that the current request overrides the empty condition
                this.entityCollection.set([{
                    text: 'gin', occurrences: 12, docsWithPhrase: 7}
                ]);

                this.createView();
            });

            it('shows the loading indicator', function() {
                expect(this.view.$('.entity-topic-map-loading')).not.toHaveClass('hide');
            });

            it('does not show the empty message', function() {
                expect(this.view.$('.entity-topic-map-empty')).toHaveClass('hide');
            });

            it('does not show the error message', function() {
                expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
            });

            it('does not show the topic map', function() {
                expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
            });
        });

        describe('rendered with entities in the collection', function() {
            beforeEach(function() {
                this.entityCollection.set([
                    {text: 'gin', occurrences: 12, docsWithPhrase: 7},
                    {text: 'siege', occurrences: 23, docsWithPhrase: 1},
                    {text: 'pneumatic', occurrences: 2, docsWithPhrase: 2}
                ]);

                this.createView();
            });

            it('does not show the loading indicator', function() {
                expect(this.view.$('.entity-topic-map-loading')).toHaveClass('hide');
            });

            it('does not show the error message', function() {
                expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
            });

            it('does not show the empty message', function() {
                expect(this.view.$('.entity-topic-map-empty')).toHaveClass('hide');
            });

            it('renders a topic map with data from the entity collection', function() {
                expect(this.topicMap.setData).toHaveBeenCalled();
                expect(this.topicMap.draw).toHaveBeenCalled();

                expect(this.topicMap.setData.calls.mostRecent().args[0]).toEqual([
                    {name: 'siege', size: 23},
                    {name: 'gin', size: 12},
                    {name: 'pneumatic', size: 2}
                ]);

                expect(this.view.$('.entity-topic-map')).not.toHaveClass('hide');
            });

            describe('when the entities collection is fetched', function() {
                beforeEach(function() {
                    this.entityCollection.currentRequest = true;
                    this.entityCollection.trigger('request');
                });

                it('shows the loading indicator', function() {
                    expect(this.view.$('.entity-topic-map-loading')).not.toHaveClass('hide');
                });

                it('does not show the error message', function() {
                    expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
                });

                it('does not show the empty message', function() {
                    expect(this.view.$('.entity-topic-map-empty')).toHaveClass('hide');
                });

                it('hides the topic map', function() {
                    expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
                });

                describe('then the fetch succeeds with no results', function() {
                    beforeEach(function() {
                        this.entityCollection.reset();
                        this.entityCollection.trigger('sync');
                    });

                    it('hides the loading indicator', function() {
                        expect(this.view.$('.entity-topic-map-loading')).toHaveClass('hide');
                    });

                    it('does not show the error message', function() {
                        expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
                    });

                    it('shows the empty message', function() {
                        expect(this.view.$('.entity-topic-map-empty')).not.toHaveClass('hide');
                    });

                    it('does not show the topic map', function() {
                        expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
                    });
                });

                describe('then the fetch fails', function() {
                    beforeEach(function() {
                        this.entityCollection.reset();
                        this.entityCollection.trigger('error', this.entityCollection, {status: 400});
                    });

                    it('hides the loading indicator', function() {
                        expect(this.view.$('.entity-topic-map-loading')).toHaveClass('hide');
                    });

                    it('shows the error message', function() {
                        expect(this.view.$('.entity-topic-map-error')).not.toHaveClass('hide');
                    });

                    it('does not show the empty message', function() {
                        expect(this.view.$('.entity-topic-map-empty')).toHaveClass('hide');
                    });

                    it('does not show the topic map', function() {
                        expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
                    });
                });

                describe('then the fetch is aborted', function() {
                    beforeEach(function() {
                        this.entityCollection.reset();
                        this.entityCollection.trigger('error', this.entityCollection, {status: 0});
                    });

                    it('does not hide the loading indicator', function() {
                        expect(this.view.$('.entity-topic-map-loading')).not.toHaveClass('hide');
                    });

                    it('does not show the error message', function() {
                        expect(this.view.$('.entity-topic-map-error')).toHaveClass('hide');
                    });

                    it('does not show the empty message', function() {
                        expect(this.view.$('.entity-topic-map-empty')).toHaveClass('hide');
                    });

                    it('does not show the topic map', function() {
                        expect(this.view.$('.entity-topic-map')).toHaveClass('hide');
                    });
                });
            });
        });
    });

});
