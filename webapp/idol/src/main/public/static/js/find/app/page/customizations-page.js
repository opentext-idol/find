/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'jquery',
    'js-whatever/js/base-page',
    'find/app/page/customizations/asset-widget',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/customizations-page.html'
], function(_, $, BasePage, AssetWidget, i18n, template) {
    'use strict';

    // matches the AssetType enum on the server
    const ASSET_TYPES = {
        bigLogo: {
            defaultImage: '/img/Find_Logo_lge.png',
            height: 45,
            type: 'BIG_LOGO',
            width: 240
        },
        smallLogo: {
            defaultImage: '/img/Find_Logo_sml.png',
            height: 20,
            type: 'SMALL_LOGO',
            width: 100
        }
    };

    return BasePage.extend({
        template: _.template(template),

        className: 'container-fluid',

        initialize: function(options) {
            this.assets = [
                [
                    _.extend({
                        collection: options.bigLogoCollection,
                        description: i18n['customizations.bigLogo.description'],
                        imageClass: 'big-logo-background',
                        title: i18n['customizations.bigLogo'],
                    }, ASSET_TYPES.bigLogo),
                    _.extend({
                        collection: options.smallLogoCollection,
                        description: i18n['customizations.smallLogo.description'],
                        imageClass: 'small-logo-background',
                        title: i18n['customizations.smallLogo'],
                    }, ASSET_TYPES.smallLogo)
                ]
            ];

            this.assetWidgets = _.map(this.assets, function(group) {
                return _.map(group, function(asset) {
                    return new AssetWidget(asset);
                })
            })
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.$widgets = this.$('.widgets');

            _.each(this.assetWidgets, function(widgetGroup) {
                var $groupEl = $('<div class="row"></div>');

                _.each(widgetGroup, function(widget) {
                    widget.render();

                    $groupEl.append(widget.$el);
                });

                this.$widgets.append($groupEl);
            }, this)
        }

    }, {
        AssetTypes: ASSET_TYPES
    });
});
