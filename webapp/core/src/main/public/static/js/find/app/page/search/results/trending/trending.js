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
    'underscore',
    'jquery',
    'd3',
    'find/app/util/widget-zoom',
    'find/app/util/widget-drag'
], function(_, $, d3, widgetZoom, widgetDrag) {
    'use strict';

    const COLORS = [
        {name: 'blue', hex: '#1f77b4'},
        {name: 'light-blue', hex: '#6baed6'},
        {name: 'orange', hex: '#ff7f0e'},
        {name: 'pink', hex: '#e377c2'},
        {name: 'green', hex: '#2ca02c'},
        {name: 'light-green', hex: '#98df8a'},
        {name: 'red', hex: '#d62728'},
        {name: 'light-pink', hex: '#ff9896'},
        {name: 'purple', hex: '#9467bd'},
        {name: 'yellow', hex: '#e7ba52'}
    ];

    const CHART_PADDING = 80;
    const AXIS_DASHED_LINE_LENGTH = 15;
    const FADE_OUT_OPACITY = 0.3;
    const POINT_RADIUS = 5;
    const LEGEND_MARKER_WIDTH = 15;
    const LEGEND_TEXT_HEIGHT = 12;
    const LEGEND_PADDING = 5;
    const MILLISECONDS_TO_SECONDS = 1000;
    const SECONDS_IN_ONE_YEAR = 31556926;
    const SECONDS_IN_ONE_WEEK = 604800;
    const SECONDS_IN_ONE_DAY = 86400;

    function setScales(options, chartHeight, chartWidth) {
        return {
            xScale: d3.time.scale()
                .domain([options.minDate, options.maxDate])
                .range([CHART_PADDING, chartWidth]),
            yScale: d3.scale.linear()
                .domain([options.chartData.minRate, options.chartData.maxRate])
                .range([chartHeight - CHART_PADDING, CHART_PADDING / 2])
        };
    }

    function createHoverCallbacks(hoverEnabled, chart, scales, chartHeight, tooltipText, yUnitText, timeFormat) {
        if(!hoverEnabled) {
            return {
                lineAndPointMouseover: _.noop,
                lineAndPointMouseout: _.noop,
                pointMouseover: _.noop,
                pointMouseout: _.noop,
                legendMouseover: _.noop,
                legendMouseout: _.noop
            }
        }

        const mouseover = function mouseoverFn(valueName) {
            d3.selectAll('.line')
                .each(function() {
                    if(this.parentNode.getAttribute('data-name') === valueName) {
                        d3.select(this)
                            .attr('stroke-width', POINT_RADIUS);
                    } else {
                        d3.select(this)
                            .attr('opacity', FADE_OUT_OPACITY);
                    }
                });
            d3.selectAll('circle')
                .each(function() {
                    if(this.parentNode.getAttribute('data-name') === valueName) {
                        d3.select(this)
                            .attr('r', 5);
                    } else {
                        d3.select(this)
                            .attr('opacity', FADE_OUT_OPACITY);
                    }
                });
            d3.selectAll('.legend-text')
                .each(function() {
                    if(this.parentNode.getAttribute('data-name') === valueName) {
                        d3.select(this)
                            .attr('class', 'legend-text bold')
                    } else {
                        d3.select(this)
                            .attr('class', 'legend-text')
                    }
                });
        };

        const mouseout = function mouseoutFn() {
            d3.selectAll('.line')
                .attr('stroke-width', 2)
                .attr('opacity', 1);
            d3.selectAll('circle')
                .attr('r', 4)
                .attr('opacity', 1);
            d3.selectAll('.legend-text')
                .attr('class', 'legend-text')
        };

        const lineAndPointMouseover = function lineAndPointMouseoverFn() {
            let valueName = d3.event.target.parentNode.getAttribute('data-name');
            mouseover(valueName);
        };

        const pointMouseover = function pointMouseoverFn(d) {
            $(d3.event.target).tooltip('destroy');

            const xMid = scales.xScale(d.mid);
            const yCount = scales.yScale(d.rate);

            chart.append('line')
                .attr('class', 'guide-line')
                .attr('x1', CHART_PADDING - AXIS_DASHED_LINE_LENGTH)
                .attr('y1', yCount)
                .attr('x2', xMid - POINT_RADIUS / 2)
                .attr('y2', yCount);

            chart.append('line')
                .attr('class', 'guide-line')
                .attr('x1', xMid)
                .attr('y1', yCount + POINT_RADIUS / 2)
                .attr('x2', xMid)
                .attr('y2', chartHeight - CHART_PADDING);

            const title = tooltipText(
                d.rate.toPrecision(2),
                yUnitText,
                _.escape(this.parentNode.getAttribute('data-name')),
                timeFormat(d.min),
                timeFormat(d.max)
            );

            $(d3.event.target)
                .tooltip({
                    title: title,
                    container: 'body',
                    placement: 'top',
                    trigger: 'manual',
                    html: true
                })
                .tooltip('show');

            lineAndPointMouseover();
        };

        const pointMouseout = function pointMouseoutFn() {
            chart
                .selectAll('.guide-line')
                .remove();
            $(d3.event.target).tooltip('destroy');
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
        chart.selectAll('.dashed-axis-line').remove();

        const yAxisScale = d3.svg.axis()
            .scale(scales.yScale)
            .orient('left')
            .outerTickSize(0);

        const xAxisScale = d3.svg.axis()
            .scale(scales.xScale)
            .orient('bottom')
            .tickFormat(timeFormat)
            .outerTickSize(0);

        const yAxis = chart.append('g')
            .attr('class', 'y-axis')
            .attr('transform', 'translate(' + (CHART_PADDING - AXIS_DASHED_LINE_LENGTH) + ',0)')
            .call(yAxisScale);

        yAxis.append('text')
            .attr('x', -(CHART_PADDING + chartHeight) / 2)
            .attr('y', -CHART_PADDING / 3 * 2)
            .attr('transform', 'rotate(270)')
            .text(yAxisLabel);

        const xAxis = chart.append('g')
            .attr('class', 'x-axis')
            .attr('transform', 'translate(0,' + (chartHeight - CHART_PADDING) + ')')
            .call(xAxisScale);

        if(foreignObjectSupported()) {
            xAxis.selectAll('.tick text')
                .call(htmlLabelWrap);
        } else {
            xAxis.selectAll('.tick text')
                .call(textLabelWrap, chartWidth);
        }

        chart.append('line')
            .attr('class', 'dashed-axis-line')
            .attr('x1', CHART_PADDING - AXIS_DASHED_LINE_LENGTH)
            .attr('y1', chartHeight - CHART_PADDING)
            .attr('x2', CHART_PADDING)
            .attr('y2', chartHeight - CHART_PADDING)
            .attr('stroke-width', 2)
            .attr('stroke-dasharray', '2,2')
            .attr('stroke', 'gray');
    }

    function foreignObjectSupported() {
        const toStringFnc = ({}).toString;
        return !!document.createElementNS &&
            /SVGForeignObject/.test(toStringFnc.call(document.createElementNS('http://www.w3.org/2000/svg', 'foreignObject')));
    }

    function htmlLabelWrap(labelWrapper) {
        const tickPadding = 5;
        const labels = labelWrapper[0];
        const numberOfTicks = labels.length;
        const getTickTranslation = function getTickTranslationFn(label) {
            return d3.transform(label.node().parentNode.getAttribute('transform')).translate[0];
        };

        let prevLabelTick = 0;
        let currentLabel = d3.select(labels[0]);
        for(let i = 0; i < numberOfTicks; i++) {
            const currentLabelTick = getTickTranslation(currentLabel);
            const text = currentLabel.text();

            let nextLabel;
            let nextLabelTick;
            let widthToDouble;
            if(i === numberOfTicks - 1) {
                widthToDouble = (currentLabelTick - prevLabelTick);
            } else {
                nextLabel = d3.select(labels[i + 1]);
                nextLabelTick = getTickTranslation(nextLabel);
                widthToDouble = prevLabelTick
                    ? Math.min((nextLabelTick - currentLabelTick), (currentLabelTick - prevLabelTick))
                    : nextLabelTick - currentLabelTick;
            }

            d3.select(currentLabel.node().parentNode)
                .append('foreignObject')
                .attr('class', 'x-axis-label')
                .attr('width', widthToDouble - tickPadding)
                .attr('transform', 'translate(' + (-(widthToDouble - tickPadding) / 2) + ', 0)')
                .attr('height', CHART_PADDING)
                .attr('cursor', 'default')
                .append('xhtml:p')
                .html(text);

            prevLabelTick = currentLabelTick;
            currentLabel = nextLabel;
        }

        d3.selectAll('.x-axis text').remove();
    }

    function textLabelWrap(labels, width) {
        const tickPadding = 5;
        const tickWidth = width / labels[0].length;

        labels.each(function() {
            const label = d3.select(this);
            const words = label.text().split(/\s+/).reverse();
            const y = label.attr("y");
            const dy = parseFloat(label.attr("dy"));
            let word;
            let lineNumber = 0;
            let line = [];
            let tspan = label.text(null).append("tspan").attr("x", 0).attr("y", y).attr("dy", dy + "em");

            //noinspection AssignmentResultUsedJS
            while(word = words.pop()) {
                line.push(word);
                tspan.text(line.join(" "));
                //noinspection JSUnresolvedFunction
                if(tspan.node().getComputedTextLength() > tickWidth - tickPadding) {
                    line.pop();
                    tspan.text(line.join(" "));
                    line = [word];
                    tspan = label.append("tspan").attr("x", 0).attr("y", y).attr("dy", ++lineNumber + dy + "em").text(word);
                }
            }
        });
    }

    function getAdjustedLegendData(data, scales) {
        const labelData = _.map(data, function(datum, i) {
            return {
                index: i,
                name: datum.name,
                color: datum.color,
                labelY: scales.yScale(datum.points[datum.points.length - 1].rate),
                dataY: scales.yScale(datum.points[datum.points.length - 1].rate)
            }
        });

        labelData.sort(function(a, b) {
            const difference = a.labelY - b.labelY;
            return difference === 0
                ? a.index - b.index
                : difference;
        });

        const maxScaledY = _.max(labelData, function(datum) {
            return datum.labelY;
        }).labelY;
        adjustLabelPositions(labelData, maxScaledY);
        return labelData;
    }

    function adjustLabelPositions(legendData, maxScaledY) {
        _.each(legendData, function(d, i) {
            if(i > 0) {
                const prevVal = legendData[i - 1].labelY + LEGEND_TEXT_HEIGHT;
                d.labelY = d.labelY < prevVal
                    ? prevVal
                    : d.labelY;
            }
        });

        if(!_.isEmpty(legendData) && legendData[legendData.length - 1].labelY > maxScaledY) {
            legendData[legendData.length - 1].labelY = maxScaledY;
            legendData.reverse();
            _.each(legendData, function(d, i) {
                if(i > 0) {
                    const prevVal = legendData[i - 1].labelY - LEGEND_TEXT_HEIGHT;
                    d.labelY = d.labelY > prevVal
                        ? prevVal
                        : d.labelY;
                }
            });
            legendData.reverse();
        }
    }

    function getTimeFormat(max, min) {
        const range = max.getTime() / MILLISECONDS_TO_SECONDS - min.getTime() / MILLISECONDS_TO_SECONDS;
        if(range > SECONDS_IN_ONE_YEAR) {
            return d3.time.format("%b %Y");
        } else if(range < SECONDS_IN_ONE_DAY) {
            return d3.time.format("%H:%M:%S %d&nbsp;%b %Y");
        } else if(range < SECONDS_IN_ONE_WEEK) {
            return d3.time.format("%H:%M %d&nbsp;%b %Y");
        } else {
            return d3.time.format("%d&nbsp;%b %Y");
        }
    }

    function getIESafeTimeFormat(max, min) {
        const range = max.getTime() / MILLISECONDS_TO_SECONDS - min.getTime() / MILLISECONDS_TO_SECONDS;
        if(range > SECONDS_IN_ONE_YEAR) {
            return d3.time.format("%b %Y");
        } else if(range < SECONDS_IN_ONE_DAY) {
            return d3.time.format("%H:%M:%S %d %b %Y");
        } else if(range < SECONDS_IN_ONE_WEEK) {
            return d3.time.format("%H:%M %d %b %Y");
        } else {
            return d3.time.format("%d %b %Y");
        }
    }

    function getColor(data, d) {
        const color = d.color
            ? _.findWhere(COLORS, {name: d.color})
            : COLORS[_.pluck(data, 'name').indexOf(d.name) % COLORS.length];
        return color.hex;
    }

    function addDragAndZoomRectangle(chart, scales) {
        chart.selectAll('.graph-area').remove();

        const xScaleRange = scales.xScale.range();
        const yScaleRange = scales.yScale.range();
        return chart.insert("rect", "g")
            .attr('class', 'graph-area')
            .attr("x", xScaleRange[0])
            .attr("y", yScaleRange[1])
            .attr("width", xScaleRange[1] - xScaleRange[0])
            .attr("height", yScaleRange[0] - yScaleRange[1])
            .style("fill", "none")
            .style("pointer-events", "all");
    }

    function Trending(settings) {
        this.el = settings.el;
        this.tooltipText = settings.tooltipText;
        this.yAxisUnitsText = settings.yAxisUnitsText;
        this.yAxisLabelForUnit = settings.yAxisLabelForUnit;
        this.zoomEnabled = settings.zoomEnabled;
        this.dragEnabled = settings.dragEnabled;
        this.hoverEnabled = settings.hoverEnabled;

        this.chart = d3.select(this.el)
            .append('svg');
    }

    _.extend(Trending.prototype, {
        colors: COLORS,

        draw: function(options) {
            const reloaded = options.reloaded;
            const data = options.chartData.data;
            const minDate = options.minDate;
            const maxDate = options.maxDate;
            const $el = $(this.el);
            const elWidth = $el.width();
            const elHeight = $el.height();
            const legendWidth = elWidth / 7;
            const chartWidth = elWidth - legendWidth;
            const chartHeight = elHeight;
            const yUnitText = this.yAxisUnitsText(options.chartData.yUnit);

            const timeFormat = foreignObjectSupported()
                ? getTimeFormat(maxDate, minDate)
                : getIESafeTimeFormat(maxDate, minDate);

            const scales = setScales(options, chartHeight, chartWidth);

            const hoverCallbacks = createHoverCallbacks(
                this.hoverEnabled,
                this.chart,
                scales,
                chartHeight,
                this.tooltipText,
                yUnitText,
                timeFormat
            );

            const line = d3.svg.line()
                .x(function(d) {
                    return scales.xScale(d.mid)
                })
                .y(function(d) {
                    return scales.yScale(d.rate)
                })
                .interpolate('linear');

            this.chart
                .attr('width', elWidth)
                .attr('height', elHeight);

            if(this.dataJoin) {
                this.dataJoin = this.dataJoin
                    .data(data, function(d) {
                        return d.name;
                    });
            } else {
                this.dataJoin = this.chart.selectAll('.value')
                    .data(data, function(d) {
                        return d.name;
                    });
            }

            this.dataJoin.enter()
                .append('g')
                .attr('data-name', function(d) {
                    return d.name;
                })
                .append('path');

            this.dataJoin
                .attr('stroke', function(d) {
                    return getColor(data, d);
                });

            this.dataJoin.select('path')
                .attr('class', 'line')
                .attr('stroke-width', 2)
                .attr('fill', 'none')
                .attr('d', function(d) {
                    return line(d.points);
                })
                .on('mouseover', hoverCallbacks.lineAndPointMouseover)
                .on('mouseout', hoverCallbacks.lineAndPointMouseout);

            this.dataJoin.exit().remove();

            if(this.pointsJoin && !reloaded) {
                this.pointsJoin = this.pointsJoin
                    .data(function(d) {
                        return d.points;
                    });
            } else {
                this.pointsJoin = this.dataJoin
                    .selectAll('circle')
                    .data(function(d) {
                        return d.points;
                    });
            }

            this.pointsJoin
                .enter()
                .append('circle')
                .attr('r', 4)
                .attr('fill', 'white')
                .attr('stroke-width', 3);

            this.pointsJoin
                .attr('cy', function(d) {
                    return scales.yScale(d.rate);
                })
                .attr('cx', function(d) {
                    return scales.xScale(d.mid);
                })
                .on('mouseover', hoverCallbacks.pointMouseover)
                .on('mouseout', hoverCallbacks.pointMouseout);

            this.pointsJoin.exit().remove();

            setAxes(this.chart, scales, chartHeight, chartWidth, this.yAxisLabelForUnit(yUnitText), timeFormat);

            this.chart.selectAll('.legend').remove();

            this.chart
                .append('g')
                .attr('class', 'legend')
                .attr('x', chartWidth)
                .attr('y', 0)
                .attr('height', chartHeight)
                .attr('width', legendWidth)
                .selectAll('g')
                .data(getAdjustedLegendData(data, scales))
                .enter()
                .append('g')
                .each(function(d) {
                    const g = d3.select(this)
                        .attr('stroke', getColor(data, d))
                        .attr('data-name', d.name);

                    g.append('line')
                        .attr('x1', chartWidth + LEGEND_PADDING)
                        .attr('y1', d.dataY)
                        .attr('x2', chartWidth + LEGEND_PADDING + LEGEND_MARKER_WIDTH)
                        .attr('y2', d.labelY)
                        .attr('stroke-width', 2)
                        .attr('stroke-dasharray', '3,2')
                        .on('mouseover', function() {
                            hoverCallbacks.legendMouseover(d.name);
                        })
                        .on('mouseout', function() {
                            hoverCallbacks.legendMouseout(d.name);
                        });

                    if(foreignObjectSupported()) {
                        g.append('foreignObject')
                            .attr('class', 'legend-text')
                            .attr('x', chartWidth + LEGEND_MARKER_WIDTH + LEGEND_PADDING)
                            .attr('y', d.labelY - (2 * LEGEND_PADDING))
                            .attr('width', legendWidth - LEGEND_MARKER_WIDTH - LEGEND_PADDING)
                            .attr('height', LEGEND_TEXT_HEIGHT)
                            .attr('cursor', 'default')
                            .append('xhtml:p')
                            .html(d.name)
                            .on('mouseover', function() {
                                hoverCallbacks.legendMouseover(d.name);
                            })
                            .on('mouseout', function() {
                                hoverCallbacks.legendMouseout(d.name);
                            });
                    } else {
                        g.append('text')
                            .attr('class', 'legend-text')
                            .attr('x', chartWidth + LEGEND_MARKER_WIDTH + LEGEND_PADDING)
                            .attr('y', d.labelY)
                            .attr('width', legendWidth - LEGEND_MARKER_WIDTH - LEGEND_PADDING)
                            .attr('cursor', 'default')
                            .attr('fill', 'black')
                            .attr('stroke', 'none')
                            .attr('font-size', LEGEND_TEXT_HEIGHT)
                            .text(d.name)
                            .on('mouseover', function() {
                                hoverCallbacks.legendMouseover(d.name);
                            })
                            .on('mouseout', function() {
                                hoverCallbacks.legendMouseout(d.name);
                            });
                    }

                });

            const graphArea = addDragAndZoomRectangle(this.chart, scales);
            const behaviourOptions = {
                chart: graphArea,
                xScale: scales.xScale,
                scaleType: 'date',
                min: minDate.getTime() / MILLISECONDS_TO_SECONDS,
                max: maxDate.getTime() / MILLISECONDS_TO_SECONDS
            };

            if(this.zoomEnabled) {
                widgetZoom.addZoomBehaviour(_.extend({
                    callback: options.zoomCallback
                }, behaviourOptions));
            }

            if(this.dragEnabled) {
                widgetDrag.addDragBehaviour(_.extend({
                    dragMoveCallback: options.dragMoveCallback,
                    dragEndCallback: options.dragEndCallback
                }, behaviourOptions));
            }
        }
    });

    return Trending;
});
