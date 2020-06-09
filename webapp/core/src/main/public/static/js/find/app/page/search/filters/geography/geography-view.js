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
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/model/geography-model',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/page/search/filters/geography/geography-modal',
    'text!find/templates/app/page/search/filters/geography/geography-view.html'
], function(_, $, Backbone, i18n, GeographyModel, SavedSearchModel, GeographyModal, template) {
    'use strict';

    const LocationFields = GeographyModel.LocationFields;
    const LocationFieldsById = GeographyModel.LocationFieldsById;

    return Backbone.View.extend({
        events: {
            'click tr': function(evt) {
                const locationId = $(evt.currentTarget).closest('tr').data('locationId');
                const previous = this.geographyModel.get(locationId);
                this.geographyModel.set(locationId, previous && previous.length ? null : this.shapes[locationId]);
            },
            'click .geography-show-map': function(evt){
                const locationId = $(evt.currentTarget).closest('tr').data('locationId');
                this.showMapModal(locationId);
                return false;
            }
        },

        initialize: function(options) {
            this.geographyModel = options.geographyModel;
            this.savedSearchModel = options.savedSearchModel;

            this.shapes = _.mapObject(options.geographyModel.toJSON(), function(val){ return val || [] });

            this.template = _.template(template);

            this.listenTo(this.geographyModel, 'change', function() {
                this.updateForGeography();
            });

            this.listenTo(this.savedSearchModel, 'sync', this.render);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                LocationFields: LocationFields
            }));

            this.updateForGeography();
        },

        updateForGeography: function() {
            const $el = this.$('tr[data-location-id]')

            _.each(LocationFields, function(field, idx){
                const $child = $el.get(idx);
                const id = field.id;

                const shapesInUse = this.geographyModel.get(id);

                let shapeFiltering = false;
                if (shapesInUse && shapesInUse.length) {
                    this.shapes[id] = shapesInUse;
                    shapeFiltering = true;
                }

                const count = this.shapes[id].length;

                $('.geography-list-count-text', $child).text(!count ? i18n['search.geography.none']
                    : i18n['search.geography.filterCount'](
                        count,
                        count === 1 ? i18n['search.geography.filter'] : i18n['search.geography.filters'],
                        shapeFiltering ? '' : i18n['search.geography.disabled']))

                $('.check-cell i', $child).toggleClass('hide', !shapeFiltering);
            }, this)
        },

        showMapModal: function(locationId) {
            new GeographyModal({
                shapes: this.shapes[locationId] || [],
                geospatialUnified: LocationFieldsById[locationId].geospatialUnified,
                actionButtonCallback: _.bind(function(shapes){
                    this.shapes[locationId] = shapes;
                    this.geographyModel.set(locationId, shapes);
                }, this)
            });
        }
    });
});
