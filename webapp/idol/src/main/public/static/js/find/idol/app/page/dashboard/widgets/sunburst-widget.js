/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'd3',
    'sunburst/js/sunburst',
    './saved-search-widget',
    'find/app/model/dependent-parametric-collection',
    'parametric-refinement/prettify-field-name',
    'text!find/idol/templates/page/dashboards/widgets/sunburst-widget-legend.html',
    'text!find/idol/templates/page/dashboards/widgets/sunburst-widget-legend-item.html',
    'text!find/idol/templates/page/dashboards/widgets/sunburst-legend-too-many-items-entry.html',
    'i18n!find/nls/bundle'
], function(_, $, d3, Sunburst, SavedSearchWidget, DependentParametricCollection,
            prettifyFieldName, legendTemplate, legendItemTemplate, tooManyItemsTemplate, i18n) {
    'use strict';

    const tooManyItemsHtml = _.template(tooManyItemsTemplate)({i18n: i18n});
    const legendItemTemplateFn = _.template(legendItemTemplate);
    const noResultsMessage = '<span class="sunburst-widget-no-results-text">' +
        i18n['dashboards.widget.sunburst.noResults'] +
        '</span>';

    function composeLegendHtml(datum) {
        return legendItemTemplateFn({
            text: datum.text,
            color: datum.color
        });
    }

    function prettyOrNull(field) {
        return field
            ? prettifyFieldName(field)
            : null;
    }

    /**
     * @desc If there are too many values to display in the Sunburst or in the legend, the
     * legend's first entry should say there are too many entries. This function prepends the legend's
     * HTML with the requisite item.
     *
     * @param {String[]} legendHtmlArray HTML strings representing the legend entries
     * @param tooMany {Boolean} flag setting whether a 'too many entries' item should be prepended
     * @returns {String}
     */
    function buildLegendHtml(legendHtmlArray, tooMany) {
        return (legendHtmlArray.length > 0 && tooMany
                ? tooManyItemsHtml
                : '') + legendHtmlArray.join('');
    }

    return SavedSearchWidget.extend({
        viewType: 'sunburst',
        legendTemplate: _.template(legendTemplate),

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            // TODO display error msg if field absent (no dashboards config validation)
            this.firstField = options.widgetSettings.firstField;
            this.secondField = options.widgetSettings.secondField;
            this.dependentParametricCollection = new DependentParametricCollection({
                minShownResults: 10
            });

            this.listenTo(this.dependentParametricCollection, 'update reset', function(collection) {
                const data = collection.toJSON();

                if(this.$visualizerContainer && this.$legendContainer) {
                    const empty = collection.isEmpty();
                    this.$visualizerContainer.toggleClass('hide', empty);
                    this.$legendContainer.toggleClass('hide', empty);
                    const $noResultsMessage = this.$('.sunburst-widget-no-results-text');
                    if(empty) {
                        if(!$noResultsMessage.length) {
                            this.$content.prepend(noResultsMessage);
                        }
                    } else {
                        const rootData = {children: data};

                        $noResultsMessage.remove();
                        this.updateLayout();
                        if(this.sunburst) {
                            this.sunburst.resize();
                            this.sunburst.redraw(rootData);
                        } else {
                            this.sunburst = this.drawSunburst(rootData);
                        }
                    }
                }
                this.populateLegend(data);
            });
        },

        render: function() {
            SavedSearchWidget.prototype.render.apply(this);

            this.$legendContainer = $('<div class="sunburst-legend"></div>');
            this.$visualizerContainer = $('<div class="sunburst-visualizer-container"></div>');

            this.$content.append(this.$visualizerContainer.add(this.$legendContainer));
            this.updateLayout();
        },

        postInitialize: function() {
            return this.updateParametricDistribution();
        },

        // Decide if legend is placed underneath the visualizer, or to the side.
        updateLayout: function() {
            if(this.$legendContainer && this.$content) {
                // Prefer side-by-side layout: widget must be slightly narrower than a square
                // for legend to be placed underneath Sunburst
                const narrowWidget = this.contentWidth() * 0.9 < this.contentHeight();

                this.$legendContainer.toggleClass('legend-one-item-per-line', !narrowWidget);
                this.$content.toggleClass('narrow-widget', narrowWidget);
            }
        },

        onResize: function() {
            this.updateLayout();

            if(this.sunburst) {
                //TODO recalculate font sizes here?
                this.sunburst.resize();
                this.sunburst.redraw();
            }
        },

        getData: function() {
            return this.updateParametricDistribution();
        },

        drawSunburst: function(data, colors) {
            if(this.$content && this.$visualizerContainer) {
                const colorScheme = _.defaults(colors || {}, {
                    centre: 'white',
                    hidden: 'white',
                    palette: d3.scale.category20c()
                });

                return new Sunburst(this.$visualizerContainer, {
                    animate: false,
                    sizeAttr: 'count',
                    nameAttr: 'text',
                    comparator: function(datumA, datumB) {
                        return d3.ascending(datumA.text, datumB.text);
                    },
                    outerRingAnimateSize: 15,
                    data: data,
                    fillColorFn: function(datum) {
                        if(datum.parent) {
                            if(datum.hidden || datum.parent.hidden) {
                                return colorScheme.hidden;
                            } else if(datum.parent.parent) {
                                // Outer tier sector
                                datum.color = colorScheme.palette(datum.text);
                            } else {
                                // Inner tier sector
                                datum.color = (datum.count && datum.text
                                    ? colorScheme.palette(datum.text)
                                    : colorScheme.hidden);
                            }
                        } else {
                            // assigns a fixed colour to the Sunburst's centre
                            return colorScheme.centre;
                        }

                        return datum.color;
                    },
                    labelFormatter: null// no labels on hover
                });
            }
        },

        updateParametricDistribution: function() {
            return this.dependentParametricCollection
                .fetchDependentFields(this.queryModel, this.firstField, this.secondField);
        },

        populateLegend: function(data) {
            if(this.$content && this.$legendContainer) {
                this.$legendContainer
                    .html(this.legendTemplate({
                        innerRingHeader: prettyOrNull(this.firstField),
                        outerRingHeader: prettyOrNull(this.secondField)
                    }));

                this.$innerLegend = this.$legendContainer
                    .find('.inner-ring-legend .sunburst-legend-field-values');
                this.$outerLegend = this.$legendContainer
                    .find('.outer-ring-legend .sunburst-legend-field-values');

                if(data.length === 0) {
                    this.$innerLegend.text(i18n['dashboards.widget.sunburst.legend.noValues']);
                    this.$outerLegend.text(i18n['dashboards.widget.sunburst.legend.noValues']);
                } else {
                    // Inner tier legend
                    const outerValues = [];
                    const innerHtmlArray = [];
                    let innerTooMany = false;

                    _.each(data, function(datum) {
                        if(datum.children && datum.children.length > 0) {
                            outerValues.push.apply(outerValues, datum.children);
                        }

                        if(datum.hidden || datum.text === '') {
                            innerTooMany = true;
                        } else {
                            innerHtmlArray.push(composeLegendHtml(datum));
                        }
                    }.bind(this));

                    this.$innerLegend.html(buildLegendHtml(innerHtmlArray, innerTooMany));

                    // Outer tier legend
                    if(outerValues.length > 0) {
                        let outerTooMany = false;
                        const outerHtmlArray = [];
                        _.each(outerValues, function(datum) {
                            if(datum.hidden || datum.text === '') {
                                outerTooMany = true;
                            } else {
                                outerHtmlArray.push(composeLegendHtml(datum));
                            }
                        }.bind(this));

                        this.$outerLegend.html(buildLegendHtml(outerHtmlArray, outerTooMany));
                    } else {
                        this.$outerLegend.text(i18n['dashboards.widget.sunburst.legend.noValues']);
                    }
                }
            }
        }
    });
});
