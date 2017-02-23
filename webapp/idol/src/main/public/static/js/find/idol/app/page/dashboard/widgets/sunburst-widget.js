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
    'i18n!find/nls/bundle'
], function(_, $, d3, Sunburst, SavedSearchWidget, DependentParametricCollection, i18n) {
    'use strict';

    return SavedSearchWidget.extend({
        viewType: 'sunburst',

        initialize: function(options) {
            SavedSearchWidget.prototype.initialize.apply(this, arguments);

            // TODO display error msg if field absent (no dashboards config validation)
            this.firstField = options.widgetSettings.firstField;
            this.secondField = options.widgetSettings.secondField;
            this.dependentParametricCollection = new DependentParametricCollection({
                minShownResults: 10
            });
        },

        render: function() {
            SavedSearchWidget.prototype.render.apply(this);

            var data = this.dependentParametricCollection.toJSON();

            if(data.length > 0) {
                this.$legendContainer = $('<div class="sunburst-legend"></div>');
                this.$visualizerContainer = $('<div class="sunburst-visualizer-container"></div>');

                this.$content.append(this.$visualizerContainer.add(this.$legendContainer));
                this.sunburst = this.drawSunburst(data);
            } else {
                this.$content.text(i18n['dashboards.widget.sunburst.noResults']);
            }
        },

        postInitialize: function() {
            return this.updateParametricDistribution();
        },

        onResize: function() {
        },

        getData: function() {
            return this.updateParametricDistribution();
        },

        drawSunburst: function(data, colors) {
            if(this.$content && this.$visualizerContainer) {
                var colorScheme = _.defaults(colors || {}, {
                    centre: 'white',
                    hidden: 'white',
                    palette: d3.scale.category20c()
                });

                this.$visualizerContainer.empty();

                var sunburst = new Sunburst(this.$visualizerContainer, {
                    animate: false,
                    sizeAttr: 'count',
                    nameAttr: 'text',
                    comparator: function(datumA, datumB) {
                        return d3.ascending(datumA.text, datumB.text);
                    },
                    outerRingAnimateSize: 15,
                    data: {
                        children: data
                    },
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

                return sunburst;
            }
        },

        updateParametricDistribution: function() {
            return this.dependentParametricCollection
                .fetchDependentFields(this.queryModel, this.firstField, this.secondField);
        }
    });
});
