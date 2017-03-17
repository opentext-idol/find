/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './saved-search-widget',
    'find/app/page/search/results/entity-topic-map-view',
    'find/app/vent'
], function(SavedSearchWidget, EntityTopicMapView, vent) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'topic-map',

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            this.maxResults = options.widgetSettings.maxResults;
        },

        postInitialize: function() {
            this.entityTopicMap = new EntityTopicMapView({
                maxResults: this.maxResults,
                queryModel: this.queryModel,
                queryState: this.queryModel.queryState,
                showSlider: false,
                type: 'QUERY'
            });

            if(this.$content) {
                this.entityTopicMap.setElement(this.$content).render();
            }

            // use the dashboard resize handler instead of the built in one
            // contrary to the Backbone docs we do need to specify vent here
            this.entityTopicMap.topicMap.stopListening(vent, 'vent:resize');
        },

        render: function() {
            SavedSearchWidget.prototype.render.apply(this);

            if(this.entityTopicMap) {
                this.entityTopicMap.setElement(this.$content).render();
            }
        },

        onResize: function() {
            if(this.entityTopicMap) {
                this.entityTopicMap.update();
            }
        },

        getData: function() {
            return this.entityTopicMap.fetchRelatedConcepts();
        },

        exportPPTData: function() {
            if (this.entityTopicMap) {
                var data = this.entityTopicMap.exportPPTData();

                if (data) {
                    return {
                        data: data,
                        type: 'topicmap'
                    }
                }
            }
        }
    });
});
