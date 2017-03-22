define([
    'underscore',
    'jquery',
    'd3',
    'find/app/util/widget-zoom',
    'find/app/util/widget-drag'
], function (_, $, d3, widgetZoom, widgetDrag) {
    'use strict';

    const CHART_PADDING = 70;
    const NUMBER_OF_COLORS = 10;
    const FADE_OUT_OPACITY = 0.3;
    const POINT_RADIUS = 5;
    const LEGEND_WIDTH = 200;
    const LEGEND_MARKER_WIDTH = 15;
    const LEGEND_TEXT_WIDTH = 100;
    const LEGEND_TEXT_HEIGHT = 12;
    const LEGEND_TEXT_EMS = 1.1;
    const LEGEND_PADDING = 5;
    const LEGEND_TICK_PADDING = 15;
    const MILLISECONDS_TO_SECONDS = 1000;
    const SECONDS_IN_ONE_YEAR = 31556926;
    const SECONDS_IN_ONE_WEEK = 604800;
    const SECONDS_IN_ONE_DAY = 86400;


    function setScales(options, chartHeight, chartWidth) {
        const data = options.data;

        const maxValue = _.max(_.flatten(_.map(_.pluck(data, 'points'), function (point) {
            return _.pluck(point, 'count');
        })));
        const minValue = _.min(_.flatten(_.map(_.pluck(data, 'points'), function (point) {
            return _.pluck(point, 'count');
        })));

        const yScale = d3.scale.linear()
            .domain([minValue, maxValue])
            .range([chartHeight - CHART_PADDING, CHART_PADDING / 2]);

        const xScale = d3.time.scale()
            .domain([options.minDate, options.maxDate])
            .range([CHART_PADDING, chartWidth - CHART_PADDING]);

        return {
            yScale: yScale,
            xScale: xScale
        };
    }

    function setHoverFunctionality(chart, scales, chartHeight, tooltipText, timeFormat) {
        const mouseover = function (valueName) {
            d3.selectAll('.line')
                .each(function () {
                    if (this.parentNode.getAttribute('data-name') === valueName) {
                        d3.select(this)
                            .attr('stroke-width', POINT_RADIUS);
                    } else {
                        d3.select(this)
                            .attr('opacity', FADE_OUT_OPACITY);
                    }
                });
            d3.selectAll('circle')
                .each(function () {
                    if (this.parentNode.getAttribute('data-name') === valueName) {
                        d3.select(this)
                            .attr('r', 5);
                    } else {
                        d3.select(this)
                            .attr('opacity', FADE_OUT_OPACITY);
                    }
                });
            d3.selectAll('.legend-text')
                .each(function () {
                    if (this.parentNode.getAttribute('data-name') === valueName) {
                        d3.select(this)
                            .attr('font-size', '15')
                    } else {
                        d3.select(this)
                            .attr('font-size', '12')
                    }
                });
        };

        const mouseout = function () {
            d3.selectAll('.line')
                .attr('stroke-width', 2)
                .attr('opacity', 1);
            d3.selectAll('circle')
                .attr('r', 4)
                .attr('opacity', 1);
            d3.selectAll('.legend-text')
                .attr('font-size', '12')
        };

        const lineAndPointMouseover = function () {
            let valueName = d3.event.target.parentNode.getAttribute('data-name');
            mouseover(valueName);
        };

        const pointMouseover = function (d) {
            chart.append('line')
                .attr({
                    class: 'guide-line',
                    x1: CHART_PADDING,
                    y1: scales.yScale(d.count),
                    x2: scales.xScale(d.mid) - POINT_RADIUS / 2,
                    y2: scales.yScale(d.count)
                });
            chart.append('line')
                .attr({
                    class: 'guide-line',
                    x1: scales.xScale(d.mid),
                    y1: scales.yScale(d.count) + POINT_RADIUS / 2,
                    x2: scales.xScale(d.mid),
                    y2: chartHeight - CHART_PADDING
                });

            const title = tooltipText(
                d.count,
                this.parentNode.getAttribute('data-name'),
                timeFormat(d.min),
                timeFormat(d.max)
            );

            $(this).tooltip({
                title: title,
                container: 'body',
                placement: 'top',
                trigger: 'manual'
            });

            $(this).tooltip('show');

            lineAndPointMouseover();
        };

        const pointMouseout = function () {
            chart.selectAll('.guide-line')
                .remove();
            chart.selectAll('.chart-tooltip')
                .remove();
            $(this).tooltip('destroy');
            mouseout();
        };

        return {
            lineAndPointMouseover: lineAndPointMouseover,
            lineAndPointMouseout: mouseout,
            pointMouseover: pointMouseover,
            pointMouseout: pointMouseout,
            legendMouseover: mouseover,
            legendMouseout: mouseout
        }
    }

    function setAxes(chart, scales, chartHeight, chartWidth, yAxisLabel, timeFormat) {

        chart.selectAll('.y-axis').remove();
        chart.selectAll('.x-axis').remove();

        const yAxisScale = d3.svg.axis()
            .scale(scales.yScale)
            .orient('left');

        const xAxisScale = d3.svg.axis()
            .scale(scales.xScale)
            .orient('bottom')
            .tickFormat(timeFormat);

        const yAxis = chart.append('g')
            .attr({
                class: 'y-axis',
                transform: 'translate(' + CHART_PADDING + ',0)'
            })
            .call(yAxisScale);

        yAxis.append('text')
            .attr({
                x: -(chartHeight / 2),
                y: -(CHART_PADDING / 5 * 4),
                transform: 'rotate(270)'
            })
            .text(yAxisLabel);

        const xAxis = chart.append('g')
            .attr({
                class: 'x-axis',
                transform: 'translate(0,' + (chartHeight - CHART_PADDING) + ')'
            })
            .call(xAxisScale);

        xAxis.selectAll('.tick text')
            .call(labelWrap, chartWidth);
    }

    function labelWrap(labels, width) {
        const tickWidth = width / labels[0].length;

        labels.each(function () {
            const label = d3.select(this);
            const words = label.text().split(/\s+/).reverse();
            const y = label.attr("y");
            const dy = parseFloat(label.attr("dy"));
            let word;
            let lineNumber = 0;
            let line = [];
            let tspan = label.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy + "em");

            //noinspection AssignmentResultUsedJS
            while (word = words.pop()) {
                line.push(word);
                tspan.text(line.join(" "));
                //noinspection JSUnresolvedFunction
                if (tspan.node().getComputedTextLength() > tickWidth - LEGEND_TICK_PADDING) {
                    line.pop();
                    tspan.text(line.join(" "));
                    line = [word];
                    tspan = label.append("tspan").attr("x", 0).attr("y", y).attr("dy", ++lineNumber * LEGEND_TEXT_EMS + dy + "em").text(word);
                }
            }
        });
    }

    function getAdjustedLegendData(data, scales) {
        const labelData = _.map(data, function (datum, i) {
            return {
                index: i,
                name: datum.name,
                labelY: scales.yScale(datum.points[datum.points.length - 1].count),
                dataY: scales.yScale(datum.points[datum.points.length - 1].count)
            }
        });

        labelData.sort(function (a, b) {
            return a.labelY - b.labelY;
        });

        const maxScaledY = _.max(labelData, function (datum) {
            return datum.labelY;
        }).labelY;
        adjustLabelPositions(labelData, maxScaledY);
        return labelData;
    }

    function adjustLabelPositions(legendData, maxScaledY) {
        _.each(legendData, function (d, i) {
            if (i >= 1) {
                const prevVal = legendData[i - 1].labelY + LEGEND_TEXT_HEIGHT;
                d.labelY = d.labelY < prevVal ? prevVal : d.labelY;
            }
        });

        if (!_.isEmpty(legendData) && legendData[legendData.length - 1].labelY > maxScaledY) {
            legendData[legendData.length - 1].labelY = maxScaledY;
            legendData.reverse();
            _.each(legendData, function (d, i) {
                if (i >= 1) {
                    const prevVal = legendData[i - 1].labelY - LEGEND_TEXT_HEIGHT;
                    d.labelY = d.labelY > prevVal ? prevVal : d.labelY;
                }
            });
            legendData.reverse();
        }
    }

    function getTimeFormat(max, min) {
        const range = max.getTime() / MILLISECONDS_TO_SECONDS - min.getTime() / MILLISECONDS_TO_SECONDS;
        if (range > SECONDS_IN_ONE_YEAR) {
            return d3.time.format("%B %Y");
        }
        if (range < SECONDS_IN_ONE_DAY) {
            return d3.time.format("%H:%M:%S %d %B %Y");
        }
        if (range < SECONDS_IN_ONE_WEEK) {
            return d3.time.format("%H:%M %d %B %Y");
        }
        return d3.time.format("%d %B %Y");
    }

    function Trending(options) {
        this.el = options.el;

        this.chart = d3.select(this.el)
            .append('svg');
    }

    _.extend(Trending.prototype, {
        draw: function (options) {
            const reloaded = options.reloaded;
            const data = options.data;
            const minDate = options.minDate;
            const maxDate = options.maxDate;
            const chartWidth = $(this.el).width() - LEGEND_WIDTH;
            const chartHeight = $(this.el).height();
            const yAxisLabel = options.yAxisLabel;
            const tooltipText = options.tooltipText;
            const timeFormat = getTimeFormat(maxDate, minDate);

            const scales = setScales(options, chartHeight, chartWidth);

            const hover = setHoverFunctionality(this.chart, scales, chartHeight, tooltipText, timeFormat);

            const getIndexOfValueName = function(name) {
                return _.pluck(data, 'name').indexOf(name);
            };

            const line = d3.svg.line()
                .x(function (d) {
                    return scales.xScale(d.mid)
                })
                .y(function (d) {
                    return scales.yScale(d.count)
                })
                .interpolate('linear');

            this.chart.attr({
                width: $(this.el).width(),
                height: $(this.el).height()
            });

            if (this.dataJoin) {
                this.dataJoin = this.dataJoin
                    .data(data, function (d) {
                        return d.name;
                    });
            } else {
                this.dataJoin = this.chart.selectAll('.value')
                    .data(data, function (d) {
                        return d.name;
                    });
            }

            this.dataJoin.enter()
                .append('g')
                .attr('data-name', function (d) {
                    return d.name;
                })
                .append('path');

            this.dataJoin
                .attr('class', function (d) {
                    return 'value color' + (getIndexOfValueName(d.name) % NUMBER_OF_COLORS);
                });

            this.dataJoin.select('path')
                .attr({
                    class: 'line',
                    'stroke-width': 2,
                    fill: 'none'
                })
                .attr('d', function (d) {
                    return line(d.points);
                })
                .on('mouseover', hover.lineAndPointMouseover)
                .on('mouseout', hover.lineAndPointMouseout);

            this.dataJoin.exit().remove();

            if (this.pointsJoin && !reloaded) {
                this.pointsJoin = this.pointsJoin
                    .data(function (d) {
                        return d.points;
                    });
            } else {
                this.pointsJoin = this.dataJoin.selectAll('circle')
                    .data(function (d) {
                        return d.points;
                    });
            }

            this.pointsJoin
                .enter()
                .append('circle')
                .attr({
                    r: 4,
                    fill: 'white',
                    'stroke-width': 3
                });

            this.pointsJoin
                .attr('cy', function (d) {
                    return scales.yScale(d.count);
                })
                .attr('cx', function (d) {
                    return scales.xScale(d.mid);
                })
                .on('mouseover', hover.pointMouseover)
                .on('mouseout', hover.pointMouseout);

            this.pointsJoin.exit().remove();

            setAxes(this.chart, scales, chartHeight, chartWidth, yAxisLabel, timeFormat);

            this.chart.selectAll('.legend').remove();

            const legend = this.chart.append('g')
                .attr({
                    class: 'legend',
                    x: chartWidth,
                    y: 0,
                    height: chartHeight,
                    width: LEGEND_WIDTH
                });

            legend.selectAll('g')
                .data(getAdjustedLegendData(data, scales))
                .enter()
                .append('g')
                .each(function (d) {
                    const g = d3.select(this)
                        .attr({
                            'data-name': d.name,
                            class: 'color' + (d.index % NUMBER_OF_COLORS)
                        });

                    g.append('line')
                        .attr({
                            x1: chartWidth - CHART_PADDING + LEGEND_PADDING,
                            y1: d.dataY,
                            x2: chartWidth - CHART_PADDING + LEGEND_PADDING + LEGEND_MARKER_WIDTH,
                            y2: d.labelY,
                            'stroke-width': 2,
                            'stroke-dasharray': '3,2'
                        })
                        .on('mouseover', function () {
                            hover.legendMouseover(d.name);
                        })
                        .on('mouseout', function () {
                            hover.legendMouseout(d.name);
                        });

                    g.append('text')
                        .attr({
                            x: chartWidth - CHART_PADDING + LEGEND_MARKER_WIDTH + LEGEND_PADDING,
                            y: d.labelY + 4 - (LEGEND_PADDING / 2),
                            class: 'legend-text',
                            width: LEGEND_TEXT_WIDTH,
                            height: LEGEND_TEXT_HEIGHT,
                            cursor: 'default',
                            'font-size': LEGEND_TEXT_HEIGHT
                        })
                        .text(d.name)
                        .on('mouseover', function () {
                            hover.legendMouseover(d.name);
                        })
                        .on('mouseout', function () {
                            hover.legendMouseout(d.name);
                        });
                });

            widgetZoom.addZoomBehaviour({
                chart: this.chart,
                xScale: scales.xScale,
                scaleType: 'date',
                minValue: minDate.getTime() / MILLISECONDS_TO_SECONDS,
                maxValue: maxDate.getTime() / MILLISECONDS_TO_SECONDS,
                callback: options.zoomCallback
            });

            widgetDrag.addDragBehaviour({
                chart: this.chart,
                xScale: scales.xScale,
                scaleType: 'date',
                min: minDate.getTime() / MILLISECONDS_TO_SECONDS,
                max: maxDate.getTime() / MILLISECONDS_TO_SECONDS,
                dragMoveCallback: options.dragMoveCallback,
                dragEndCallback: options.dragEndCallback
            });
        }
    });

    return Trending;
});