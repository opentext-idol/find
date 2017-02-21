/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    './widget'
], function(_, Widget) {
    'use strict';

    const loadingTemplate = '<i class="widget-loading-spinner fa fa-spinner fa-spin pull-right hide"></i>';

    return Widget.extend({
        isUpdating: _.constant(true),
        onComplete: _.noop,
        onIncrement: _.noop,
        onCancelled: _.noop,

        render: function() {
            Widget.prototype.render.apply(this);

            this.$('.title').append(loadingTemplate);
        },

        toggleSpinner: function(show) {
            this.$('.widget-loading-spinner').toggleClass('hide', !show);
        },

        update: function(updateTracker) {
            this.toggleSpinner(true);

            const listener = function(callback) {
                this.toggleSpinner(false);

                callback.call(this, updateTracker);

                this.stopListening(updateTracker);
            };

            this.listenTo(updateTracker, 'change:complete', _.partial(listener, this.onComplete));
            this.listenTo(updateTracker, 'change:cancelled', _.partial(listener, this.onCancelled));

            this.listenTo(updateTracker, 'change:count', function() {
                this.onIncrement(updateTracker);
            });

            this.doUpdate(function() {
                this.toggleSpinner(false);

                updateTracker.increment();
            }.bind(this), updateTracker);
        },

        // override this with your update code
        doUpdate: function(done, updateTracker) {
            done();
        }
    });
});
