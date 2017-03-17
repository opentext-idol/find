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
    'find/idol/app/page/dashboard/legend-color-collection',
    'text!find/idol/templates/page/dashboards/widgets/sunburst-widget-legend.html',
    'text!find/idol/templates/page/dashboards/widgets/sunburst-widget-legend-item.html',
    'text!find/idol/templates/page/dashboards/widgets/sunburst-legend-too-many-items-entry.html',
    'i18n!find/nls/bundle'
], function(_, $, d3, Sunburst, SavedSearchWidget, LegendColorCollection, legendTemplate,
            legendItemTemplate, tooManyItemsTemplate, i18n) {
    'use strict';

    const tooManyItemsHtml = _.template(tooManyItemsTemplate)({i18n: i18n});
    const legendItemTemplateFn = _.template(legendItemTemplate);
    const noResultsMessage = '<span class="sunburst-widget-no-results-text hide">' +
        _.escape(i18n['dashboards.widget.sunburst.noResults']) +
        '</span>';
    const HIDDEN_COLOR = '#ffffff';

    function composeLegendHtml(datum) {
        return legendItemTemplateFn({
            text: datum.text,
            color: datum.color
        });
    }

    /**
     * Prettify the given field name for display. Replaces underscores with spaces and capitalises the first letter of
     * each word.
     * @alias module:prettify-field-name
     * @function
     * @param {String} name The input field name
     * @returns {String} The display name
     */
    function prettifyFieldName(name) {
        // Compact to deal with field names which begin with underscore or contain consecutive underscores
        return _.chain(name.substring(name.lastIndexOf('/') + 1).split('_')).compact().map(function(word) {
            return word[0].toUpperCase() + word.slice(1).toLowerCase();
        }).value().join(' ');
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
        return legendHtmlArray.join('') +
            (legendHtmlArray.length > 0 && tooMany
                ? tooManyItemsHtml
                : '');
    }

    return SavedSearchWidget.extend({
        viewType: 'sunburst',
        legendTemplate: _.template(legendTemplate),

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            // TODO display error msg if field absent (no dashboards config validation)
            this.firstField = options.widgetSettings.firstField;
            this.secondField = options.widgetSettings.secondField;
            this.maxLegendEntries = options.widgetSettings.maxLegendEntries || 5;

            this.legendColorCollection = new LegendColorCollection(null, {
                hiddenColor: HIDDEN_COLOR,
                maxLegendEntries: this.maxLegendEntries
            });

            this.listenTo(this.legendColorCollection, 'update reset', this.updateSunburstAndLegend);
        },

        exportPPTData: function(){
            const data = this.legendColorCollection.map(function(model){
                return {
                    category: model.get('text') || i18n['search.resultsView.sunburst.others'],
                    value: model.get('count'),
                    color: model.get('color') || HIDDEN_COLOR
                }
            }).sort(function(a, b){
                return d3.ascending(a.category, b.category)
            });

            return data.length ? {
                    type: 'sunburst',
                    data: {
                        categories: _.pluck(data, 'category'),
                        values: _.pluck(data, 'value'),
                        title: prettyOrNull(this.firstField),
                        colors: _.pluck(data, 'color'),
                        strokeColors: ['#000000']
                    }
                } : null
        },

        render: function() {
            SavedSearchWidget.prototype.render.apply(this);

            this.$legendContainer = $('<div class="sunburst-legend"></div>');
            this.$visualizerContainer = $('<div class="sunburst-visualizer-container"></div>');
            this.$emptyMessage = $(noResultsMessage);

            this.$content.append(this.$emptyMessage.add(this.$visualizerContainer.add(this.$legendContainer)));
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
                this.sunburst.resize();
                this.sunburst.redraw();
            }
        },

        getData: function() {
            return this.updateParametricDistribution();
        },

        drawSunburst: function(data) {
            if(this.$content && this.$visualizerContainer) {
                return new Sunburst(this.$visualizerContainer, {
                    animate: true,
                    sizeAttr: 'count',
                    nameAttr: 'text',
                    comparator: function(datumA, datumB) {
                        return d3.ascending(datumA.text, datumB.text);
                    },
                    data: data,
                    fillColorFn: function(datum) {
                        if(!datum.parent) {
                            return 'none';
                        }

                        // All data should have colors by this point
                        return datum.color
                            ? datum.color
                            : HIDDEN_COLOR;
                    },
                    labelFormatter: null// no labels on hover
                });
            }
        },

        updateParametricDistribution: function() {
            return this.legendColorCollection
                .fetchDependentFields(this.queryModel, this.firstField, this.secondField);
        },

        updateSunburstAndLegend: function(collection) {
            if(this.$visualizerContainer && this.$legendContainer && this.$emptyMessage) {
                const empty = collection.isEmpty();
                this.$visualizerContainer.toggleClass('hide', empty);
                this.$legendContainer.toggleClass('hide', empty);
                this.$emptyMessage.toggleClass('hide', !empty);
                if(empty) {
                    // Next time we have data, the initialisation animation will run again
                    this.sunburst = null;
                    this.$visualizerContainer.empty();
                } else {
                    const rootData = {children: collection.toJSON()};

                    this.updateLayout();
                    if(this.sunburst) {
                        this.sunburst.resize();
                        this.sunburst.redraw(rootData);
                    } else {
                        this.sunburst = this.drawSunburst(rootData);
                    }
                }
            }
            this.populateLegend();
        },

        populateLegend: function() {
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

                const tier1Hash = this.legendColorCollection.tier1;
                const tier2Hash = this.legendColorCollection.tier2;
                const tier1 = {
                    legendData: tier1Hash ? tier1Hash.legendData : null,
                    hidden: tier1Hash ? tier1Hash.hidden : null,
                    $el: this.$innerLegend
                };
                const tier2 = {
                    legendData: tier2Hash ? tier2Hash.legendData : null,
                    hidden: tier2Hash ? tier2Hash.hidden : null,
                    $el: this.$outerLegend
                };

                _.each([tier1, tier2], function(tier) {
                    if(!tier || (tier && tier.legendData === null)) {
                        tier.$el.text(i18n['dashboards.widget.sunburst.legend.noValues']);
                    } else {
                        const htmlArray = [];

                        _.each(tier.legendData, function(legendDatum) {
                            htmlArray.push(composeLegendHtml(legendDatum));
                        });

                        if(htmlArray.length > 0) {
                            tier.$el.html(buildLegendHtml(htmlArray, tier.hidden));
                        } else {
                            tier.$el.text(i18n['dashboards.widget.sunburst.legend.noValues']);
                        }
                    }
                });
            }
        }
    });
});
