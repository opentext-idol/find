/*
 * Copyright 2014-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/util/confirm-view',
    'js-whatever/js/list-view',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/customisations/asset-viewer.html',
    'text!find/templates/app/page/customisations/asset.html'
], function(Backbone, _, Confirm, ListView, configuration, i18n, template, assetTemplate) {
    'use strict';

    const PAGE_SIZE = 5;

    const defaultAssetTemplate = _.template('<div class="asset">' + assetTemplate + '</div>');

    return Backbone.View.extend({

        template: _.template(template),
        assetTemplate: _.template(assetTemplate),

        events: {
            'click .delete-asset': function(e) {
                const file = $(e.target).closest('[data-id]').attr('data-id');

                new Confirm({
                    cancelClass: 'btn-white',
                    cancelIcon: '',
                    cancelText: i18n['app.cancel'],
                    hiddenEvent: 'hidden.bs.modal',
                    message: i18n['customisations.delete.message'](file),
                    okText: i18n['app.button.delete'],
                    okClass: 'btn-danger',
                    okIcon: '',
                    title: i18n['customisations.delete.title'],
                    okHandler: _.bind(function () {
                        this.collection.get(file).destroy({
                            wait: true
                        });
                    }, this)
                });
            }
        },

        initialize: function(options) {
            this.height = options.height;
            this.width = options.width;

            this.pageCollection = new Backbone.Collection();

            this.assetList = new ListView({
                collection: this.collection,
                headerHtml: defaultAssetTemplate({
                    height: this.height,
                    imageClass: options.imageClass,
                    i18n: i18n,
                    width: this.width,
                    data: {
                        deletable: false,
                        id: 'DEFAULT',
                        url: '/static-' + configuration().commit + options.defaultImage
                    }
                }),
                itemOptions: {
                    className: 'asset',
                    template: this.assetTemplate,
                    templateOptions: {
                        height: this.height,
                        imageClass: options.imageClass,
                        i18n: i18n,
                        width: this.width,
                        url: _.result(this.collection, 'url')
                    }
                }
            })
        },

        render: function() {
            this.$el.html(this.template({
                collection: this.collection
            }));

            this.assetList.render();
            this.$('.asset-list').append(this.assetList.$el);
        }

    });

});
