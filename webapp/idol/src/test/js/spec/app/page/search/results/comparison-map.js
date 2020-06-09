/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'underscore',
    'backbone',
    'find/idol/app/page/search/results/comparison-map',
    'find/app/page/search/results/map-view',
    'find/app/configuration',
    'jasmine-jquery'
], function(_, Backbone, ComparisonMap, MockMapView, configuration) {
    'use strict';

    describe('Comparison Map View', function() {
        jasmine.getEnv().configure({ random: false });

        beforeEach(function() {
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

            _.each(this.view.resultSets, function(resultSet) {
                resultSet.collection.totalResults = 0;
            });

            this.view.render();
        });

        it('should show the show more button but it should be disabled', function() {
            const $showMore = this.view.$('.location-comparison-show-more');

            expect($showMore).toExist();
            expect($showMore).toBeDisabled();
        });

        it('should enable the show more button when the collections are not fetching', function() {
            _.each(this.view.resultSets, function(resultSet) {
                resultSet.collection.fetching = false;
                resultSet.collection.totalResults = 5;
                resultSet.collection.trigger('sync');
            });

            const $showMore = this.view.$('.location-comparison-show-more');
            expect($showMore).toExist();
            expect($showMore).not.toBeDisabled();
        });

        describe('after adding a model', function() {
            beforeEach(function() {
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

            it('should add a marker to the map', function() {
                expect(this.view.mapResultsViewStrategy.mapView.getIcon.calls.count()).toBe(1);
                expect(this.view.mapResultsViewStrategy.mapView.getMarker.calls.count()).toBe(1);
            });

            describe('after a sync', function() {
                beforeEach(function() {
                    _.each(this.view.resultSets, function(resultSet) {
                        resultSet.collection.fetching = false;
                    });
                    this.view.resultSets[0].collection.trigger('sync');
                });

                it('should call addGroupingLayer on the map view', function() {
                    expect(this.view.mapResultsViewStrategy.mapView.addGroupingLayer.calls.count()).toBe(1);
                });

                it('should call addMarkers on the map view', function() {
                    expect(this.view.mapResultsViewStrategy.mapView.addMarkers.calls.count()).toBe(1);
                });

                it('the error span should be hidden', function() {
                    expect(this.view.$('.map-error')).toHaveClass('hide');
                });

                it('the show more button should be enabled', function() {
                    expect(this.view.$('.location-comparison-show-more')).not.toBeDisabled();
                });

                it('should call fitMapToMarkerBounds on the map view', function() {
                    expect(this.view.mapResultsViewStrategy.mapView.fitMapToMarkerBounds.calls.count()).toBe(1);
                });
            });

            describe('after an error', function() {
                beforeEach(function() {
                    this.view.resultSets[0].collection.trigger('error', null, {
                        status: 1,
                        responseJSON: {}
                    });
                    _.each(this.view.resultSets, function(resultSet) {
                        resultSet.collection.trigger = false;
                    });
                });

                it('the error span should be visible', function() {
                    expect(this.view.$('.map-error')).not.toHaveClass('hide');
                });

                it('the show more button should be disabled', function() {
                    expect(this.view.$('.location-comparison-show-more')).toBeDisabled();
                });

                it('the loading spinner should not be visible', function() {
                    expect(this.view.$('.loading-spinner')).toHaveClass('hide');
                });
            });
        });
    });
});
