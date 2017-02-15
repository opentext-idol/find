/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './updating-widget',
    'i18n!find/nls/bundle',
    'text!find/idol/templates/page/dashboards/widgets/time-last-refreshed-widget.html',
    'moment-timezone-with-data'
], function(UpdatingWidget, i18n, template, moment) {
    'use strict';

    return UpdatingWidget.extend({

        lastRefreshTemplate: _.template(template),

        initialize: function(options) {
            UpdatingWidget.prototype.initialize.apply(this, arguments);

            this.dateFormat = options.widgetSettings.dateFormat || 'hh:mma z';
            this.timeZone = options.widgetSettings.timeZone || moment.tz.guess();
            this.updateInterval = options.updateInterval;

            this.lastUpdated = moment().tz(this.timeZone);
            this.nextRefresh = this.lastUpdated.clone().add(this.updateInterval)
        },

        render: function() {
            UpdatingWidget.prototype.render.apply(this, arguments);

            this.$content.html(this.lastRefreshTemplate({
                lastUpdated: i18n['dashboards.widget.lastRefresh.timeLastUpdated'](this.formatDate(this.lastUpdated)),
                nextRefresh: i18n['dashboards.widget.lastRefresh.nextRefresh'](this.formatDate(this.nextRefresh))
            }));

            this.$lastRefresh = this.$('.last-refresh');
            this.$nextRefresh = this.$('.next-refresh');
            this.$updating = this.$('.updating');
            this.$updateProgress = this.$('.update-progress');

            this.hasRendered = true;
        },

        doUpdate: function(done, updateTracker) {
            if (this.hasRendered) { // set up progress bar
                this.$updating.removeClass('hide');
                this.$updateProgress.text(i18n['dashboards.widget.lastRefresh.refreshing'](0, updateTracker.get('total') - 1));
                this.$updateProgress.removeClass('hide');
            }

            done();
        },

        onCancelled: function() {
            if (this.hasRendered) {
                this.$updating.addClass('hide');
                this.$updateProgress.addClass('hide');
            }
        },

        onComplete: function() {
            this.lastUpdated = moment().tz(this.timeZone);
            this.nextRefresh = this.lastUpdated.clone().add(this.updateInterval);

            if (this.hasRendered) {
                this.$updating.addClass('hide');
                this.$updateProgress.addClass('hide');

                this.$lastRefresh.text(i18n['dashboards.widget.lastRefresh.timeLastUpdated'](this.formatDate(this.lastUpdated)));
                this.$nextRefresh.text(i18n['dashboards.widget.lastRefresh.nextRefresh'](this.formatDate(this.nextRefresh)));
            }
        },

        onIncrement: function(updateTracker) {
            // we do not include this widget in the counts
            const completedWidgets = updateTracker.get('count') - 1;
            const totalWidgets = updateTracker.get('total') - 1;

            if (this.hasRendered) {
                this.$updateProgress.text(i18n['dashboards.widget.lastRefresh.refreshing'](completedWidgets, totalWidgets));
            }
        },

        formatDate: function(date) {
            return date.format(this.dateFormat);
        },

        exportPPTData: function(){
            // Depending on how complicated we need to make this parser, we could also handle font, italics, etc.
            return {
                data: {
                    text: _.map([this.$lastRefresh, this.$nextRefresh], function($el){
                        return {
                            text: $el.text() + '\n',
                            fontSize: 10
                        }
                    })
                },
                type: 'text'
            };
        }

    });

});