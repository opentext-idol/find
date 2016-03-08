/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'topicmap/js/topicmap'
], function(Backbone, _, vent, i18n) {

    /**
     * Wraps the topic map in a resize-aware Backbone view. If the view element is not visible when resized, draw must be
     * called when the view is visible to update the SVG size.
     */
    return Backbone.View.extend({
        initialize: function(options) {
            this.data = options.data || [];
            this.clickHandler = options.clickHandler;

            this.render();

            this.listenTo(vent, 'vent:resize', function() {
                if (this.$el.is(':visible')) {
                    this.draw();
                }
            });
        },

        render: function() {
            var topicMapOptions = {
                hideLegend: false,
                skipAnimation: false,
                i18n: {
                    'autn.vis.topicmap.noResultsAvailable': i18n['search.topicMap.noResults']
                }
            };

            if (this.clickHandler) {
                this.$el.addClass('clickable');

                topicMapOptions.onLeafClick = _.bind(function(node) {
                    this.clickHandler(node.name);
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
            this.$el.topicmap('renderData', {
                size: 1.0,
                children: this.data
            });
        }
    });

});
