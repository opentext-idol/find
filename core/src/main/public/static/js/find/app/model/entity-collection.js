/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection',
    'underscore'
], function(FindBaseCollection, _) {

    return FindBaseCollection.extend({
        url: '../api/public/search/find-related-concepts',

        initialize: function(models, options){
            this.queryState = options.queryState;
        },

        parse: function(response) {

            return _.chain(response)
                .reject(function(model){
                    // A negative cluster indicates that the associated documents did not fall into a cluster
                    return model.cluster < 0;
                })
                .filter(this.displayRelatedConcept, this)
                .groupBy(function(obj){
                    return obj.cluster;
                })
                .map(function(array){
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

        displayRelatedConcept: function (concept) {
            var selectedRelatedConcepts = _.flatten(this.queryState.queryTextModel.get('relatedConcepts'));

            // check to ensure each related concept are not the same as the query text or in the selected related concepts
            return concept.text.toLowerCase() !== this.queryState.queryTextModel.get('inputText').toLowerCase()
                && (selectedRelatedConcepts.length === 0 || !_.contains(selectedRelatedConcepts, concept.text));
        }
    });

});
