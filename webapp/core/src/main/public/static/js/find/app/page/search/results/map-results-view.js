define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/configuration',
    'find/app/page/search/results/field-selection-view',
    'find/app/page/search/results/map-view',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'find/app/page/search/results/add-links-to-summary',
    'text!find/templates/app/page/search/results/map-results-view.html',
    'text!find/templates/app/page/search/results/map-popover.html',
    'text!find/templates/app/page/loading-spinner.html',
    'find/app/vent',
    'html2canvas'

], function (Backbone, _, $, configuration, FieldSelectionView, MapView, i18n, DocumentsCollection, addLinksToSummary, template, popoverTemplate, loadingSpinnerTemplate, vent) {

    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        popoverTemplate: _.template(popoverTemplate),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),
        markers: [],

        events: {
            'click .map-show-more': function() {
                this.fetchDocumentCollection()
            },
            'click .map-popup-title': function (e) {
                vent.navigateToDetailRoute(this.documentsCollection.get(e.currentTarget.getAttribute('cid')));
            },
            'click .map-pptx': function(e){
                e.preventDefault();

                var $mapEl = this.$('.location-results-map'),
                    map = this.mapResultsView.map,
                    mapSize = map.getSize();

                var visible = {}, markers = [];

                function lPad(str) {
                    return str.length < 2 ? '0' + str : str
                }

                function hexColor(str){
                    var match;
                    if (match = /rgba\((\d+),\s*(\d+),\s*(\d+),\s*([0-9.]+)\)/.exec(str)) {
                        return '#' + lPad(Number(match[1]).toString(16))
                            + lPad(Number(match[2]).toString(16))
                            + lPad(Number(match[3]).toString(16))
                    }
                    else if (match = /rgb\((\d+),\s*(\d+),\s*(\d+)\)/.exec(str)) {
                        return '#' + lPad(Number(match[1]).toString(16))
                            + lPad(Number(match[2]).toString(16))
                            + lPad(Number(match[3]).toString(16))
                    }
                    return str
                }

                _.each(this.markers, function(marker){
                    var merged = this.mapResultsView.clusterMarkers.getVisibleParent(marker);

                    if (merged && !visible.hasOwnProperty(merged._leaflet_id)) {
                        visible[merged._leaflet_id] = merged;

                        var pos = this.mapResultsView.map.latLngToContainerPoint(merged.getLatLng())

                        var isCluster = merged.getChildCount

                        var xFraction = pos.x / mapSize.x;
                        var yFraction = pos.y / mapSize.y;
                        var tolerance = 0.001;

                        if (xFraction > -tolerance && xFraction < 1 + tolerance && yFraction > -tolerance && yFraction < 1 + tolerance) {
                            markers.push({
                                x: xFraction,
                                y: yFraction,
                                text: isCluster ? merged.getChildCount() :  $(merged.getPopup()._content).find('.map-popup-title').text(),
                                cluster: !!isCluster,
                                color: isCluster ? hexColor($(merged._icon).css('background-color')) : hexColor('rgba(55, 168, 218, 1)')
                            })
                        }
                    }
                }, this)

                var $objs = $mapEl.find('.leaflet-objects-pane').addClass('hide')

                html2canvas($mapEl, {
                    logging: true,
                    // This seems to avoid issues with IE11 only rendering a small portion of the map the size of the window
                    // If width and height are undefined, Firefox sometimes renders black areas.
                    // If width and height are equal to the $mapEl.width()/height(), then Chrome has the same problem as IE11.
                    width: $mapEl.width() * 2,
                    height: $mapEl.height() * 2,
                    proxy: '../api/public/map/proxy',
                    onrendered: _.bind(function(canvas) {
                        $objs.removeClass('hide')

                        var $form = $('<form class="hide" method="post" target="_blank" action="../api/bi/export/ppt/map"><input name="title"><input name="image"><input name="markers"><input type="submit"></form>');
                        $form[0].title.value = 'Showing field ' + this.fieldSelectionView.model.get('displayValue')
                        $form[0].image.value = canvas.toDataURL('image/jpeg')
                        $form[0].markers.value = JSON.stringify(markers)
                        $form.appendTo(document.body).submit().remove()
                    }, this)
                });
            }
        },

        initialize: function (options) {
            this.locationFields = configuration().map.locationFields;
            this.resultsStep = options.resultsStep;
            this.allowIncrement = options.allowIncrement;
            
            this.queryModel = options.queryModel;

            this.documentsCollection = new DocumentsCollection();
            this.model = new Backbone.Model({
                loading: false,
                text:''
            });

            this.fieldSelectionView = new FieldSelectionView({
                model: this.model,
                name: 'FieldSelectionView',
                fields: _.pluck(this.locationFields, 'displayName'),
                allowEmpty: false
            });
            
            this.mapResultsView = new MapView({addControl: false});

            this.listenTo(this.model, 'change:loading', this.toggleLoading);

            this.listenTo(this.documentsCollection, 'add', function (model) {
                var locations = model.get('locations');
                var location = _.findWhere(locations, {displayName: this.model.get('field')});
                if (location) {
                    var longitude = location.longitude;
                    var latitude = location.latitude;
                    var title = model.get('title');
                    var popover = this.popoverTemplate({
                        title: title,
                        i18n: i18n,
                        summary: addLinksToSummary(model.get('summary')),
                        cidForClickRouting: model.cid
                    });
                    var marker = this.mapResultsView.getMarker(latitude, longitude, this.getIcon(), title, popover);
                    this.markers.push(marker);
                }
            });

            this.listenTo(this.documentsCollection, 'sync', _.bind(function () {
                if (!_.isEmpty(this.markers)) {
                    this.mapResultsView.addMarkers(this.markers, true);
                    this.mapResultsView.loaded();
                }
                this.$('.map-results-count').html(this.getResultsNoHTML());
                this.model.set('loading', false);
            }, this));

            this.listenTo(this.queryModel, 'change', this.reloadMarkers);
            this.listenTo(this.model, 'change:field', this.reloadMarkers);
        },

        render: function () {
            this.$el.html(this.template({
                showMore: i18n['search.resultsView.map.show.more']
            }));
            this.mapResultsView.setElement(this.$('.location-results-map')).render();
            this.$el.prepend(this.fieldSelectionView.$el);
            this.$loadingSpinner = $(this.loadingTemplate);
            this.$loadMoreButton = this.$('.map-show-more');
            if (!this.allowIncrement) {
                this.$loadMoreButton.addClass('hide disabled');
            }
            this.fieldSelectionView.$el.after(this.$loadMoreButton);
            this.$('.map-loading-spinner').html(this.$loadingSpinner);

            this.toggleLoading();
            this.$loadMoreButton.prop('disabled', true);

            this.fieldSelectionView.render();
        },

        getResultsNoHTML: function () {
            if (this.documentsCollection.isEmpty()) {
                return i18n['search.resultsView.amount.shown.no.results'];
            } else {
                return this.allowIncrement ?
                    i18n['search.resultsView.amount.shown'](1, this.documentsCollection.length, this.documentsCollection.totalResults) :
                    i18n['search.resultsView.amount.shown.no.increment'](this.resultsStep, this.documentsCollection.totalResults);
            }
        },

        reloadMarkers: function () {
            this.clearMarkers();
            this.fetchDocumentCollection();
        },

        clearMarkers: function () {
            this.documentsCollection.reset();
            this.$('.map-results-count').empty();
            this.mapResultsView.clearMarkers(true);
            this.markers = [];
        },

        getIcon: function () {
            var locationField = _.findWhere(this.locationFields, {displayName: this.model.get('field')});
            return this.mapResultsView.getIcon(locationField.iconName, locationField.iconColor, locationField.markerColor);
        },

        toggleLoading: function () {
            if (this.$loadingSpinner) {
                this.$loadMoreButton.prop('disabled', this.documentsCollection.length === this.documentsCollection.totalResults || this.model.get('loading') || !this.model.get('field'));
                this.$loadingSpinner.toggleClass('hide', !this.model.get('loading'));
            }
        },

        getFetchOptions: function (selectedField) {
            var locationField = _.findWhere(this.locationFields, {displayName: selectedField});

            var latitudeFieldsInfo = configuration().fieldsInfo[locationField.latitudeField];
            var longitudeFieldsInfo = configuration().fieldsInfo[locationField.longitudeField];

            var latitudesFieldsString = latitudeFieldsInfo.names.join(':');
            var longitudeFieldsString = longitudeFieldsInfo.names.join(':');

            var exists = 'EXISTS{}:' + latitudesFieldsString + ' AND EXISTS{}:' + longitudeFieldsString;

            var newFieldText = this.queryModel.get('fieldText') ? this.queryModel.get('fieldText') + ' AND ' + exists : exists;

            return {
                data: {
                    text: this.queryModel.get('queryText'),
                    start: this.allowIncrement ? this.documentsCollection.length + 1 : 1,
                    max_results: this.allowIncrement ? this.documentsCollection.length + this.resultsStep : this.resultsStep,
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
            var selectedField = this.model.get('field');

            if (selectedField) {
                this.model.set('loading', true);
            } else {
                this.model.set('loading', false);
                this.toggleLoading();
                return;
            }

            var options = this.getFetchOptions(selectedField);

            if (options) {
                this.documentsCollection.fetch(options)
            }
        }
    });
});
