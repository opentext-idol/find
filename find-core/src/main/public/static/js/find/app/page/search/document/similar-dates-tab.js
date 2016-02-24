/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'find/app/page/search/document/similar-abstract-tab',
    'text!find/templates/app/page/search/document/similar-dates-tab.html',
    'slider/bootstrap-slider'

], function (_, i18n, DocumentsCollection, SimilarAbstractTab, template) {
    'use strict';

    return SimilarAbstractTab.extend({

        template: _.template(template),

        createCollection: function () {
            return new DocumentsCollection([], {
                indexes: this.indexesCollection.pluck('id')
            });
        },

        fetchData: function () {
            return {
                text: '*',
                max_results: 5,
                sort: 'relevance',
                summary: 'context',
                indexes: this.indexesCollection.pluck('id'),
                min_date: this.model.get('date').clone().subtract(this.ticks[this.$beforeDateSlider.val()].moment).toJSON(),
                max_date: this.model.get('date').clone().add(this.ticks[this.$afterDateSlider.val()].moment).toJSON(),
                highlight: false,
                auto_correct: false
            }
        },

        ticks: [{
            moment: {
                minutes: 1
            },
            label: '1m',
            text: '1 minute'
        }, {
            moment: {
                minutes: 5
            },
            label: '5m',
            text: '5 minutes'
        }, {
            moment: {
                minutes: 15
            },
            label: '15m',
            text: '15 minutes'
        }, {
            moment: {
                minutes: 30
            },
            label: '30m',
            text: '30 minutes'
        }, {
            moment: {
                hours: 1
            },
            label: '1h',
            text: '1 hour'
        }, {
            moment: {
                hours: 2
            },
            label: '2h',
            text: '2 hours'
        }, {
            moment: {
                hours: 6
            },
            label: '6h',
            text: '6 hours'
        }, {
            moment: {
                hours: 12
            },
            label: '12h',
            text: '12 hours'
        }, {
            moment: {
                days: 1
            },
            label: '1d',
            text: '1 day'
        }, {
            moment: {
                months: 1
            },
            label: '1M',
            text: '1 month'
        }, {
            moment: {
                years: 1
            },
            label: '1y',
            text: '1 year'
        }],

        postRender: function () {
            this.$afterDateSlider = this.$('.after-date-slider');
            this.$beforeDateSlider = this.$('.before-date-slider');

            var tickIndexes = _.map(this.ticks, function (data, index) {
                return index;
            });
            var tickPositionsInterval = 100 / (this.ticks.length - 1);
            var tickPositions = _.map(this.ticks, function (data, index) {
                return index * tickPositionsInterval
            });

            this.$beforeDateSlider.slider({
                ticks: tickIndexes,
                ticks_positions: tickPositions,
                ticks_labels: _.pluck(this.ticks, 'label'),
                tooltip: 'hide',
                value: 3,
                reversed: true
            });

            this.$afterDateSlider.slider({
                ticks: tickIndexes,
                ticks_positions: tickPositions,
                ticks_labels: _.pluck(this.ticks, 'label'),
                tooltip: 'hide',
                value: 3
            });
            this.$afterDateSlider.on('change', _.bind(this.getSimilarDocuments, this));
            this.$beforeDateSlider.on('change', _.bind(this.getSimilarDocuments, this));


        },

        getSimilarDocuments: function () {
            this.$('.similar-dates-summary').html(i18n['search.document.detail.tabs.similarDates.temporalSummaryHtml'](
                this.ticks[this.$beforeDateSlider.val()].text,
                this.ticks[this.$afterDateSlider.val()].text
            ));

            SimilarAbstractTab.prototype.getSimilarDocuments.call(this);
        }
    });
});
