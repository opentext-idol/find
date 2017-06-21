/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'find/app/model/saved-searches/saved-search-model',
    'find/app/page/search/filters/geography/geography-modal',
    'text!find/templates/app/page/search/filters/geography/geography-view.html'
], function(_, $, Backbone, i18n, SavedSearchModel, GeographyModal, template) {
    'use strict';

    return Backbone.View.extend({
        events: {
            'click tr': function(event) {
                const previous = this.geographyModel.get('shapes');
                this.geographyModel.set('shapes', previous && previous.length ? null : this.shapes);
            },
            'click .geography-show-map': function(evt){
                this.showMapModal();
                return false;
            }
        },

        initialize: function(options) {
            this.geographyModel = options.geographyModel;
            this.savedSearchModel = options.savedSearchModel;

            this.shapes = options.geographyModel.get('shapes') || [];

            this.template = _.template(template);

            this.listenTo(this.geographyModel, 'change:shapes', function() {
                this.updateForGeography();
            });

            this.listenTo(this.savedSearchModel, 'sync', this.render);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.updateForGeography();
        },

        updateForGeography: function() {
            const shapesInUse = this.geographyModel.get('shapes');

            var shapeFiltering = false;
            if (shapesInUse && shapesInUse.length) {
                this.shapes = shapesInUse;
                shapeFiltering = true;
            }

            const count = this.shapes.length;

            this.$('.geography-list-count-text').text(!count ? i18n['search.geography.none']
                : i18n['search.geography.filterCount'](
                    count,
                    count === 1 ? i18n['search.geography.filter'] : i18n['search.geography.filters'],
                    shapeFiltering ? '' : i18n['search.geography.disabled']))

            this.$('.check-cell i').toggleClass('hide', !shapeFiltering);
        },

        showMapModal: function() {
            new GeographyModal({
                shapes: this.shapes,
                actionButtonCallback: _.bind(function(shapes){
                    this.shapes = shapes;
                    this.geographyModel.set('shapes', shapes);
                }, this)
            });
        }
    });
});
