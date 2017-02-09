/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './updating-widget',
    'find/app/page/search/results/entity-topic-map-view',
    'find/idol/app/model/idol-indexes-collection',
    'find/app/vent'
], function(UpdatingWidget, EntityTopicMapView, IndexesCollection, vent) {
    'use strict';

    return UpdatingWidget.extend({

        clickable: true,

        viewType: 'topic-map',

        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);

            this.maxResults = options.widgetSettings.maxResults;

            this.fetchPromise.done(function() {
                const queryModel = this.savedSearchModel.toQueryModel(IndexesCollection, false);

                this.entityTopicMap = new EntityTopicMapView({
                    maxResults: this.maxResults,
                    queryModel: queryModel,
                    queryState: queryModel.queryState,
                    showSlider: false,
                    type: 'QUERY'
                });

                // use the dashboard resize handler instead of the built in one
                this.entityTopicMap.topicMap.stopListening('vent:resize');
            }.bind(this))
        },

        render: function() {
            UpdatingWidget.prototype.render.apply(this, arguments);

            this.fetchPromise.done(function() {
                this.entityTopicMap.setElement(this.$content);
                this.entityTopicMap.render();
            }.bind(this))
        },

        onResize: function() {
            if (this.entityTopicMap) {
                this.entityTopicMap.update();
            }
        },

        doUpdate: function(done) {
            if (this.entityTopicMap) {
                this.updatePromise = this.entityTopicMap.fetchRelatedConcepts();

                // fetchRelatedConcepts doesn't return anything if the required parameters are not set
                if (this.updatePromise) {
                    this.updatePromise.done(function() {
                        delete this.updatePromise;
                        done();
                    }.bind(this));
                }
                else {
                    done();
                }
            }
            else {
                done();
            }
        },

        onCancelled: function() {
            if (this.updatePromise && this.updatePromise.abort) {
                this.updatePromise.abort();
            }
        }

    });

});