/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone'
], function(Backbone) {

    // TODO: Move this to js-whatever
    return Backbone.Collection.extend({
        initialize: function(models, options) {
            this.typeAttribute = options.typeAttribute || 'type';
            this.idAttribute = options.idAttribute || 'id';

            _.each(options.collections, function(collection) {
                this.listenTo(collection, 'add', function(model) {
                    this.add(model);
                });

                this.listenTo(collection, 'remove', function(model) {
                    this.remove(model);
                });

                this.listenTo(collection, 'reset', function(collection, options) {
                    // Models from other collections
                    var otherModels = this.difference(options.previousModels);
                    this.reset(otherModels.concat(collection.models));
                });

                collection.each(function(model) {
                    models.push(model);
                });
            }, this);
        },

        modelId: function(attributes) {
            return attributes[this.typeAttribute] + ':' + attributes[this.idAttribute];
        }
    });

});
