/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'find/app/model/parametric-collection',
    'find/app/util/filtering-collection',
], function(_, Backbone, ParametricCollection, FilteringCollection) {
    'use strict';

    function filterPredicate(model, filterModel) {
        const searchText = filterModel && filterModel.get('text');
        return !searchText || searchMatches(model.get('displayName'), filterModel.get('text'));
    }

    function searchMatches(text, search) {
        return text.toLowerCase().indexOf(search.toLowerCase()) > -1;
    }

    return FilteringCollection.extend({
        initialize: function(models, options) {
            this.queryModel = options.queryModel;
            this.parametricCollection = options.parametricCollection;
            this.filteredParametricCollection = options.filteredParametricCollection;
            this.valueRestrictedParametricCollection = new ParametricCollection([], {url: 'api/public/parametric/values'});

            this.listenTo(this.parametricCollection, 'sync', this.onParametricSync);
            this.listenTo(this.valueRestrictedParametricCollection, 'request', this.onFilteredParametricRequest);
            this.listenTo(this.valueRestrictedParametricCollection, 'error', this.onFilteredParametricError);
            this.listenTo(this.valueRestrictedParametricCollection, 'sync', this.onFilteredParametricSync);

            FilteringCollection.prototype.initialize.apply(this, [models, _.defaults({predicate: filterPredicate}, options)]);
        },

        filterModels: function() {
            const filterText = this.filterModel.get('text');
            this.matchingFieldIds = _.pluck(this.collection.filter(this.predicate), 'id');
            const fieldIds = _.difference(_.pluck(this.collection.where({type: 'Parametric'}), 'id'), this.matchingFieldIds);
            if(fieldIds.length > 0) {
                this.valueRestrictedParametricCollection.fetchFromQueryModel(this.queryModel, {
                    fieldNames: fieldIds,
                    maxValues: 5,
                    valueRestrictions: filterText ? ["*" + filterText + "*"] : null
                });
            } else {
                this.filteredParametricCollection.set(this.parametricCollection.models);
                FilteringCollection.prototype.filterModels.apply(this);
            }
        },

        onParametricSync: function() {
            if(this.filterModel && this.filterModel.get('text')) {
                this.filterModels();
            }
        },

        onFilteredParametricRequest: function() {
            this.trigger('request');
        },

        onFilteredParametricError: function(collection, xhr) {
            if(xhr.status !== 0) {
                // The request was not aborted, so there isn't another request in flight
                this.trigger('error', collection, xhr);
            }
        },

        onFilteredParametricSync: function() {
            const matchedFieldModels = this.collection.filter(function(model) {
                return _.contains(this.matchingFieldIds, model.id) || this.valueRestrictedParametricCollection.get(model.id);
            }.bind(this));

            this.valueRestrictedParametricCollection.models.forEach(function(model) {
                if(this.parametricCollection.get(model.id)) {
                    model.set('totalValues', this.parametricCollection.get(model.id).get('totalValues'));
                }
            }.bind(this));
            const matchingParametricModels = this.valueRestrictedParametricCollection.models.concat(this.parametricCollection.filter(function(model) {
                return _.contains(this.matchingFieldIds, model.id);
            }.bind(this)));

            this.filteredParametricCollection.set(matchingParametricModels);
            this.set(matchedFieldModels);
            this.trigger('sync');
        }
    });
});
