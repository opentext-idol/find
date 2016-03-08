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

            this.entityCollection = new Backbone.Collection([
                {text: 'gin', occurrences: 12, docsWithPhrase: 7},
                {text: 'siege', occurrences: 23, docsWithPhrase: 1},
                {text: 'pneumatic', occurrences: 2, docsWithPhrase: 2}
            ]);

            this.view = new EntityTopicMapView({
                clickHandler: this.clickHandler,
                entityCollection: this.entityCollection
            });

            // The view only updates when visible
            this.view.$el.appendTo(document.body);

            this.view.render();
            this.view.update();

            this.topicMap = TopicMapView.instances[0];
        });

        afterEach(function() {
            this.view.remove();
            TopicMapView.reset();
        });

        it('renders a topic map with data from the entity collection', function() {
            expect(this.topicMap.setData).toHaveBeenCalled();

            expect(this.topicMap.setData.calls.mostRecent().args[0]).toEqual([
                {name: 'siege', size: 23},
                {name: 'gin', size: 12},
                {name: 'pneumatic', size: 2}
            ]);
        });
    });

});
