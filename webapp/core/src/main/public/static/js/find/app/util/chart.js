/*
 * Copyright 2018 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'flot',
    'flot.time'
], function(_, $){
    return function($container){
        $container.find('chart').map(function(idx, el){
            var $el = $(el);
            var label = $el.attr('label');
            var points = $el.attr('points').split('|').map(function(xy){
                return xy.split(',').map(function(val){
                    return Number(val)
                });
            });

            var $chartEl = $('<div class="conversation-chart">');

            $el.replaceWith($chartEl);

            var $tooltip = $('<div class="conversation-chart-tooltip">').appendTo($chartEl.parent()).hide();

            $.plot($chartEl, [{
                data: points,
                color: '#0DD7B9',
                label: label,
                series: {
                    lines: { show: true },
                    points: {
                        radius: 3,
                        show: true,
                        fill: true
                    }
                }
            }], {
                grid: {
                    hoverable: true
                },
                xaxis: {
                    mode: 'time',
                    font: {
                        color: '#545454',
                        size: 9
                    }
                },
                yaxis: {
                    font: {
                        color: '#545454',
                        size: 10
                    }
                }
            });

            $chartEl.on('plothover', function(evt, pos, item){
                if (item) {
                    var date = item.series.xaxis.tickFormatter(item.datapoint[0], item.series.xaxis),
                        val = Number(item.datapoint[1]);
                    $tooltip.html(date + ': ' + val);

                    var color = item.series.color;
                    var lighter = $.color.parse(color).scale('a', 0.8);
                    var width = $tooltip.width();
                    var height = $tooltip.height();
                    var parentNode = $tooltip[0].parentNode;
                    var offset = $(parentNode).offset();
                    $tooltip.css({
                        top: pos.pageY - offset.top - height - 20,
                        left: Math.min(Math.max(5, pos.pageX - offset.left - 0.5 * width), window.screen.availWidth - width - 5),
                        borderColor: color,
                        backgroundColor: lighter.toString()
                    }).fadeIn(200);
                } else {
                    $tooltip.finish().hide()
                }
            }).on('mouseout', function(){
                $tooltip.finish().hide()
            })
        });
    }
});