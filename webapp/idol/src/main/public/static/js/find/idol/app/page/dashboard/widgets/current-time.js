/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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

            this.dateFormat = options.widgetSettings.dateFormat || 'll';
            this.timeFormat = options.widgetSettings.timeFormat || 'HH:mm z';
            this.timeZone = options.widgetSettings.timeZone || moment.tz.guess();
        },

        render: function() {
            Widget.prototype.render.apply(this);

            this.$content.html(this.currentTimeTemplate());

            this.$time = this.$('.current-time');
            this.$day = this.$('.day');
            this.$date = this.$('.date');

            this.updateTime();

            setInterval(this.updateTime.bind(this), 1000);
        },

        updateTime: function() {
            const time = moment().tz(this.timeZone);

            this.$time.text(time.format(this.timeFormat));
            this.$day.text(time.format('dddd'));
            this.$date.text(time.format(this.dateFormat));
        },

        exportPPTData: function(){
            const fontScale = 10 / 16;

            return {
                data: {
                    text: _.map([this.$time, this.$day, this.$date], function($el){
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
