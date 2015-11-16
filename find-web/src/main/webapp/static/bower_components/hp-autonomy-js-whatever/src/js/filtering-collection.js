/**
 * @module js-whatever/js/filtering-collection
 */
define([
    '../../../backbone/backbone'
], function(Backbone) {

    var getTrackedAttributes = function(trackedModel) {
        if (this.attributes) {
            return trackedModel.pick.apply(trackedModel, this.attributes);
        } else {
            return trackedModel.attributes;
        }
    };

    var createTrackingModel = function(trackedModel) {
        return new this.model(getTrackedAttributes.call(this, trackedModel), {trackedModel: trackedModel});
    };

    var getTrackingModel = function(trackedModel) {
        return this.find(function(currentModel) {
            return currentModel.trackedModel === trackedModel;
        });
    };

    var getFilteredModels = function() {
        var filteredModels = this.collection.filter(function(model) {
            return this.modelFilter(model, this.filters);
        }, this);

        return _.map(filteredModels, function(model) {
            return createTrackingModel.call(this, model);
        }, this);
    };

    var removeModel = function(model) {
        this.remove(model);
        model.stopListening();
    };

    /**
     * @callback module:js-whatever/js/filtering-collection.FilteringCollection~modelFilter
     * @param {Backbone.Model} model The model to check against the filter
     * @param {Object} filters Filters data
     * @returns {Boolean} True if the model passes the filter, false if not
     */
    /**
     * @typedef module:js-whatever/js/filtering-collection.FilteringCollection~FilteringCollectionOptions
     * @property {Backbone.Collection} collection The collection to track
     * @property {module:js-whatever/js/filtering-collection.FilteringCollection~modelFilter} modelFilter
     * @property {Object} [filters={}] Initial filters data which will be passed to the modelFilter
     */
    /**
     * @name module:js-whatever/js/filtering-collection.FilteringCollection
     * @desc Collection which tracks another collection. Models added to the tracked collection are only added to the
     * FilteringCollection if they pass the modelFilter function. If the tracked model changes, the modelFilter is applied
     * again and if it passes the new attributes are set on the tracking model, or if it fails the model is removed.
     * Models removed from the tracked collection are removed from the FilteringCollection.
     * @constructor
     * @param {Backbone.Model[]} models Initial models
     * @param {module:js-whatever/js/filtering-collection.FilteringCollection~FilteringCollectionOptions} Options for the filtering collection
     * @extends Backbone.Collection
     */
    return Backbone.Collection.extend(/** @lends module:js-whatever/js/filtering-collection.FilteringCollection.prototype */{
        initialize: function(models, options) {
            this.attributes = options.attributes;
            this.collection = options.collection;
            this.filters = options.filters || {};
            this.modelFilter = options.modelFilter;

            this.listenTo(this.collection, 'add', function(trackedModel) {
                if (this.modelFilter(trackedModel, this.filters)) {
                    this.add(createTrackingModel.call(this, trackedModel));
                }
            });

            this.listenTo(this.collection, 'change', function(trackedModel) {
                var existingModel = getTrackingModel.call(this, trackedModel);

                if (existingModel) {
                    if (this.modelFilter(trackedModel, this.filters)) {
                        existingModel.set(getTrackedAttributes.call(this, trackedModel));
                    } else {
                        removeModel.call(this, existingModel);
                    }
                } else if (this.modelFilter(trackedModel, this.filters)) {
                    this.add(createTrackingModel.call(this, trackedModel));
                }
            });

            this.listenTo(this.collection, 'remove', function(trackedModel) {
                var trackingModel = getTrackingModel.call(this, trackedModel);

                if (trackingModel) {
                    removeModel.call(this, trackingModel);
                }
            });

            this.listenTo(this.collection, 'reset', function() {
                this.reset(getFilteredModels.call(this));
            });

            _.each(getFilteredModels.call(this), function(model) {
                models.push(model);
            });
        },

        /**
         * @desc Updates the filters data to the method parameter and re-filters the collection
         * @param {Object} filters New filters data
         */
        filter: function(filters) {
            this.filters = filters;
            this.reset(getFilteredModels.call(this));
        },

        model: Backbone.Model.extend({
            initialize: function(attributes, options) {
                this.trackedModel = options.trackedModel;
            }
        })
    });

});