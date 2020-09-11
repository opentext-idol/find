import _ from 'underscore';
import Backbone from 'backbone';
import * as d3 from 'd3';
import * as d3Sankey from 'd3-sankey';
import template from './view.html';

const MIN_TOPIC_SIZE_PCT = 0.45;

const CHART_ASPECT_RATIO = 1.5;
const CHART_PADDING = 6;
const CHART_EDGE_PADDING = CHART_PADDING;
const CHART_NODE_WIDTH = 4 * CHART_PADDING;
const CHART_NODE_PADDING = 2 * CHART_PADDING;
const CHART_NODE_BORDER_WIDTH = 0;
const CHART_SOURCE_LABEL_SIZE = 30; // NOTE: font-size
const CHART_SOURCE_LABEL_PADDING = 4 * CHART_PADDING;
const CHART_TOPIC_LABEL_SIZE = 16; // NOTE: font-size
const CHART_TOPIC_LABEL_PADDING = 2 * CHART_PADDING;

const CHART_BACKGROUND_COLOUR = 'none';
const CHART_NODE_BORDER_COLOUR = 'currentcolor';
const CHART_TEXT_COLOUR = 'currentcolor';
const CHART_SENTIMENT_COLOURS = {
    Positive: '#1aac60',
    Neutral: '#656668',
    Negative: '#e5004c'
};
const CHART_TOPIC_COLOURS = ['#dcdedf'];

const COLOURS_MF_BRIGHT =
    ['#3939c6', '#00abf3', '#43e4ff', '#1ffbba', '#75da4d', '#ffce00', '#eb23c2', '#ba47e2'];
const COLOURS_MF_DARK =
    ['#271782', '#014272', '#0b8eac', '#00a989', '#5bba36', '#ffb000', '#9b1e83', '#5216ac'];

const sum = function (ns) {
    return _.reduce(ns, function (total, n) {
        return total + n;
    }, 0)
}

const drawLinkCurveHorizontal = function (path, link) {
    const xMid = (link.source.x + link.target.x) / 2;
    path.bezierCurveTo(
        xMid, link.source.y,
        xMid, link.target.y,
        link.target.x, link.target.y);
}

const buildLinkPathHorizontal = function (link) {
    const links = _.map(['y0', 'y1'], function(yProperty) {
        return {
            source: { x: link.source.x1, y: link.source[yProperty] },
            target: { x: link.target.x0, y: link.target[yProperty] }
        };
    });
    const path = d3.path();
    path.moveTo(links[0].source.x, links[0].source.y);
    drawLinkCurveHorizontal(path, links[0]);
    path.lineTo(links[1].target.x, links[1].target.y);
    drawLinkCurveHorizontal(path, { source: links[1].target, target: links[1].source });
    path.closePath();
    return path;
}

const normInterest = function (interest) {
    return Math.round(Math.pow(interest, 0.7));
}

const processData = function (data) {
    const sourceMultipliers = {};
    const sourcesTotalInterest =
        _.object(_.pluck(data.sources, 'name'), _.pluck(data.sources, 'documents'));
    const sourcesTotalDocs = sum(_.pluck(data.sources, 'documents'));
    _.each(data.sources, function (source) {
        sourceMultipliers[source.name] = sourcesTotalDocs / source.documents;
    });

    const nodes = [];
    const links = [];
    _.each(data.topicsInterest, function (topicInterest, topic) {
        if (_.all(topicInterest.sources, function (sourceInterest, sourceName) {
            const sourceInterestRatio =
                sourceInterest.interest / sourcesTotalInterest[sourceName];
            return sourceInterestRatio < (MIN_TOPIC_SIZE_PCT / 100);
        })) {
            return;
        }

        _.each(data.sources, function (source, sourceIndex) {
            const sourceInterest = normInterest(
                sourceMultipliers[source.name] *
                topicInterest.sources[source.name].interest);
            if (sourceInterest <= 0) {
                return;
            }

            nodes.push({
                source: source.name,
                topic: topic,
                terms: topicInterest.terms,
                sentiment: topicInterest.sources[source.name].sentiment,
                fixedValue: sourceInterest
            });

            _.find(data.sources.slice(sourceIndex + 1), function (targetSource) {
                const targetInterest = normInterest(
                    sourceMultipliers[targetSource.name] *
                    topicInterest.sources[targetSource.name].interest);
                if (targetInterest <= 0) {
                    return false;
                }

                links.push({
                    source: source.name + ':' + topic,
                    target: targetSource.name + ':' + topic,
                    value: Math.min(sourceInterest, targetInterest),
                    sourceValue: sourceInterest,
                    targetValue: targetInterest
                });

                return true;
            });

        });
    });

    return {
        sources: _.pluck(data.sources, ['name']),
        nodes: nodes,
        links: links,
        nodeId: function (node) { return node.source + ':' + node.topic }
    }
}

const View = Backbone.View.extend({
    template: _.template(template),

    initialize: function (options) {
        this.data = processData(options.data);
    },

    render: function () {
        this.$el.html(this.template({}));
        this.$container = this.$('.interests-graph');
        this.showGraph();
    },

    showGraph: function () {
        const data = this.data;
        const sentimentColourScale = d3.scaleOrdinal()
            .domain(_.keys(CHART_SENTIMENT_COLOURS))
            .range(_.values(CHART_SENTIMENT_COLOURS));
        const topicColourScale = d3.scaleOrdinal().range(CHART_TOPIC_COLOURS);
        const width = this.$container.width();
        const height = Math.ceil(width / CHART_ASPECT_RATIO);

        this.$container.html('');

        const topPadding =
            CHART_EDGE_PADDING + CHART_SOURCE_LABEL_SIZE + CHART_SOURCE_LABEL_PADDING;
        const sankey = d3Sankey.sankey()
            .nodes(data.nodes)
            .links(data.links)
            .nodeId(data.nodeId)
            .nodeAlign(function (node) { return data.sources.indexOf(node.source) })
            .extent([
                [CHART_EDGE_PADDING, topPadding],
                [width - 2 * CHART_EDGE_PADDING, height - topPadding - CHART_EDGE_PADDING]
            ])
            .nodeWidth(CHART_NODE_WIDTH)
            .nodePadding(CHART_NODE_PADDING);
        const graph = sankey();

        const chart = d3.select(this.$container.get(0))
            .append('svg')
            .attr('width', width)
            .attr('height', height)
            .style('background', CHART_BACKGROUND_COLOUR);

        // boxes for source interest
        chart.append('g')
            .attr('stroke', CHART_NODE_BORDER_COLOUR)
            .attr('stroke-width', CHART_NODE_BORDER_WIDTH + 'px')
            .selectAll('rect')
            .data(graph.nodes)
            .join('rect')
            .attr('x', function (node) { return node.x0; })
            .attr('y', function (node) { return node.y0; })
            .attr('width', function (node) { return node.x1 - node.x0; })
            .attr('height', function (node) { return node.y1 - node.y0; })
            .attr('fill', function (node) { return sentimentColourScale(node.sentiment) })
            .append('title')
            .text(function (node) { return node.sentiment + ' sentiment'; });

        // links between sources
        chart.append('g')
            .attr('fill', 'none')
            // links blend with each other but not the background
            .style('isolation', 'isolate')
            .selectAll('path')
            .data(graph.links)
            .join('path')
            .attr('d', buildLinkPathHorizontal)
            .attr('stroke-width', 0)
            .attr('fill', function (link) { return topicColourScale(link.source.topic); })
            .style('mix-blend-mode', 'multiply')
            .append('title')
            .text(function (link) { return link.source.terms.join(', '); });

        // source labels at the top
        chart.append('g')
            .style('font-size', CHART_SOURCE_LABEL_SIZE + 'px')
            .selectAll('text')
            .data(_.map(_.groupBy(graph.nodes, 'source'), _.first))
            .join('text')
            .text(function (node) { return node.source; })
            .attr('x', function (node) {
                return (node.x0 < width / 2) ? node.x0 : node.x1;
            })
            .attr('text-anchor', function (node) {
                return (node.x0 <= width / 2) ? 'start' : 'end';
            })
            .attr('y', function (node) { return CHART_EDGE_PADDING; })
            .attr('dy', CHART_SOURCE_LABEL_SIZE)
            .attr('fill', CHART_TEXT_COLOUR);

        // topic labels on the links
        chart.append('g')
            .style('font-size', CHART_TOPIC_LABEL_SIZE + 'px')
            .selectAll('text')
            .data(_.filter(graph.nodes, function (node) {
                return node.source === data.sources[1];
            }))
            .join('text')
            .text(function (node) { return node.topic; })
            .attr('x', function (node) { return node.x1 + CHART_TOPIC_LABEL_PADDING; })
            .attr('y', function (node) { return (node.y0 + node.y1) / 2; })
            .attr('dy', CHART_TOPIC_LABEL_SIZE * 0.25)
            .attr('text-anchor', 'start')
            .attr('fill', CHART_TEXT_COLOUR)
            .append('title')
            .text(function (node) { return node.terms.join(', '); });
    }
});

export default {
    render: function (data) {
        return new View({ data: data });
    }
};
