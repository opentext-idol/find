/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'backbone'
], function(_, Backbone) {
    'use strict';

    return Backbone.Collection.extend({
        initialize: function(models, options) {
            this.filterModel = options.filterModel;
            this.collection = options.collection;

            this.predicate = _.partial(options.predicate, _, this.filterModel);
            this.resetOnFilter = options.resetOnFilter || false;

            _.each(options.collectionFunctions, function(functionName) {
                this[functionName] = function() {
                    return options.collection[functionName].apply(this, arguments);
                }
            }, this);

            if(this.filterModel) {
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
                if(this.predicate(model)) {
                    models.push(model);
                }
            }, this);
        },

        onAdd: function(model) {
            if(this.predicate(model)) {
                this.add(model);
            }
        },

        onRemove: function(model) {
            this.remove(model);
        },

        onChange: function(model) {
            if(!this.predicate(model)) {
                this.remove(model);
            }
        },

        onReset: function(collection) {
            this.reset(collection.filter(this.predicate))
        },

        onRequest: function() {
            this.trigger('request');
        },

        onError: function(collection, xhr) {
            this.trigger('error', collection, xhr);
        },

        onSync: function() {
            this.trigger('sync');
        },

        filterModels: function() {
            const models = this.collection.filter(this.predicate);

            if(this.resetOnFilter) {
                this.reset(models);
            } else {
                this.set(models);
            }
        },

        isProcessing: function() {
            return Boolean(this.collection.currentRequest);
        }
    })
});
