/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'jquery',
    './widget',
    'html2canvas'
], function($, Widget) {
    'use strict';

    return Widget.extend({
        initialize: function(options) {
            Widget.prototype.initialize.apply(this, arguments);

            this.url = options.widgetSettings.url;
        },

        render: function() {
            Widget.prototype.render.apply(this);

            const html = $('<div class="static-image" style=\'background-image: url("' + this.url + '")\'></div>');

            this.$content.html(html);
        },

        exportPPTData: function(){
            var $imageEl = this.$('.static-image');

            if (!$imageEl.length) {
                return
            }

            var deferred = $.Deferred();
            html2canvas($imageEl[0], {
                useCORS: true,
                onrendered: function(canvas) {
                    try {
                        deferred.resolve({
                            image: canvas.toDataURL('image/jpeg'),
                            markers: []
                        });
                    }
                    catch (e) {
                        // canvas.toDataURL can throw exceptions in IE11 even if there's CORS headers on the background-image
                        deferred.resolve(null)
                    }
                }
            });

            return deferred.then(function(data){
                return data && {
                    data: data,
                    type: 'map'
                }
            });
        }
    });
});
