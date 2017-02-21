/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'd3',
    'sunburst/js/sunburst',
    'find/app/page/search/results/parametric-results-view',
    'i18n!find/nls/bundle',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/sunburst/sunburst-label.html',
    'find/app/vent'
], function(_, $, d3, Sunburst, ParametricResultsView, i18n, generateErrorHtml, labelTemplate, vent) {
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

        const sunburst = new Sunburst($el, {
            animate: false,
            nameAttr: 'text',
            sizeAttr: 'count',
            comparator: null,
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
                const hoveringCenter = prevClicked ? datum === prevClicked.parent : datum.depth === 0;
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
                svg.selectAll('path')
                    .filter(function(d) {
                        // TODO Assumes depth=2 is the outer ring - will need to change if this changes
                        return d.text !== '' && d.depth === 2 && d.text === hoveredDatum.text;
                    })
                    .each(function(d) {
                        d3.select(this)
                            .transition()
                            .duration(100)
                            .attr('d', arc(outerRingAnimateSize));
                    });
            }
        });

        return sunburst;
    }

    return ParametricResultsView.extend({

        events: _.extend({
            'click .parametric-pptx': function(evt) {
                evt.preventDefault();

                var $form = $('<form class="hide" enctype="multipart/form-data" method="post" target="_blank" action="api/bi/export/ppt/sunburst"><textarea name="data"></textarea><input type="submit"></form>');

                var categories = [];
                var values = [];

                this.dependentParametricCollection.each(function(model){
                    categories.push(model.get('text') || i18n['search.resultsView.sunburst.others']);
                    values.push(model.get('count'));
                });

                $form[0].data.value = JSON.stringify({
                    categories: categories,
                    values: values,
                    title: i18n['search.resultsView.sunburst.breakdown.by'](this.fieldsCollection.at(0).get('displayValue'))
                });

                $form.appendTo(document.body).submit().remove();
            }
        }, ParametricResultsView.prototype.events),

        initialize: function(options) {
            ParametricResultsView.prototype.initialize.call(this, _.defaults({
                emptyDependentMessage: i18n['search.resultsView.sunburst.error.noDependentParametricValues'],
                emptyMessage: generateErrorHtml({errorLookup: 'emptySunburstView'}),
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

                const noValidChildren = _.chain(this.dependentParametricCollection.pluck('children'))
                    .flatten()
                    .compact()
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
            ParametricResultsView.prototype.render.apply(this);

            this.$content.addClass('sunburst');
        }
    });
});
