/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'find/app/configuration',
    'find/app/page/search/results/map-view',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'find/app/page/search/results/add-links-to-summary',
    'text!find/templates/app/page/search/results/map-results-view.html',
    'text!find/templates/app/page/search/results/map-popover.html',
    'text!find/templates/app/page/loading-spinner.html',
    'find/app/vent'
], function (_, $, Backbone, configuration, MapView, i18n, DocumentsCollection,
             addLinksToSummary, template, popoverTemplate, loadingSpinnerTemplate, vent) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        popoverTemplate: _.template(popoverTemplate),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),
        markers: {},

        events: {
            'click .map-show-more': function () {
                this.fetchDocumentCollection()
            },
            'click .map-popup-title': function (e) {
                //noinspection JSUnresolvedFunction
                vent.navigateToDetailRoute(this.documentsCollection.get(e.currentTarget.getAttribute('cid')));
            }
        },

        initialize: function (options) {
            this.resultsStep = options.resultsStep;
            this.allowIncrement = options.allowIncrement;

            this.queryModel = options.queryModel;

            this.documentsCollection = new DocumentsCollection();
            this.model = new Backbone.Model({
                loading: false,
                text: ''
            });

            this.mapResultsView = new MapView({addControl: true});

            this.listenTo(this.model, 'change:loading', this.toggleLoading);

            this.listenTo(this.documentsCollection, 'add', function (model) {
                const locations = model.get('locations');
                Object.keys(locations).forEach(function (locationName) {
                    locations[locationName].forEach(function (location) {
                        const longitude = location.longitude;
                        const latitude = location.latitude;
                        const title = model.get('title');
                        const popover = this.popoverTemplate({
                            title: title,
                            i18n: i18n,
                            summary: addLinksToSummary(model.get('summary')),
                            cidForClickRouting: model.cid
                        });
                        const icon = this.mapResultsView.getIcon(location.iconName, location.iconColor, location.markerColor);
                        const marker = this.mapResultsView.getMarker(latitude, longitude, icon, title, popover);

                        if (this.markers[location.displayName]) {
                            this.markers[location.displayName].push(marker);
                        } else {
                            this.markers[location.displayName] = [marker];
                        }
                    }, this);
                }, this);
            });

            this.listenTo(this.documentsCollection, 'sync', _.bind(function () {
                if (!_.isEmpty(this.markers)) {
                    Object.keys(this.markers).forEach(function (markerName) {
                        this.mapResultsView.addMarkers(this.markers[markerName], {
                            clusterLayer: this.clusterLayer,
                            name: markerName
                        });
                    }, this);
                    this.mapResultsView.fitMapToMarkerBounds();
                }
                this.$('.map-results-count').html(this.getResultsNoHTML());
                this.model.set('loading', false);
            }, this));

            this.listenTo(this.queryModel, 'change', this.reloadMarkers);
        },

        render: function () {
            this.$el.html(this.template({
                showMore: i18n['search.resultsView.map.show.more']
            }));
            this.mapResultsView.setElement(this.$('.location-results-map')).render();
            this.$loadingSpinner = $(this.loadingTemplate);
            this.$loadMoreButton = this.$('.map-show-more');
            if (!this.allowIncrement) {
                this.$loadMoreButton.addClass('hide disabled');
            }
            this.$('.map-loading-spinner').html(this.$loadingSpinner);

            this.toggleLoading();
            this.$loadMoreButton.prop('disabled', true);

            this.reloadMarkers();
        },

        getResultsNoHTML: function () {
            return this.documentsCollection.isEmpty()
                ? i18n['search.resultsView.amount.shown.no.results']
                : this.allowIncrement
                    ? i18n['search.resultsView.amount.shown'](1, this.documentsCollection.length, this.documentsCollection.totalResults)
                    : i18n['search.resultsView.amount.shown.no.increment'](this.resultsStep, this.documentsCollection.totalResults);
        },

        reloadMarkers: function () {
            if (this.mapResultsView.mapRendered()) {
                this.clearMarkers();
                this.clusterLayer = this.mapResultsView.addClusterLayer();
                this.fetchDocumentCollection();
            }
        },

        clearMarkers: function () {
            this.documentsCollection.reset();
            this.$('.map-results-count').empty();
            this.mapResultsView.clearMarkers();
            this.clusterLayer = null;
            this.markers = {};
        },

        toggleLoading: function () {
            if (this.$loadingSpinner) {
                const loading = this.model.get('loading');
                this.$loadMoreButton.prop('disabled', this.documentsCollection.length === this.documentsCollection.totalResults || loading);
                this.$loadingSpinner.toggleClass('hide', !loading);
            }
        },

        getFetchOptions: function (fieldText) {
            const newFieldText = this.queryModel.get('fieldText')
                ? '(' + this.queryModel.get('fieldText') + ') AND (' + fieldText + ')'
                : fieldText;

            return {
                data: {
                    text: this.queryModel.get('queryText'),
                    start: this.allowIncrement
                        ? this.documentsCollection.length + 1
                        : 1,
                    max_results: this.allowIncrement
                        ? this.documentsCollection.length + this.resultsStep
                        : this.resultsStep,
                    indexes: this.queryModel.get('indexes'),
                    field_text: newFieldText,
                    min_date: this.queryModel.get('minDate'),
                    max_date: this.queryModel.get('maxDate'),
                    sort: 'relevance',
                    summary: 'context',
                    queryType: 'MODIFIED'
                },
                remove: false,
                reset: false
            };
        },

        fetchDocumentCollection: function () {
            const config = configuration();

            const locationFields = config.map.locationFields;
            if (!_.isEmpty(locationFields)) {
                this.model.set('loading', true);

                const fieldText = locationFields.map(function (locationField) {
                    //noinspection JSUnresolvedVariable
                    return '(EXISTS{}:' + config.fieldsInfo[locationField.latitudeField].names.join(':') +
                        ' AND EXISTS{}:' + config.fieldsInfo[locationField.longitudeField].names.join(':') + ')';
                }).join(' OR ');

                const options = this.getFetchOptions(fieldText);
                this.documentsCollection.fetch(options);
            }
        }
    });
});
