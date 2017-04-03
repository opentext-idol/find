/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'backbone',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'topicmap/js/topicmap'
], function(_, Backbone, vent, i18n) {
    'use strict';

    /**
     * Wraps the topic map in a resize-aware Backbone view. If the view element is not visible when resized, draw must be
     * called when the view is visible to update the SVG size.
     */
    return Backbone.View.extend({
        initialize: function(options) {
            this.data = options.data || [];
            this.clickHandler = options.clickHandler;
            this.titleClickHandler = options.titleClickHandler;

            this.listenTo(vent, 'vent:resize', this.draw);
        },

        render: function() {
            const topicMapOptions = {
                hideLegend: false,
                skipAnimation: true,
                textFadeStartDelay: 0,
                textFadeMaxDelay: 100,
                textFadeDuration: 100,
                animationDelay: 5,
                animationStepIncrement: 3,
                i18n: {
                    'autn.vis.topicmap.noResultsAvailable': i18n['search.topicMap.noResults']
                }
            };

            this.$el.addClass('clickable');

            if(this.clickHandler && this.clickHandler !== _.noop) {
                topicMapOptions.onLeafClick = _.bind(function(node) {
                    this.clickHandler([node.name]);
                }, this);

                topicMapOptions.onNodeTitleClick = _.bind(function(node) {
                    this.clickHandler(node.children
                        ? _.pluck(node.children, 'name')
                        : [node.name]);
                }, this);
            }

            this.$el.topicmap(topicMapOptions);
            this.draw();
        },

        /**
         * Set the data for the topic map. Call draw to update the SVG.
         */
        setData: function(data) {
            this.data = data || [];
        },

        /**
         * Draw the current data as a topic map in the SVG.
         */
        draw: function() {
            if(this.$el.is(':visible')) {
                this.$('svg').attr('width', this.$el.width()).attr('height', this.$el.height());
                this.$el.topicmap('renderData', {
                    size: 1.0,
                    children: this.data
                });
            }
        },

        exportPaths: function() {
            return this.$el.topicmap('exportPaths');
        }
    });
});
