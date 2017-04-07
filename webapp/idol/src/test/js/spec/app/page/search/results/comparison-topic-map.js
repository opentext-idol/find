/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    'backbone',
    'find/idol/app/page/search/results/comparison-topic-map',
    'jasmine-jquery'
], function($, Backbone, ComparisonTopicMap) {
    'use strict';

    describe('Comparison Topic Map view', function() {
        beforeEach(function() {
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

            this.view = new ComparisonTopicMap({
                model: this.model,
                searchModels: {
                    first: firstModel,
                    second: secondModel,
                    both: bothModel
                }
            });

            this.view.render();
            this.view.$el.appendTo($('body'));
        });

        afterEach(function() {
            this.view.remove();
        });

        it('should have three tabs', function() {
            expect(this.view.$('.topic-map-comparison-container > div').children()).toHaveLength(3);
        });

        it('should have the first tab selected', function() {
            expect(this.view.$('.topic-map-comparison-container > div').children()[0]).toHaveClass('active');
        });

        it('should change tab when clicked', function() {
            const $tabs = this.view.$('.topic-map-comparison-container > div');
            expect($tabs.children()).toHaveLength(3);
            expect($tabs.children()[0]).toHaveClass('active');

            const $tabSelectors = this.view.$('.topic-map-comparison-selection > ul > li');
            expect($tabSelectors).toHaveLength(3);
            expect($tabSelectors[0]).toHaveClass('active');

            this.view.$($tabSelectors[1]).find('a').trigger('click');
            expect($tabSelectors[1]).toHaveClass('active');
            expect($tabs.children()[1]).toHaveClass('active');
        });
    });
});
