/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection'
], function(FindBaseCollection) {

    return FindBaseCollection.extend({
        url: '../api/public/search/find-related-concepts',

        parse: function(response) {
            return _.reject(response, function (model) {
                // A negative cluster indicates that the associated documents did not fall into a cluster
                return model.cluster < 0;
            })
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
        }
    });

});
