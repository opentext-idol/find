/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * @module databases-view/js/databases-collection
 */
define([
    'backbone',
    'underscore',
    'js-whatever/js/escape-hod-identifier'
], function(Backbone, _, escapeHodIdentifier) {

    /**
     * @name module:databases-view/js/databases-collection.DatabaseModel
     * @desc Model representing a single HOD resource. Must have at least a domain and a name attribute.
     * @constructor
     * @extends Backbone.Model
     */
    var DatabaseModel = Backbone.Model.extend(/** @lends module:databases-view/js/databases-collection.DatabaseModel.prototype */{
        /**
         * @desc Convert the model to an object with a domain and a name property.
         * @returns {ResourceIdentifier}
         * @function
         */
        toResourceIdentifier: function() {
            return this.pick('domain', 'name');
        }
    });

    /**
     * @name module:databases-view/js/databases-collection.DatabasesCollection
     * @desc Collection representing a set of HOD resources. Each model must have a domain and a name attribute.
     * @constructor
     * @see {module:databases-view/js/databases-collection.DatabaseModel}
     * @extends Backbone.Collection
     */
    return Backbone.Collection.extend(/** @lends module:databases-view/js/databases-collection.DatabasesCollection.prototype */{
        model: DatabaseModel,

        modelId: function(attributes) {
            // HOD resources are the same when they have the same domain and name
            return escapeHodIdentifier(attributes.domain) + ':' + escapeHodIdentifier(attributes.name);
        },

        /**
         * @desc Convert the collection to an array of objects with a domain and a name property.
         * @returns {ResourceIdentifier}
         * @function
         */
        toResourceIdentifiers: function() {
            return this.invoke('toResourceIdentifier');
        }
    });

});
