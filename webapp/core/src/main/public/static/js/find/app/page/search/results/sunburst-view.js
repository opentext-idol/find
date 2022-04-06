/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'find/app/configuration',
    'sunburst/js/sunburst',
    'find/app/page/search/results/parametric-results-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/results/sunburst/sunburst-label.html',
    'find/app/vent'
], function(_, $, d3, configuration, Sunburst, ParametricResultsView, i18n, labelTemplate, vent) {
    'use strict';

    const HIDDEN_COLOR = '#f0f0f0';

    const sunburstLabelIcon = '<i class="icon-zoom-out"></i>';
    const sunburstLabelTemplate = _.template(labelTemplate);

    function generateDataRoot(data) {
        return {
            text: i18n['search.sunburst.title'],
            children: data,
            count: _.reduce(_.pluck(data, 'count'), function(a, b) {
                return a + b;
            })
        }
    }

    function drawSunburst($el, data, onClick) {
        const color = d3.scale.category20c();
        $el.empty();

        return new Sunburst($el, {
            animate: false,
            nameAttr: 'text',
            sizeAttr: 'count',
            comparator: function(x, y) {
                const hiddenComparison = x.hidden - y.hidden;
                if(hiddenComparison !== 0) {
                    return hiddenComparison;
                }

                const valueComparison = y.value - x.value;
                if(valueComparison !== 0) {
                    return valueComparison;
                }

                return d3.ascending(x, y);
            },
            clickCallback: onClick,
            outerRingAnimateSize: 15,
            data: data,
            fillColorFn: function(datum) {
                if(!datum.parent) {
                    // Set the centre of the Sunburst to a fixed color
                    return 'white';
                }

                if(datum.hidden || datum.parent.hidden) {
                    return HIDDEN_COLOR;
                }

                // First tier sector
                if(datum.parent.parent) {
                    // Second tier sector
                    datum.color = color(datum.text);
                } else {
                    datum.color = datum.count
                        ? color(datum.parent.children.indexOf(datum))
                        : 'black';
                }

                return datum.color;
            },
            labelFormatter: function(datum, prevClicked) {
                const zoomedOnRoot = !prevClicked || prevClicked.depth === 0;
                const hoveringCenter = prevClicked
                    ? datum === prevClicked.parent
                    : datum.depth === 0;
                const textIsEmpty = datum.text === '';

                const templateArguments = {
                    size: datum.count,
                    icon: !zoomedOnRoot && hoveringCenter
                        ? sunburstLabelIcon
                        : '',
                    hasValue: !textIsEmpty,
                    italic: textIsEmpty
                };

                const hiddenFilterCount = datum.hiddenFilterCount;
                if(hiddenFilterCount > 0) {
                    // Child comprises values hidden by dependentParametricCollection
                    templateArguments.name = i18n['search.sunburst.tooSmall'](hiddenFilterCount);
                } else {
                    templateArguments.name = hiddenFilterCount === 0
                        // Child comprises results with no values for secondary parametric field
                        ? i18n['search.sunburst.missingValues'](datum.count, this.fieldsCollection.at(1).get('displayName'))
                        : datum.text;
                }

                return sunburstLabelTemplate(templateArguments);
            }.bind(this),
            hoverCallback: function(hoveredDatum, arc, outerRingAnimateSize, arcEls, arcData, svg) {
                const maxDepth = _.max(arcData, 'depth').depth
                svg.selectAll('path')
                    .filter(function(d) {
                        // We want to expand the outermost layer on hover.
                        return d.text !== '' && d.depth === maxDepth && d.text === hoveredDatum.text;
                    })
                    .each(function(d) {
                        d3.select(this)
                            .transition()
                            .duration(100)
                            .attr('d', arc(outerRingAnimateSize));
                    });
            }
        });
    }

    return ParametricResultsView.extend({
        initialize: function(options) {
            this.queryModel = options.queryModel;
            const config = configuration();
            this.allowMultipleDatabases = config.sunburst && config.sunburst.allowMultipleDatabases;
            if (this.allowMultipleDatabases === undefined || this.allowMultipleDatabases === null) {
                this.allowMultipleDatabases = true;
            }

            ParametricResultsView.prototype.initialize.call(this, _.defaults({
                emptyDependentMessage: i18n['search.resultsView.sunburst.noDependentParametricValues'],
                emptyMessage: i18n['search.resultsView.sunburst.noParametricValues'],
                errorMessageArguments: {messageToUser: i18n['search.resultsView.sunburst.error.query']}
            }, options));

            this.listenTo(vent, 'vent:resize', function() {
                if(this.sunburst && this.$content.is(':visible')) {
                    this.sunburst.resize();
                    this.sunburst.redraw();
                }
            });
        },

        update: function() {
            if(!this.parametricCollection.isEmpty()) {
                const data = generateDataRoot(this.dependentParametricCollection.toJSON());

                if(this.sunburst) {
                    this.sunburst.resize();
                    this.sunburst.redraw(data);
                } else {
                    this.sunburst = drawSunburst.call(this,
                        this.$content,
                        data,
                        _.bind(this.onClick, this)
                    );
                }
            }

            const noValidChildren = _.chain(this.dependentParametricCollection.pluck('children'))
                .flatten()
                .compact()
                .isEmpty()
                .value();

            if(this.fieldsCollection.at(1).get('field') !== '' && noValidChildren) {
                this.updateMessage(i18n['search.resultsView.sunburst.error.noSecondFieldValues']);
            }
            this.toggleContentDisplay();
        },

        render: function() {
            ParametricResultsView.prototype.render.apply(this);

            this.$content.addClass('sunburst fixed-height');
        },

        fetchDependentFields: function() {
            if (this.allowMultipleDatabases || this.queryModel.get('indexes').length === 1) {
                ParametricResultsView.prototype.fetchDependentFields.apply(this);
            } else {
                this.model.set('loading', false);
                this.updateMessage(i18n['search.resultsView.sunburst.multipleDatabases']);
                this.$parametricSelections.toggleClass('hide', true);
            }
        }
    });
});
