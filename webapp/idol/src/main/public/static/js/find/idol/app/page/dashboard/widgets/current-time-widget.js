/*
 * Copyright 2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const Widget = require('./widget');
const template = require('find/idol/templates/page/dashboards/widgets/current-time-widget.html');
const moment = require('moment-timezone-with-data');

module.exports = Widget.extend({
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

