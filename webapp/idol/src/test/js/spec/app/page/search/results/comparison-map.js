/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'find/idol/app/page/search/results/comparison-map',
    'find/app/page/search/results/map-view',
    'find/app/configuration',
    'backbone',
    'underscore',
    'jasmine-jquery'
], function ($, ComparisonMap, MockMapView, configuration, Backbone, _) {
    'use strict';

    describe('Comparison Map view', function () {
        beforeEach(function () {
            configuration.and.returnValue({
                map: {
                    enabled: true,
                    initialLocation: {
                        latitude: 51.5074,
                        longitude: 0.1278
                    },
                    locationFields: [{
                        displayName: 'Test',
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

            const firstModel = new Backbone.Model({
                indexes: ['firstIndexes'],
                field: 'test'
            });

            const secondModel = new Backbone.Model({
                indexes: ['secondIndexes'],
                field: 'test'
            });

            const bothModel = new Backbone.Model({
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

            _.each(this.view.resultSets, function (resultSet) {
                resultSet.collection.totalResults = 0;
            });

            this.view.render();
        });

        it('should show the show more button but it should be disabled', function () {
            const $showMore = this.view.$('.location-comparison-show-more');

            expect($showMore).toExist();
            expect($showMore).toBeDisabled();
        });

        it('should enable the show more button when the collections are not fetching', function () {
            _.each(this.view.resultSets, function (resultSet) {
                resultSet.collection.fetching = false;
                resultSet.collection.totalResults = 5;
                resultSet.collection.trigger('sync');
            });

            const $showMore = this.view.$('.location-comparison-show-more');
            expect($showMore).toExist();
            expect($showMore).not.toBeDisabled();
        });

        describe('after adding a model', function () {
            beforeEach(function () {
                this.view.resultSets[0].collection.add(new Backbone.Model({
                    summary: 'Here be dragons',
                    title: 'testTitle',
                    locations: {
                        Test: [{
                            displayName: 'Test',
                            latitude: 100,
                            longitude: 42
                        }]
                    }
                }));
            });

            it('should add a marker to the map', function () {
                expect(this.view.mapResultsViewStrategy.mapView.getIcon.calls.count()).toEqual(1);
                expect(this.view.mapResultsViewStrategy.mapView.getMarker.calls.count()).toEqual(1);
            });

            describe('after a sync', function () {
                beforeEach(function () {
                    _.each(this.view.resultSets, function (resultSet) {
                        resultSet.collection.fetching = false;
                    });
                    this.view.resultSets[0].collection.trigger('sync');
                });

                it('should call addGroupingLayer on the map view', function () {
                    expect(this.view.mapResultsViewStrategy.mapView.addGroupingLayer.calls.count()).toEqual(1);
                });

                it('should call addMarkers on the map view', function () {
                    expect(this.view.mapResultsViewStrategy.mapView.addMarkers.calls.count()).toEqual(1);
                });

                it('should call fitMapToMarkerBounds on the map view', function () {
                    expect(this.view.mapResultsViewStrategy.mapView.fitMapToMarkerBounds.calls.count()).toEqual(1);
                });
            });
        });
    });
});
