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
    'find/app/model/dependent-parametric-collection',
], function(_, DependentParametricCollection) {
    'use strict';

    // Based chiefly on d3.scale.category20b() and d3.scale.category20c()
    const defaultPalette = [
        '#3182bd',
        '#31a354',
        '#fd8d3c',
        '#fdd0a2',
        '#636363',
        '#6baed6',
        '#74c476',
        '#756bb1',
        '#969696',
        '#9ecae1',
        '#a1d99b',
        '#9e9ac8',
        '#bcbddc',
        '#c7e9c0',
        '#8ca252',
        '#b5cf6b',
        '#ce6dbd',
        '#de9ed6',
        '#e6550d',
        '#fdae6b'
    ];

    function initializePalette(palette) {
        return _.map(palette, function(color) {
            return {color: color, text: null};
        });
    }

    // Sets default colour for each new result
    function initializeResults(results, defaultColor) {
        return _.map(results, function(result) {
            const children = result.children;
            if(children) {
                result.children = initializeResults(children, defaultColor);
            }

            return _.extend(result,
                {color: defaultColor}
            );
        });
    }

    function extractTiersFromData(tiers, data, tierDepth) {
        const depth = tierDepth === undefined || tierDepth === null
            ? 0
            : tierDepth;

        if(!_.isArray(tiers[depth])) {
            tiers[depth] = [];
        }

        _.each(data, function(d) {
            const children = d.children;
            if(children && children.length > 0) {
                extractTiersFromData(tiers, children, depth + 1)
            }
            tiers[depth].push(_.pick(d, 'count', 'text', 'hidden'));
        });
    }

    function reuseColors(palette, datum) {
        const datumIndex = _.findIndex(palette, function(paletteEntry) {
            return paletteEntry.text === datum.text;
        });
        const nullIndex = _.findIndex(palette, function(paletteEntry) {
            return paletteEntry.text === null;
        });

        let entry = datumIndex === -1 // Queue has no colour assigned for current datum
            ? (nullIndex === -1 // No more unassigned colours
                ? palette.pop() // Grab colour to reassign: least recently used
                : palette.splice(nullIndex, 1)[0]) // Take first available unassigned colour
            : palette.splice(datumIndex, 1)[0]; // Pull out cached colour for datum

        entry = _.extend(entry, datum);
        // Place (re)used colour at beginning of queue
        palette.unshift(_.pick(entry, 'text', 'color'));

        return entry;
    }

    return DependentParametricCollection.extend({
        initialize: function(models, options) {
            DependentParametricCollection.prototype.initialize.apply(this, arguments);

            const tempPalette = (options.palette || defaultPalette);

            this.hiddenColor = options.hiddenColor || '#ffffff';
            this.maxLegendEntries = Math.min((options.maxLegendEntries || 20), tempPalette.length);

            // Palettes are arrays which act as queues to ensure reuse of the *least* recently used colours.
            // Palette entries are {text: 'parametric value', color: 'hex color'} objects
            this.palettes = [
                // To minimise colour collisions, start using tier palettes from opposite ends
                initializePalette(tempPalette.slice()),
                initializePalette(tempPalette.slice().reverse())
            ];

            this.listenTo(this, 'reset update', this.addColorsToCollection);
        },

        addColorsToCollection: function(collection) {
            // TODO move this to parse, work on results array rather than mutate the collection

            const data = this.toJSON();
            let tiersHiddenFlags = [];
            let tiers = [];

            extractTiersFromData(tiers, data);

            // Get the names of the initial most populated items in each tier
            tiers = _.map(tiers, function(tier, index) {
                return _.chain(tier)
                    .groupBy('text')
                    .values()
                    .map(function(group) {
                        const reduction = _.reduce(group, function(aggregate, current) {
                            return _.extend({
                                count: aggregate.count + current.count,
                                hidden: aggregate.hidden || current.hidden
                            });
                        });

                        return _.extend(
                            {text: group[0].text},
                            reduction
                        );
                    })
                    .filter(function(d) {// Remove empty or hidden values
                        const hiddenOrEmpty = !d.text || d.hidden;
                        if(hiddenOrEmpty || tier.length > this.maxLegendEntries) {
                            tiersHiddenFlags[index] = true;
                        }
                        return !hiddenOrEmpty;
                    }.bind(this))
                    .sortBy('text')
                    .sortBy(function(d) {
                        return -d.count
                    })
                    .map(_.partial(_.pick, _, 'text', 'count'))
                    .first(this.maxLegendEntries)
                    .value();
            }.bind(this));

            // Assign colours from the queue to the largest items in each tier
            _.each(tiers, function(tier, i) {
                this['tier' + (i + 1)] = tier && tier.length > 0
                    ? {
                        // Ultimately, legend should be sorted alphabetically
                        legendData: _.sortBy(
                            _.map(tier, _.partial(reuseColors, this.palettes[i])),
                            'text'
                        ),
                        hidden: !!tiersHiddenFlags[i]
                    }
                    : null;
            }.bind(this));

            // Insert the colours back into collection
            if(this.tier1 && this.tier1.legendData) {
                collection.each(function(model) {
                    const text = model.get('text');
                    const colorDatum = _.findWhere(this.tier1.legendData, {text: text});
                    if(colorDatum) {
                        model.set(colorDatum);
                    }

                    if(this.tier2 && this.tier2.legendData) {
                        const children = model.get('children');
                        if(children && children.length > 0) {
                            const newChildren = _.map(children, function(datum) {
                                const colorDatum = _.findWhere(this.tier2.legendData, {text: datum.text});
                                return colorDatum
                                    ? _.extend(datum, colorDatum)
                                    : datum;
                            }.bind(this));

                            model.set('children', newChildren);
                        }
                    }
                }.bind(this));
            }
        },

        parse: function(results) {
            let uncoloredResults = DependentParametricCollection.prototype.parse.apply(this, arguments);

            return initializeResults(uncoloredResults, this.hiddenColor);
        }
    });
});
