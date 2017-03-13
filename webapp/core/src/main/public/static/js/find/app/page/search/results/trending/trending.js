define([
    'backbone',
    'underscore',
    'jquery',
    'd3'
], function (Backbone, _, $, d3) {
    'use strict';

    const CHART_PADDING = 50;
    const TIME_FORMAT = d3.time.format("%Y-%m-%d");
    const NUMBER_OF_COLORS = 10;
    const FADE_OUT_OPACITY = 0.3;
    const LEGEND_WIDTH = 300;
    const LEGEND_MARKER_HEIGHT = 15;
    const LEGEND_TEXT_WIDTH = 100;
    const LEGEND_ITEM_HEIGHT = 25;


    return function Trending(options) {
        const getContainerCallback = options.getContainerCallback;

        return {
            draw: function (options) {
                const data = options.data;
                const names = options.names;
                const minDate = options.minDate;
                const maxDate = options.maxDate;
                const containerWidth = options.containerWidth;
                const containerHeight = options.containerHeight;
                const chartWidth = containerWidth - LEGEND_WIDTH;
                const chartHeight = containerHeight;
                const xAxisLabel = options.xAxisLabel;
                const yAxisLabel = options.yAxisLabel;
                const maxValue = _.max(_.map(data, function (d) {
                    return _.max(d, function (v) {
                        return v[1];
                    })[1]
                }));
                const minValue = _.min(_.map(data, function (d) {
                    return _.min(d, function (v) {
                        return v[1];
                    })[1]
                }));

                // Set the scales //
                const yScale = d3.scale.linear()
                    .domain([minValue, maxValue])
                    .range([chartHeight - CHART_PADDING, CHART_PADDING]);

                const xScale = d3.time.scale()
                    .domain([minDate, maxDate])
                    .range([CHART_PADDING, chartWidth - CHART_PADDING]);

                const yAxisScale = d3.svg.axis()
                    .scale(yScale)
                    .orient('left');

                const xAxisScale = d3.svg.axis()
                    .scale(xScale)
                    .orient('bottom')
                    .tickFormat(TIME_FORMAT);

                // Create the chart svg //
                const svg = d3.select(getContainerCallback())
                    .append('svg')
                    .attr('width', chartWidth + LEGEND_WIDTH)
                    .attr('height', chartHeight);

                // Draw a line on the chart for each value //
                const parametricValue = svg.selectAll('.data-item')
                    .data(data)
                    .enter()
                    .append('g')
                    .attr('class', function (d, i) {
                        return 'data-item color' + (i % NUMBER_OF_COLORS);
                    })
                    .attr('data-name', function (d, i) {
                        return names[i];
                    });

                const line = d3.svg.line()
                    .x(function (d) {
                        return xScale(d[0])
                    })
                    .y(function (d) {
                        return yScale(d[1])
                    })
                    .interpolate('linear');

                const lineAndPointMouseover = function () {
                    let newValue = this.parentNode.getAttribute('data-name');
                    d3.selectAll('.line')
                        .each(function () {
                            //noinspection JSPotentiallyInvalidUsageOfThis
                            if (this.parentNode.getAttribute('data-name') === newValue) {
                                d3.select(this)
                                    .attr('stroke-width', 5);
                            } else {
                                d3.select(this)
                                    .attr('opacity', FADE_OUT_OPACITY);
                            }
                        });

                    d3.selectAll('circle')
                        .each(function () {
                            //noinspection JSPotentiallyInvalidUsageOfThis
                            if (this.parentNode.getAttribute('data-name') === newValue) {
                                d3.select(this)
                                    .attr('r', 5);
                            } else {
                                d3.select(this)
                                    .attr('opacity', FADE_OUT_OPACITY)
                            }
                        })
                };

                const lineAndPointMouseout = function () {
                    d3.selectAll('.line')
                        .attr('opacity', 1)
                        .attr('stroke-width', 2);

                    d3.selectAll('circle')
                        .attr('opacity', 1)
                        .attr('r', 4);
                };

                parametricValue.append('path')
                    .attr('class', 'line')
                    .attr('stroke-width', 2)
                    .attr('fill', 'none')
                    .attr('d', function (d) {
                        return line(d);
                    })
                    .on('mouseover', lineAndPointMouseover)
                    .on('mouseout', lineAndPointMouseout);

                // Add the data points along the lines //
                const points = parametricValue.selectAll('circle')
                    .data(function (d) {
                        return d;
                    });

                points.enter()
                    .append('circle')
                    .attr('r', 4)
                    .attr('cy', function (d) {
                        return yScale(d[1]);
                    })
                    .attr('cx', function (d) {
                        return xScale(d[0]);
                    })
                    .attr('fill', 'white')
                    .attr('stroke-width', 3)
                    .on('mouseover', lineAndPointMouseover)
                    .on('mouseout', lineAndPointMouseout);


                // Add the x and y axes //
                const yAxis = svg.append('g')
                    .attr('class', 'y-axis')
                    .attr('transform', 'translate(' + CHART_PADDING + ',0)')
                    .call(yAxisScale);

                yAxis.append('text')
                    .attr('x', -(chartHeight/2))
                    .attr('y', -(CHART_PADDING/5*4))
                    .attr('transform', 'rotate(270)')
                    .text(yAxisLabel);

                const xAxis = svg.append('g')
                    .attr('class', 'x-axis')
                    .attr('transform', 'translate(0,' + (chartHeight - CHART_PADDING) + ')')
                    .call(xAxisScale);

                xAxis.append('text')
                    .attr('x', chartWidth/2)
                    .attr('y', CHART_PADDING/5*4)
                    .text(xAxisLabel);

                // Add the legend //
                const legend = svg.append('g')
                    .attr('class', 'legend')
                    .attr('x', chartWidth)
                    .attr('y', 0)
                    .attr('height', chartHeight)
                    .attr('width', LEGEND_WIDTH);

                const legendMouseover = function (d) {
                    d3.selectAll('.line')
                        .each(function (data) {
                            if (this.parentNode.getAttribute('data-name') === d) {
                                d3.select(this)
                                    .attr('stroke-width', 5);
                            } else {
                                d3.select(this)
                                    .attr('opacity', FADE_OUT_OPACITY);
                            }
                        });
                    d3.selectAll('circle')
                        .each(function (data) {
                            if (this.parentNode.getAttribute('data-name') === d) {
                                d3.select(this)
                                    .attr('r', 5);
                            } else {
                                d3.select(this)
                                    .attr('opacity', FADE_OUT_OPACITY);
                            }
                        });
                };

                const legendMouseout = function () {
                    d3.selectAll('.line')
                        .each(function () {
                            d3.select(this)
                                .attr('stroke-width', 2)
                                .attr('opacity', 1);
                        });
                    d3.selectAll('circle')
                        .attr('r', 4)
                        .attr('opacity', 1);
                };

                legend.selectAll('g')
                    .data(names)
                    .enter()
                    .append('g')
                    .each(function (d, i) {
                        const g = d3.select(this)
                            .attr('data-name', d)
                            .attr('class', 'color' + (i % NUMBER_OF_COLORS));

                        g.append('rect')
                            .attr('x', chartWidth)
                            .attr('y', (i + 1) * LEGEND_ITEM_HEIGHT)
                            .attr('width', LEGEND_MARKER_HEIGHT)
                            .attr('height', LEGEND_MARKER_HEIGHT)
                            .on('mouseover', legendMouseover)
                            .on('mouseout', legendMouseout);

                        g.append('text')
                            .attr('x', chartWidth + LEGEND_ITEM_HEIGHT)
                            .attr('y', (i + 1) * LEGEND_ITEM_HEIGHT + 12)
                            .attr('class', 'legend-text')
                            .attr('width', LEGEND_TEXT_WIDTH)
                            .attr('height', LEGEND_MARKER_HEIGHT)
                            .attr('cursor', 'default')
                            .text(d)
                            .on('mouseover', legendMouseover)
                            .on('mouseout', legendMouseout);

                    });
            }
        }

    }
});