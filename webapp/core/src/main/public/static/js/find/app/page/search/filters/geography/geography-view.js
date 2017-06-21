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
                // TODO: toggle on/off of the filters and apply them
                const $targetRow = $(event.currentTarget);
                const selected = $targetRow.attr('data-filter-id');
                const previous = this.geographyModel.get('geography');

                this.geographyModel.set('geography', selected === previous
                    ? null
                    : selected);
            },
            'click .geography-show-map': function(evt){
                this.showMapModal();
                return false;
            }
        },

        initialize: function(options) {
            this.geographyModel = options.geographyModel;
            this.savedSearchModel = options.savedSearchModel;

            this.template = _.template(template);

            this.listenTo(this.geographyModel, 'change:geography', function() {
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
            const geographyFilters = this.geographyModel.get('geography');

            const count = geographyFilters && geographyFilters.length;
            this.$('.geography-list-count-text').text(!count ? i18n['search.geography.none'] :
                i18n['search.geography.filterCount'](count, count === 1 ? i18n['search.geography.filter'] : i18n['search.geography.filters']))

            this.$('.check-cell i').toggleClass('hide', !!count);
        },

        showMapModal: function() {
            new GeographyModal({
                geography: this.geographyModel.get('geography') || []
            });
        }
    });
});
