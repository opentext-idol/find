/*
 * Copyright 2016-2017 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'backbone',
    'i18n!find/nls/bundle',
    'text!find/templates/app/util/selector.html'
], function(_, $, Backbone, i18n, selectorTemplate) {
    'use strict';

    return Backbone.View.extend({
        selectorTemplate: _.template(selectorTemplate, {variable: 'data'}),

        events: {
            'shown.bs.tab [data-tab-id]': function(event) {
                var selectedTab = $(event.target).attr('data-tab-id');
                this.model.set('selectedTab', selectedTab);
            }
        },

        initialize: function(options) {
            this.views = options.views;
            this.model = options.model;
        },

        render: function() {
            this.$el.html('<ul class="nav nav-tabs minimal-tab selector-list" role="tablist"></ul>');

            const $selectorList = this.$('.selector-list');
            const selectedTab = this.model.get('selectedTab');

            _.each(this.views, function(viewData) {
                $(this.selectorTemplate({
                    i18n: i18n,
                    id: viewData.id,
                    uniqueId: viewData.uniqueId,
                    selector: viewData.selector
                })).toggleClass('active', viewData.id === selectedTab)
                    .appendTo($selectorList);
            }, this);
        },

        switchTab: function(tab) {
            const $tab = this.$('[data-tab-id = "' + tab + '"]');
            if($tab) {
                $tab.tab('show');
            }
        }
    });
});
