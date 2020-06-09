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
    './updating-widget',
    'i18n!find/nls/bundle',
    'text!find/idol/templates/page/dashboards/widgets/time-last-refreshed-widget.html',
    'moment-timezone-with-data'
], function(_, UpdatingWidget, i18n, template, moment) {
    'use strict';

    const EXPORT_FONT_SIZE = 10;
    const EXPORT_TIME_FONT_SIZE = 15;

    return UpdatingWidget.extend({
        lastRefreshTemplate: _.template(template),

        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);

            this.dateFormat = this.widgetSettings.dateFormat || 'HH:mm z';
            this.timeZone = this.widgetSettings.timeZone || moment.tz.guess();

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

        exportData: function() {
            return {
                data: {
                    text: [
                        {
                            text: i18n['dashboards.widget.lastRefresh.timeLastUpdated'] + '\n',
                            fontSize: EXPORT_FONT_SIZE
                        },
                        {
                            text: this.$lastRefresh.text() + '\n',
                            fontSize: EXPORT_TIME_FONT_SIZE
                        },
                        {
                            text: i18n['dashboards.widget.lastRefresh.nextRefresh'] + '\n',
                            fontSize: EXPORT_FONT_SIZE
                        },
                        {
                            text: this.$nextRefresh.text() + '\n',
                            fontSize: EXPORT_TIME_FONT_SIZE
                        }
                    ]
                },
                type: 'text'
            };
        }
    });
});
