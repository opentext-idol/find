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
    './widget',
    'text!find/idol/templates/page/dashboards/widgets/current-time-widget.html',
    'moment-timezone-with-data'
], function(_, Widget, template, moment) {
    'use strict';

    return Widget.extend({
        currentTimeTemplate: _.template(template),

        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);

            this.dateFormat = this.widgetSettings.dateFormat || 'll';
            this.timeFormat = this.widgetSettings.timeFormat || 'HH:mm z';
            this.timeZone = this.widgetSettings.timeZone || moment.tz.guess();
        },

        render: function() {
            Widget.prototype.render.apply(this);

            this.$content.html(this.currentTimeTemplate());

            this.$time = this.$('.current-time');
            this.$day = this.$('.day');
            this.$date = this.$('.date');

            this.updateTime();

            setInterval(this.updateTime.bind(this), 250);
        },

        updateTime: function() {
            const time = moment().tz(this.timeZone);

            this.$time.text(time.format(this.timeFormat));
            this.$day.text(time.format('dddd'));
            this.$date.text(time.format(this.dateFormat));
        },

        exportData: function() {
            const fontScale = 10 / 16;

            return {
                data: {
                    text: _.map([this.$time, this.$day, this.$date], function($el) {
                        return {
                            text: $el.text().toUpperCase() + '\n',
                            fontSize: Math.round(parseInt($el.css('font-size')) * fontScale)
                        }
                    })
                },
                type: 'text'
            };
        }
    });
});
