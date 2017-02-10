/*
 * Copyright 2014-2017 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    './widget',
    'text!find/idol/templates/page/dashboards/widgets/current-time-widget.html',
    'moment-timezone-with-data'
], function(Widget, template, moment) {
    'use strict';

    return Widget.extend({

        currentTimeTemplate: _.template(template),

        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);

            this.dateFormat = options.widgetSettings.dateFormat || 'll';
            this.timeFormat = options.widgetSettings.timeFormat || 'hh:mma z';
            this.timeZone = options.widgetSettings.timeZone || moment.tz.guess();
        },

        render: function() {
            Widget.prototype.render.apply(this, arguments);

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
            // Depending on how complicated we need to make this parser, we could also handle font, italics, etc.
            return {
                data: {
                    text: _.map([this.$time, this.$day, this.$date], function($el){
                        return { text: $el.text() + '\n' }
                    })
                },
                type: 'text'
            };
        }

    });

});