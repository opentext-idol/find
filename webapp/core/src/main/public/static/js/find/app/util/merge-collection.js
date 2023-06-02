/*
 * Copyright 2016 Open Text.
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

// TODO: Move this to js-whatever
define([
    'backbone',
    'underscore'
], function(Backbone, _) {
    'use strict';

    /**
     * @typedef {Object} MergeCollectionOptions
     * @property {Backbone.Collection[]} collections The target collections
     * @property {?String} typeAttribute Used to uniquely identify models
     * @property {?String} idAttribute Used to uniquely identify models
     */
    /**
     * Collection which merges models multiple target collections into itself. The type and id attribute options are combined
     * and used to uniquely identify target models. Typically, the type attribute value will be the same on all models in a
     * target collection, and the id attribute values will be unique in a target collection.
     * @name MergeCollection
     * @constructor
     * @param {Backbone.Model[]} models
     * @param {MergeCollectionOptions} options
     * @extends Backbone.Collection
     */
    var MergeCollection = Backbone.Collection.extend({
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
            var id = attributes[this.idAttribute];

            // Exclude null or undefined from valid ids, not 0 or the empty string
            //noinspection EqualityComparisonWithCoercionJS
            return id == null ? null : attributes[this.typeAttribute] + ':' + id;
        }
    });

    return MergeCollection;
});
