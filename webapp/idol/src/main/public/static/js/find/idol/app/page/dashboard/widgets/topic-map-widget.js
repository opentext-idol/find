/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './saved-search-widget',
    'find/app/page/search/results/entity-topic-map-view',
    'find/idol/app/model/idol-indexes-collection',
    'find/app/vent'
], function(SavedSearchWidget, EntityTopicMapView, IndexesCollection, vent) {
    'use strict';

    return SavedSearchWidget.extend({

        viewType: 'topic-map',

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            this.maxResults = options.widgetSettings.maxResults;
        },

        postInitialize: function () {
            this.entityTopicMap = new EntityTopicMapView({
                maxResults: this.maxResults,
                queryModel: this.queryModel,
                queryState: this.queryModel.queryState,
                showSlider: false,
                type: 'QUERY'
            });

            // use the dashboard resize handler instead of the built in one
            this.entityTopicMap.topicMap.stopListening('vent:resize');
        },

        render: function() {
            SavedSearchWidget.prototype.render.apply(this, arguments);

            this.entityTopicMap.setElement(this.$content);
            this.entityTopicMap.render();
        },

        onResize: function() {
            if (this.entityTopicMap) {
                this.entityTopicMap.update();
            }
        },

        getData: function() {
            return this.entityTopicMap.fetchRelatedConcepts();
        }

    });

});