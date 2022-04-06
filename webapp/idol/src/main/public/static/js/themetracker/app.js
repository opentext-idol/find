/*
 * (c) Copyright 2014-2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'moment',
    'themetracker/themetracker',
    'bootstrap'
], function(_, $, Backbone, moment, ThemeTracker) {
    'use strict';

    return Backbone.View.extend({
        el: '.page',

        initialize: function(){
            _.bindAll(this, 'updateTime')

            $(document).ajaxError(function(event, xhr) {
                if(xhr.status === 401 || xhr.status === 403) {
                    // redirect to home for login page
                    window.location = '';
                }
            })

            this.render();
        },

        render: function(){
            this.$time = this.$('.clock-time');
            this.$date = this.$('.clock-date');

            this.updateTime();

            var daysMatch = /[&?]days=(\d+)\b/.exec(window.location.search);
            var days = daysMatch && daysMatch[1] > 0 ? +daysMatch[1] : 7;

            function formatLabel(days) {
                return days === 1 ? 'Last Day' :
                    days === 7 ? 'Last Week' :
                    days === 14 ? 'Last Fortnight' :
                    (days >= 28 && days <= 32) ? 'Last Month'
                    : 'Last ' + days + ' days';
            }

            var jobsConf = {};
            var job = '';

            jobsConf[job] = 'All';

            this.$('.date-controls')
                .find('.dropdown-menu').append((function(){
                    return [7, 14].map(function(days){
                        return '<li><a href="' + _.escape(window.location.href.replace(/(\?.*)?$/, '?days=' + days)) + '">' + formatLabel(days) + '</a></li>';

                    })
                })())
                .end()
                .find('button span').text(formatLabel(days));

            var themetracker = new ThemeTracker({
                $el: this.$('.themetracker'),
                jobs: jobsConf,
                showDatepickers: true,
                days: days,
                width: this.$('.themetracker').width(),
                height: 2400,
                images: {
                    'details-btn': 'static-HEAD/img/cluster-detail.png'
                }
            });

            setInterval(this.updateTime, 1000);

            this.$('.label-controls').on('click', '.dropdown-menu a', function(evt){
                var text = $(evt.target).text();
                $(evt.delegateTarget).find('button span').text(text);
                themetracker.toggleDetails(text === 'Show all labels')
            });
        },

        updateTime: function(){
            const now = moment();
            this.$time.text(now.format('HH:mm:ss'));
            this.$date.text(now.format('ddd MMM Do YYYY'));
        }
    });
});
