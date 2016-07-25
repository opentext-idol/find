/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/idol/app/page/search/results/comparison-map',
    'find/app/page/search/results/map-view',
    'find/app/configuration',
    'backbone',
    'jasmine-jquery'
], function(ComparisonMap, MockMapView, configuration, Backbone) {

    describe('Comparison Map view', function() {
        beforeEach(function() {
            configuration.and.returnValue({
                map: {
                    enabled: true,
                    initialLocation: {
                        latitude: 51.5074,
                        longitude:  0.1278
                    },
                    locationFields: [{
                        displayName: 'test',
                        latitudeField: 'latitude',
                        longitudeField: 'longitude'
                    }]
                },
                fieldsInfo: {
                    latitude: {
                        names: [
                            "LAT"
                        ],
                        type: "NUMBER"
                    },
                    longitude: {
                        names: [
                            "LON"
                        ],
                        type: "NUMBER"
                    }
                }

            });

            this.model = new Backbone.Model({
                id: 0,
                bothText: 'both',
                firstText: 'first',
                secondText: 'second',
                onlyInFirst: 'onlyInFirst',
                onlyInSecond: 'onlyInSecond',
                inBoth: 'inBoth'
            });

            var firstModel = new Backbone.Model({
                indexes: ['firstIndexes'],
                field: 'test'
            });

            var secondModel = new Backbone.Model({
                indexes: ['secondIndexes'],
                field: 'test'
            });

            var bothModel = new Backbone.Model({
                indexes: ['firstIndexes', 'secondIndexes'],
                field: 'test'
            });

            this.view = new ComparisonMap({
                model: this.model,
                searchModels: {
                    first: firstModel,
                    second: secondModel,
                    both: bothModel
                }
            });

            this.mapView = MockMapView.instances[0];

            this.view.render();
        });

        it('displays three dropdown boxes with the correct field names', function() {
            var $selects = this.view.$('.chosen-select');
            expect($selects).toHaveLength(3);
            expect($selects[0]).toHaveText('test');
            expect($selects[1]).toHaveText('test');
            expect($selects[2]).toHaveText('test');
        });

        it('should show the show more button but it should be disabled', function() {
            var $showMore = this.view.$('.location-comparison-show-more');

            expect($showMore).toExist();
            expect($showMore).toBeDisabled();
        });

        it('should enable the show more button when the collections are not fetching', function() {
            _.each(this.view.comparisons, function(comparison) {
                comparison.collection.fetching = false;
                comparison.collection.trigger('sync');
            });

            var $showMore = this.view.$('.location-comparison-show-more');
            expect($showMore).toExist();
            expect($showMore).not.toBeDisabled();
        });

        describe('after triggering a fetch', function() {
            beforeEach(function() {
                _.each(this.view.comparisons, function(comparison) {
                    comparison.collection.fetching = true;
                    comparison.model.trigger('change:field');
                });

            });
            
            it('should disable the show more button when the collections are fetching', function() {
                var $showMore = this.view.$('.location-comparison-show-more');
                expect($showMore).toExist();
                expect($showMore).toBeDisabled();
            });            
            
            it('should show the loading spinner when the collections are fetching', function() {
                var $showMore = this.view.$('.location-comparison-show-more');
                expect($showMore).toExist();
                expect($showMore).toBeDisabled();
            });            
        });

        describe('after adding a model', function() {
            beforeEach(function() {
                this.view.comparisons[0].collection.add(new Backbone.Model({
                    summary: 'Here be dragons',
                    title: 'testTitle',
                    locations: [{
                        displayName: 'test',
                        latitude: 100,
                        longitude: 42
                    }]
                }));
            });

            it('should add a marker to the map', function() {
                expect(this.view.comparisons[0].layer.addLayer.calls.count()).toEqual(1);
                expect(this.view.mapView.getIcon.calls.count()).toEqual(1);
                expect(this.view.mapView.getMarker.calls.count()).toEqual(1);
            });
        });

        describe('after a sync', function() {
            beforeEach(function() {
                _.each(this.view.comparisons, function(comparison) {
                    comparison.collection.fetching = false;
                });
                this.view.comparisons[0].collection.trigger('sync');
            });

            it('should call loaded on the map view', function() {
                expect(this.view.mapView.loaded.calls.count()).toEqual(1);
            });
        });
    });
});
