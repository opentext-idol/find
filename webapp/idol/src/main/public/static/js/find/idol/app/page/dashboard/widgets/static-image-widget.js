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
    'jquery',
    './widget',
    'html2canvas'
], function($, Widget, html2canvas) {
    'use strict';

    return Widget.extend({
        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);
            this.url = this.widgetSettings.url;
        },

        render: function() {
            Widget.prototype.render.apply(this);

            const html = $('<div class="static-image" style=\'background-image: url("' + this.url + '")\'></div>');

            this.$content.html(html);
        },

        exportData: function(){
            const $imageEl = this.$('.static-image');
            if (!$imageEl.length) {
                return null;
            }

            const deferred = $.Deferred();
            html2canvas($imageEl[0], {
                useCORS: true,
                onrendered: function(canvas) {
                    try {
                        deferred.resolve(canvas.toDataURL('image/jpeg'));
                    }
                    catch (e) {
                        // canvas.toDataURL can throw exceptions in IE11 even if there's CORS headers on the background-image
                        deferred.resolve(null)
                    }
                }
            });

            return deferred.then(function(data){
                return data && {
                        data: {
                            image: data,
                            markers: []
                        },
                        type: 'map'
                    };
            });
        }
    });
});
