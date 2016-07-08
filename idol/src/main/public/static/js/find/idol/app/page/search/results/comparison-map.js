define([
    'backbone',
    'find/idol/app/model/comparison/comparison-documents-collection',
    'find/app/page/search/results/state-token-strategy',
    'find/app/page/search/results/map-view',
    'find/app/page/search/results/field-selection-view',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'i18n!find/idol/nls/comparisons',
    'find/app/util/search-data-util',
    'text!find/templates/app/page/loading-spinner.html',
    'text!find/idol/templates/comparison/map-comparison-view.html',
    'text!find/templates/app/page/search/results/map-popover.html',
    'find/app/vent',
    'iCheck'
], function (Backbone, ComparisonDocumentsCollection, stateTokenStrategy, MapView, FieldSelectionView, configuration, i18n, comparisonsI18n, searchDataUtil, loadingSpinnerTemplate, template, popoverTemplate, vent) {

    return Backbone.View.extend({
        className: 'service-view-container',
        template: _.template(template),
        loadingTemplate: _.template(loadingSpinnerTemplate)({i18n: i18n, large: false}),
        popoverTemplate: _.template(popoverTemplate),
        
        events: {
            'click .map-popup-title': function (e) {
                var allCollections = _.chain(this.comparisons).pluck('collection').pluck('models').flatten().value();
                vent.navigateToDetailRoute(_.findWhere(allCollections, {cid: e.currentTarget.getAttribute('cid')}));
            }
        },

        initialize: function (options) {
            this.searchModels = options.searchModels;
            this.locationFields = configuration().map.locationFields;
            this.resultsStep = configuration().map.resultsStep;

            this.mapView = new MapView({addControl: true});

            var firstQueryModel = this.createQueryModel(this.model.get('firstText'), this.model.get('onlyInFirst'), [this.searchModels.first]);
            var bothQueryModel = this.createQueryModel(this.model.get('bothText'), this.model.get('inBoth'), [this.searchModels.first, this.searchModels.second]);
            var secondQueryModel = this.createQueryModel(this.model.get('secondText'), this.model.get('onlyInSecond'), [this.searchModels.second]);

            this.firstSelectionView = new FieldSelectionView({
                model: firstQueryModel,
                name: 'FirstFieldSelectionView',
                width: '100%',
                fields: _.pluck(this.locationFields, 'displayName'),
                allowEmpty: false
            });

            this.bothSelectionView = new FieldSelectionView({
                model: bothQueryModel,
                name: 'BothFieldSelectionView',
                width: '100%',
                fields: _.pluck(this.locationFields, 'displayName'),
                allowEmpty: false
            });

            this.secondSelectionView = new FieldSelectionView({
                model: secondQueryModel,
                name: 'SecondFieldSelectionView',
                width: '100%',
                fields: _.pluck(this.locationFields, 'displayName'),
                allowEmpty: false
            });


            this.comparisons = [
                {
                    name: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                    collection: new ComparisonDocumentsCollection(),
                    layer: this.mapView.createLayer({
                        iconCreateFunction: this.mapView.getDivIconCreateFunction('first-location-cluster')
                    }),
                    model: firstQueryModel,
                    color: 'green'
                },
                {
                    name: comparisonsI18n['list.title.both'],
                    collection: new ComparisonDocumentsCollection(),
                    layer: this.mapView.createLayer({
                        iconCreateFunction: this.mapView.getDivIconCreateFunction('both-location-cluster')
                    }),
                    model: bothQueryModel,
                    color: 'orange'
                },
                {
                    name: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                    collection: new ComparisonDocumentsCollection(),
                    layer: this.mapView.createLayer({
                        iconCreateFunction: this.mapView.getDivIconCreateFunction('second-location-cluster')
                    }),
                    model: secondQueryModel,
                    color: 'red'
                }
            ];

            this.createAddListeners(this.comparisons);
            this.createSyncListeners(this.comparisons);
            this.createModelListeners(this.comparisons);
        },

        render: function () {
            this.$el.html(this.template({
                bothLabel: comparisonsI18n['list.title.both'],
                firstLabel: comparisonsI18n['list.title.first'](this.searchModels.first.get('title')),
                secondLabel: comparisonsI18n['list.title.second'](this.searchModels.second.get('title')),
                showMore: i18n['search.resultsView.map.show.more']
            }));

            this.$loadingSpinner = $(this.loadingTemplate);
            this.$('.map-loading-spinner').html(this.$loadingSpinner);

            this.$('.first-select-label').append(this.firstSelectionView.$el);
            this.$('.both-select-label').append(this.bothSelectionView.$el);
            this.$('.second-select-label').append(this.secondSelectionView.$el);

            this.firstSelectionView.render();
            this.bothSelectionView.render();
            this.secondSelectionView.render();

            this.mapView.setElement(this.$('.location-comparison-map')).render();

            this.$('.location-comparison-show-more').click(_.bind(function () {
                _.each(this.comparisons, function (comparison) {
                    this.fetchDocuments(comparison.model, comparison.collection);
                }, this);
                this.toggleLoading();
            }, this));

            this.addLayers();

            this.toggleLoading();
        },

        createQueryModel: function (queryText, stateTokens, searchModels) {
            var indexes = _.chain(searchModels)
                .map(function (model) {
                    return searchDataUtil.buildIndexes(model.get('indexes'));
                })
                .flatten()
                .uniq()
                .value();

            return new Backbone.Model(_.extend({
                queryText: queryText,
                indexes: indexes
            }, stateTokens));
        },

        createAddListeners: function (comparisons) {
            _.each(comparisons, function (comparison) {
                this.listenTo(comparison.collection, 'add', function (model) {
                    var location = _.findWhere(model.get('locations'), {displayName: comparison.model.get('field')});
                    if (location) {
                        var title = model.get('title');
                        var popover = this.popoverTemplate({
                            title: title,
                            i18n: i18n,
                            latitude: location.latitude,
                            longitude: location.longitude,
                            cidForClickRouting: model.cid
                        });
                        var icon = this.mapView.getIcon('hp-record', 'white', comparison.color);
                        var marker = this.mapView.getMarker(location.latitude, location.longitude, icon, title, popover);
                        model.set('marker', marker);
                        comparison.layer.addLayer(marker);
                    }
                });
            }, this);
        },

        createSyncListeners: function (comparisons) {
            _.each(comparisons, function (comparison) {
                this.listenTo(comparison.collection, 'sync', function () {
                    var allMarkers = _.chain(this.comparisons).pluck('layer').invoke('getLayers').flatten().value();
                    if (!_.isEmpty(allMarkers) && !this.collectionsFetching()) {
                        this.mapView.loaded(allMarkers);
                    }
                    this.toggleLoading()
                });
            }, this)
        },

        createModelListeners: function (comparisons) {
            _.each(comparisons, function (comparison) {
                this.listenTo(comparison.model, 'change:field', _.bind(this.reloadMarkers, this, comparison));
            }, this)
        },

        addLayers: function () {
            _.each(this.comparisons, function (comparison) {
                this.mapView.addLayer(comparison.layer, comparison.name)
            }, this);
        },

        reloadMarkers: function (comparison) {
            this.clearMarkers(comparison.collection, comparison.layer);
            this.fetchDocuments(comparison.model, comparison.collection);
            this.toggleLoading();
        },

        clearMarkers: function (collection, layer) {
            collection.reset();
            layer.clearLayers();
        },

        collectionsFetching: function () {
            return _.chain(this.comparisons).pluck('collection').pluck('fetching').some().value();
        },

        collectionsFull: function () {
            return _.chain(this.comparisons)
                .pluck('collection')
                .reject(function (collection) {
                    return collection.length === collection.totalResults
                })
                .isEmpty()
                .value();
        },

        toggleLoading: function () {
            this.$loadingSpinner.toggleClass('hide', !this.collectionsFetching());
            this.$('.location-comparison-show-more').prop('disabled', this.collectionsFetching() || this.collectionsFull());
        },

        getFetchOptions: function (queryModel, selectedField, length) {
            var locationField = _.findWhere(this.locationFields, {displayName: selectedField});

            var latitudeFieldsInfo = configuration().fieldsInfo[locationField.latitudeField];
            var longitudeFieldsInfo = configuration().fieldsInfo[locationField.longitudeField];

            var latitudesFieldsString = latitudeFieldsInfo.names.join(':');
            var longitudeFieldsString = longitudeFieldsInfo.names.join(':');

            var exists = 'EXISTS{}:' + latitudesFieldsString + ' AND EXISTS{}:' + longitudeFieldsString;

            var newFieldText = queryModel.get('fieldText') ? queryModel.get('fieldText') + ' AND ' + exists : exists;

            return {
                data: _.extend({
                    start: length + 1,
                    max_results: length + this.resultsStep,
                    field_text: newFieldText,
                    sort: 'relevance',
                    summary: 'off'
                }, stateTokenStrategy.requestParams(queryModel)),
                remove: false,
                reset: false
            };
        },

        fetchDocuments: function (queryModel, collection) {
            if (collection.length !== collection.totalResults) {
                var selectedField = queryModel.get('field');

                var options = this.getFetchOptions(queryModel, selectedField, collection.length);

                collection.fetch(options)
            }
        }
    });

});