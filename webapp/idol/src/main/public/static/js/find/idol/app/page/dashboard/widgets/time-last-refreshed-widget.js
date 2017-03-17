/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    './updating-widget',
    'i18n!find/nls/bundle',
    'text!find/idol/templates/page/dashboards/widgets/time-last-refreshed-widget.html',
    'moment-timezone-with-data'
], function(_, UpdatingWidget, i18n, template, moment) {
    'use strict';

    return UpdatingWidget.extend({
        lastRefreshTemplate: _.template(template),

        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);

            this.dateFormat = options.widgetSettings.dateFormat || 'HH:mm z';
            this.timeZone = options.widgetSettings.timeZone || moment.tz.guess();
            this.updateInterval = options.updateInterval;

            this.lastUpdated = moment().tz(this.timeZone);
            this.nextRefresh = this.lastUpdated.clone().add(this.updateInterval)
        },

        render: function() {
            UpdatingWidget.prototype.render.apply(this);

            this.$content.html(this.lastRefreshTemplate({
                lastUpdated: this.formatDate(this.lastUpdated),
                nextRefresh: this.formatDate(this.nextRefresh),
                i18n: i18n
            }));

            this.$lastRefresh = this.$('.last-refresh-time');
            this.$nextRefresh = this.$('.next-refresh-time');
            this.$updating = this.$('.updating');
            this.$updateProgress = this.$('.update-progress');

            this.hasRendered = true;
        },

        doUpdate: function(done, updateTracker) {
            if(this.hasRendered) { // set up progress bar
                this.$updating.removeClass('hide');
                this.$updateProgress.text(i18n['dashboards.widget.lastRefresh.refreshing'](0, updateTracker.get('total') - 1));
                this.$updateProgress.removeClass('hide');
            }

            done();
        },

        onCancelled: function() {
            if(this.hasRendered) {
                this.$updating.addClass('hide');
                this.$updateProgress.addClass('hide');
            }
        },

        onComplete: function() {
            this.lastUpdated = moment().tz(this.timeZone);
            this.nextRefresh = this.lastUpdated.clone().add(this.updateInterval);

            if(this.hasRendered) {
                this.$updating.addClass('hide');
                this.$updateProgress.addClass('hide');

                this.$lastRefresh.text(this.formatDate(this.lastUpdated));
                this.$nextRefresh.text(this.formatDate(this.nextRefresh));
            }
        },

        onIncrement: function(updateTracker) {
            // we do not include this widget in the counts
            const completedWidgets = updateTracker.get('count') - 1;
            const totalWidgets = updateTracker.get('total') - 1;

            if(this.hasRendered) {
                this.$updateProgress.text(i18n['dashboards.widget.lastRefresh.refreshing'](completedWidgets, totalWidgets));
            }
        },

        formatDate: function(date) {
            return date.format(this.dateFormat);
        },

        exportPPTData: function(){
            return {
                data: {
                    text: _.map([this.$lastRefresh, this.$nextRefresh], function($el){
                        return {
                            text: $el.text() + '\n',
                            fontSize: 15
                        }
                    })
                },
                type: 'text'
            };
        }
    });
});
