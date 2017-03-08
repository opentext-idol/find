define([
    'backbone',
    'underscore',
    'find/app/model/parametric-collection',
    'find/app/util/filtering-collection',
], function (Backbone, _, ParametricCollection, FilteringCollection) {
    'use strict';

    return FilteringCollection.extend({
        initialize: function (models, options) {
            FilteringCollection.prototype.initialize.apply(this, [models, _.defaults({
                predicate: function () {
                    return true;
                }
            }, options)]);

            this.listenTo(this.collection, 'request', this.onRequest);
            this.listenTo(this.collection, 'error', this.onError);
            this.listenTo(this.collection, 'sync', this.onSync);

        },

        onRequest: function () {
            this.trigger('request');
        },

        onError: function (collection, xhr) {
            this.trigger('error', collection, xhr, Boolean(this.collection.currentRequest));
        },

        onSync: function () {
            this.trigger('sync');
        }
    });
});