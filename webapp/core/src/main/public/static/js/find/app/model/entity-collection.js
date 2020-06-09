/*
 * (c) Copyright 2014-2017 Micro Focus or one of its affiliates.
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
    'jquery',
    'find/app/model/find-base-collection'
], function(_, $, FindBaseCollection) {
    'use strict';

    const CLUSTER_MODE = 'docsWithPhrase';

    function sum(a, b) {
        return a + b;
    }

    const Type = {
        QUERY: 'QUERY',
        STATE_TOKEN: 'STATE_TOKEN'
    };

    return FindBaseCollection.extend({
        url: 'api/public/search/find-related-concepts',

        initialize: function(models, options) {
            // Returns an array of strings representing the currently selected concepts
            this.getSelectedRelatedConcepts = options.getSelectedRelatedConcepts;
        },

        parse: function(response) {
            return _.chain(response)
                .reject(function(model) {
                    // A negative cluster indicates that the associated documents did not fall into a cluster
                    return model.cluster < 0;
                })
                .filter(this.displayRelatedConcept, this)
                .groupBy(function(obj) {
                    return obj.cluster;
                })
                .map(function(array) {
                    // Take a maximum of 10 for each cluster
                    return _.first(array, 10);
                })
                .flatten()
                .value();
        },

        /**
         * Get the text for entities in the cluster with the given id.
         * @param {Number} clusterId
         * @return {String[]}
         */
        getClusterEntities: function(clusterId) {
            return this.chain()
                .filter(function(model) {
                    return model.get('cluster') === clusterId;
                })
                .map(function(model) {
                    return model.get('text');
                })
                .value();
        },

        displayRelatedConcept: function(concept) {
            // check to ensure each related concept are not the same as the query text or in the selected related concepts
            return _.every(this.getSelectedRelatedConcepts(), function(string) {
                return concept.text.toLowerCase() !== string.toLowerCase();
            });
        },

        processDataForTopicMap: function() {
            return _.chain(this.groupBy('cluster'))
            // Order the concepts in each cluster
                .map(function(cluster) {
                    return _.sortBy(cluster, function(model) {
                        return -model.get(CLUSTER_MODE);
                    });
                })
                // For each related concept give the name and size
                .map(function(cluster) {
                    return cluster.map(function(model) {
                        return {name: model.get('text'), size: model.get(CLUSTER_MODE)};
                    })
                })
                // Give each cluster a name (first concept in list), total size and add all
                // concepts to the children attribute to create the topic map double level.
                .map(function(cluster) {
                    return {
                        name: cluster[0].name,
                        size: _.chain(cluster)
                            .pluck('size')
                            .reduce(sum)
                            .value(),
                        children: cluster
                    };
                })
                .sortBy(function(clusterNode) {
                    return -clusterNode.size;
                })
                .value();
        },

        fetchRelatedConcepts: function(queryModel, type, maxResults) {
            let data;

            if(type === Type.STATE_TOKEN) {
                data = {
                    queryText: '*',
                    stateDontMatchTokens: queryModel.get('stateDontMatchIds')
                };
            } else if(queryModel.get('queryText') && queryModel.get('indexes').length > 0) {
                data = {
                    queryText: queryModel.get('queryText'),
                    fieldText: queryModel.get('fieldText'),
                    minDate: queryModel.getIsoDate('minDate'),
                    maxDate: queryModel.getIsoDate('maxDate'),
                    minScore: queryModel.get('minScore'),
                    queryType: 'MODIFIED'
                };
            }

            return data
                ? this.fetch({
                    data: _.extend({
                        databases: queryModel.get('indexes'),
                        maxResults: maxResults,
                        stateMatchTokens: queryModel.get('stateMatchIds')
                    }, data)
                })
                : $.when();
        }
    });
});
