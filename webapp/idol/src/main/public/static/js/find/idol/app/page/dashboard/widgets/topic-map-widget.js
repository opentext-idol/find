/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const SavedSearchWidget = require('./saved-search-widget');
const TopicMapView = require('find/app/util/topic-map-view');
const EntityCollection = require('find/app/model/entity-collection');

module.exports = SavedSearchWidget.extend({
        viewType: 'topic-map',

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);
            this.maxResults = this.widgetSettings.maxResults || 300;

            this.entityCollection = new EntityCollection([], {
                getSelectedRelatedConcepts: function() {
                    return _.flatten(this.queryModel.queryState.conceptGroups.pluck('concepts'));
                }.bind(this)
            });

            this.topicMap = new TopicMapView({
                clickHandler: _.noop,
                autoResize: false
            });
        },

        render: function() {
            SavedSearchWidget.prototype.render.apply(this);
            this.$content.addClass('fixed-height');
            this.topicMap.setElement(this.$content).render();
        },

        onResize: function() {
            if(this.topicMap) {
                this.topicMap.draw();
            }
        },

        updateVisualizer: function(){
            if(this.topicMap && !this.isEmpty()) {
                this.topicMap.setData(this.entityCollection.processDataForTopicMap());
                this.topicMap.draw();
            }
        },

        getData: function() {
            return this.entityCollection
                .fetchRelatedConcepts(this.queryModel, 'QUERY', this.maxResults);
        },

        isEmpty: function(){
            return this.entityCollection.isEmpty();
        },

        exportData: function() {
            if(this.topicMap) {
                const data = this.topicMap.exportData();
                return data
                    ? {
                        data: data,
                        type: 'topicmap'
                    }
                    : null;
            }

            return null;
        }
    });

