/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
            this.initialised();
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
