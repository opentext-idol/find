/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'find/app/vent',
    'text!find/templates/app/page/search/document/similar-dates-tab.html',
    'slider/bootstrap-slider'
], function (Backbone, _, i18n, DocumentsCollection, vent, template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        similarDocumentTemplate: _.template('<li data-cid="<%-cid%>" class="clickable"><h4><%-model.get("title")%></h4><p><%-model.get("summary").trim().substring(0, 200) + "..."%></p></li>'),

        events: {
            'click [data-cid]': function(e) {
                var cid = $(e.currentTarget).data('cid');
                var model = this.collection.get(cid);

                vent.navigateToDetailRoute(model);
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


        initialize: function (options) {
            this.indexesCollection = options.indexesCollection;
        },

        render: function () {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$afterDateSlider = this.$('.after-date-slider');
            this.$beforeDateSlider = this.$('.before-date-slider');

            var tickIndexes = _.map(this.ticks, function(data, index) {return index;});
            var tickPositionsInterval = 100 / (this.ticks.length -1);
            var tickPositions = _.map(this.ticks, function(data, index) {return index * tickPositionsInterval});

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

            this.$afterDateSlider.on('change', _.bind(this.getSimilarDateDocuments, this));
            this.$beforeDateSlider.on('change', _.bind(this.getSimilarDateDocuments, this));

            if (this.indexesCollection.length) {
                this.getSimilarDateDocuments();
            } else {
                this.listenTo(this.indexesCollection, 'sync', this.getSimilarDateDocuments);
            }
        },

        getSimilarDateDocuments: function () {
            this.$('.loading-spinner').removeClass('hide');

            this.$('.similar-dates-summary').html(i18n['search.document.detail.tabs.similarDates.temporalSummaryHtml'](
                this.ticks[this.$beforeDateSlider.val()].text,
                this.ticks[this.$afterDateSlider.val()].text
            ));

            this.$('ul').empty();

            this.collection = new DocumentsCollection([], {
                indexes: this.indexesCollection.pluck('id')
            });

            this.collection.fetch({
                data: {
                    text: '*',
                    max_results: 5,
                    sort: 'relevance',
                    summary: 'context',
                    indexes: this.indexesCollection.pluck('id'),
                    min_date: this.model.get('date').clone().subtract(this.ticks[this.$beforeDateSlider.val()].moment).toJSON(),
                    max_date: this.model.get('date').clone().add(this.ticks[this.$afterDateSlider.val()].moment).toJSON(),
                    highlight: false,
                    auto_correct: false
                },
                error: _.bind(function () {
                    this.$('ul').empty();
                    this.$('ul').html(i18n['search.similarDocuments.error']);
                }, this),
                success: _.bind(function () {
                    //TODO add the DontMatchReference to haven search components and use that instead of filtering on collection.
                    var filteredModels = this.collection.filter(function (model) {
                            return model.get('reference') !== this.model.get('reference');
                        }, this);

                    if (filteredModels.length === 0) {
                        this.$('ul').html(i18n['search.similarDocuments.none']);
                    } else {
                        var html = _.map(filteredModels, function (model) {
                                return this.similarDocumentTemplate({
                                    model: model,
                                    cid: model.cid
                                });
                            }, this)
                            .join('');
                        this.$('ul').html(html);
                    }
                }, this)
            }).always(_.bind(function () {
                this.$('.loading-spinner').addClass('hide');
            }, this));


        }
    });
});
