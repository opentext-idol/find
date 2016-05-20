/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore'
], function(Backbone, _) {
    "use strict";

    return Backbone.View.extend({
        // will be overridden
        updateEmpty: null,
        template: null,

        monitorCollection: function(collection) {
            this.model = new Backbone.Model({
                processing: Boolean(collection.currentRequest),
                error: false,
                empty: collection.isEmpty()
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.model, 'change:processing', this.updateProcessing);
            //noinspection JSUnresolvedFunction
            this.listenTo(this.model, 'change:error', this.updateError);
            //noinspection JSUnresolvedFunction
            this.listenTo(this.model, 'change', this.updateEmpty);

            //noinspection JSUnresolvedFunction
            this.listenTo(collection, 'request', function() {
                this.model.set({processing: true, error: false});
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(collection, 'error', function(collection, xhr) {
                if (xhr.status === 0) {
                    this.model.set({processing: Boolean(collection.currentRequest)});
                } else {
                    // The request was not aborted, so there isn't another request in flight
                    this.model.set({error: true, processing: false});
                }
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(collection, 'sync', function() {
                this.model.set({processing: false});
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(collection, 'update reset', function() {
                this.model.set('empty', collection.isEmpty());
            });
        },

        render: function() {
            //noinspection JSUnresolvedVariable
            this.$el.html(this.template).prepend(this.fieldNamesListView.$el);
            this.fieldNamesListView.render();

            //noinspection JSUnresolvedFunction
            this.$emptyMessage = this.$('.parametric-empty');
            //noinspection JSUnresolvedFunction
            this.$errorMessage = this.$('.parametric-error');
            //noinspection JSUnresolvedFunction
            this.$processing = this.$('.parametric-processing-indicator');

            this.updateEmpty();
            this.updateError();
            this.updateProcessing();

            return this;
        },

        updateProcessing: function() {
            if (this.$processing) {
                this.$processing.toggleClass('hide', !this.model.get('processing'));
            }
        },

        updateError: function() {
            if (this.$errorMessage) {
                this.$errorMessage.toggleClass('hide', !this.model.get('error'));
            }
        }
    });
});