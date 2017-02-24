/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/results/parametric-results-view',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'sunburst/js/sunburst',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/sunburst/sunburst-label.html',
    'd3'
], function(ParametricResultsView, _, $, i18n, Sunburst, generateErrorHtml, labelTemplate, d3) {
    'use strict';

    const SUNBURST_NAME_ATTR = 'text';
    const SUNBURST_SIZE_ATTR = 'count';
    const SUNBURST_FILTER_NUMBER = 'hiddenFilterCount';
    const STROKE_COLOUR = '#f0f0f0';

    const sunburstLabelIcon = '<i class="icon-zoom-out"></i>';
    const sunburstLabelTemplate = _.template(labelTemplate);

    function drawSunburst($el, data, onClick) {
        const color = d3.scale.category20c();
        $el.empty();

        this.sunburst = new Sunburst($el, {
            animate: false,
            i18n: i18n,
            nameAttr: SUNBURST_NAME_ATTR,
            sizeAttr: SUNBURST_SIZE_ATTR,
            strokeColour: STROKE_COLOUR,
            comparator: null,
            onClick: onClick,
            outerRingAnimateSize: 15,
            data: {
                text: i18n['search.sunburst.title'],
                children: data,
                count: _.reduce(_.pluck(data, SUNBURST_SIZE_ATTR), function(a, b) {
                    return a + b;
                })
            },
            colorFn: function(data) {
                if(!data.parent) {
                    // set the centre of the sunburst to always be white
                    return 'white';
                }

                if(data.hidden || data.parent.hidden) {
                    return STROKE_COLOUR;
                }

                if(!data.parent.parent) {
                    return data.color = data[SUNBURST_SIZE_ATTR] ? color(data.parent.children.indexOf(data)) : 'black';
                }

                return data.color = color(data[SUNBURST_NAME_ATTR]);
            },
            labelFormatter: function(data, prevClicked) {
                const zoomedOnRoot = !prevClicked || prevClicked.depth === 0;
                const hoveringCenter = prevClicked ? data === prevClicked.parent : data.depth === 0;

                const templateArguments = {
                    size: data[SUNBURST_SIZE_ATTR],
                    icon: !zoomedOnRoot && hoveringCenter ? sunburstLabelIcon : '',
                    noVal: false
                };

                if(data[SUNBURST_NAME_ATTR] === '') {
                    templateArguments.name = i18n['search.sunburst.noValue'](data[SUNBURST_FILTER_NUMBER]);
                    templateArguments.italic = true;
                    templateArguments.noVal = true;
                } else {
                    templateArguments.name = data[SUNBURST_NAME_ATTR];
                    templateArguments.italic = false;
                }

                return sunburstLabelTemplate(templateArguments);
            },
            hoverAnimation: function(d, arc, outerRingAnimateSize, arcEls, arcData, paper) {
                _.chain(_.zip(arcData, arcEls))
                    .filter(function(dataEl) {
                        const data = dataEl[0];

                        // TODO Assumes depth=2 is the outer ring - will need to change if this changes
                        return data.text !== '' && data.depth === 2 && data.text === d.text;
                    })
                    .each(function(dataEl) {
                        const el = dataEl[1];
                        paper.set(el).animate({path: arc(outerRingAnimateSize)(dataEl[0])}, 100);
                    });
            }
        });

        return this.sunburst;
    }

    return ParametricResultsView.extend({
        initialize: function(options) {
            ParametricResultsView.prototype.initialize.call(this, _.defaults({
                emptyDependentMessage: i18n['search.resultsView.sunburst.error.noDependentParametricValues'],
                emptyMessage: generateErrorHtml({errorLookup: 'emptySunburstView'}),
                errorMessageArguments: {messageToUser: i18n['search.resultsView.sunburst.error.query']}
            }, options));
        },

        update: function() {
            if(!this.parametricCollection.isEmpty()) {
                drawSunburst.call(this, this.$content, this.dependentParametricCollection.toJSON(), _.bind(this.onClick, this));

                const noValidChildren = _.chain(this.dependentParametricCollection.pluck('children'))
                    .compact()
                    .flatten()
                    .isEmpty()
                    .value();

                if(this.fieldsCollection.at(1).get('field') !== '' && noValidChildren) {
                    this.$message.text(i18n['search.resultsView.sunburst.error.noSecondFieldValues']);
                } else {
                    this.$message.empty();
                }
            }
        },

        render: function() {
            ParametricResultsView.prototype.render.apply(this, arguments);

            this.$content.addClass('sunburst');

            $(window).resize(_.bind(function() {
                if(this.sunburst) {
                    this.sunburst.resize();
                }
            }, this));
        }
    });
});
