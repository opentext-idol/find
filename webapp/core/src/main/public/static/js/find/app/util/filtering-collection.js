/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore'
], function(Backbone, _) {

    'use strict';

    return Backbone.Collection.extend({
        initialize: function(models, options) {
            this.filterModel = options.filterModel;
            this.collection = options.collection;

            // _ allows us to pass the model as the first argument
            this.predicate = _.partial(options.predicate, _, this.filterModel);
            this.resetOnFilter = options.resetOnFilter || false;

            _.each(options.collectionFunctions, function(functionName) {
                this[functionName] = function() {
                    return options.collection[functionName].apply(this, arguments);
                }
            }, this);

            if (this.filterModel) {
                this.listenTo(this.filterModel, 'change', this.filterModels);
            }

            this.listenTo(this.collection, 'add', this.onAdd);
            this.listenTo(this.collection, 'remove', this.onRemove);
            this.listenTo(this.collection, 'change', this.onChange);
            this.listenTo(this.collection, 'reset', this.onReset);
            this.listenTo(this.collection, 'request', this.onRequest);
            this.listenTo(this.collection, 'error', this.onError);
            this.listenTo(this.collection, 'sync', this.onSync);

            this.collection.each(function(model) {
                if (this.predicate(model)) {
                    models.push(model);
                }
            }, this);
        },

        onAdd: function(model) {
            if (this.predicate(model)){
                this.add(model);
            }
        },

        onRemove: function(model) {
            this.remove(model);
        },

        onChange: function(model) {
            if (!this.predicate(model)) {
                this.remove(model);
            }
        },

        onReset: function(collection) {
            this.reset(collection.filter(this.predicate))
        },

        onRequest: function () {
            this.trigger('request');
        },

        onError: function (collection, xhr) {
            this.trigger('error', collection, xhr);
        },

        onSync: function () {
            this.trigger('sync');
        },

        filterModels: function() {
            const models = this.collection.filter(this.predicate);

            if (this.resetOnFilter) {
                this.reset(models);
            } else {
                this.set(models);
            }           
        },

        isProcessing: function () {
            return Boolean(this.collection.currentRequest);
        }
    })
});

