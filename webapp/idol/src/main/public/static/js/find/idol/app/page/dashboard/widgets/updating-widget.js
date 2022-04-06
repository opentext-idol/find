/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'jquery',
    './widget'
], function(_, $, Widget) {
    'use strict';

    const loadingHtml = '<i class="widget-update-spinner fa fa-spinner fa-spin"></i>';

    function beginInitialization() {
        // display initialisation loading spinner
        this.toggleContent(false);
        this.$loadingSpinner = this.$('.widget-init-spinner').removeClass('hide');
    }

    function completeInitialization() {
        this.toggleContent(true);
        // Need a loading spinner in order to show that an update is ongoing -- put it in
        // title bar if one is shown, or in the corner of the widget if it is hidden.
        // The new spinner replaces central initialisation loading spinner, which may be removed.
        this.$loadingSpinner.remove();
        const loadingEl = $(loadingHtml);
        if(this.widgetHasTitleBar) {
            this.$loadingSpinner = loadingEl.addClass('pull-right', 'hide');
            this.$('.title').append(this.$loadingSpinner);
        } else {
            this.$loadingSpinner = $('<div class="widget-spinner-container hide"></div>')
                .append(loadingEl);
            this.$content.after(this.$loadingSpinner);
        }
    }

    return Widget.extend({
        isUpdating: _.constant(true),
        onComplete: _.noop,
        onIncrement: _.noop,
        onCancelled: _.noop,

        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);
            this.widgetHasTitleBar = options.displayWidgetName === 'always';
            this.beginInitialization = _.once(beginInitialization.bind(this));
            this.completeInitialization = _.once(completeInitialization.bind(this));
        },

        toggleContent: function(show) {
            this.$content.toggleClass('hide', !show);
        },

        toggleSpinner: function(show) {
            this.$loadingSpinner.toggleClass('hide', !show);
        },

        update: function(updateTracker) {
            this.beginInitialization();
            this.toggleSpinner(true);

            const listener = function(callback) {
                this.toggleSpinner(false);
                this.completeInitialization();
                callback.call(this, updateTracker);
                this.stopListening(updateTracker);
            }.bind(this);

            this.listenTo(updateTracker, 'change:complete', _.partial(listener, this.onComplete));
            this.listenTo(updateTracker, 'change:cancelled', _.partial(listener, this.onCancelled));
            this.listenTo(updateTracker, 'change:count', function() {
                this.onIncrement(updateTracker);
            });

            this.doUpdate(function() {
                this.toggleSpinner(false);
                this.completeInitialization();
                updateTracker.increment();
            }.bind(this), updateTracker);
        },

        // To be overridden; every execution path should include calling done(), so
        // that the update loading spinner is toggled.
        doUpdate: function(done, updateTracker) {
            done();
        }
    });
});
