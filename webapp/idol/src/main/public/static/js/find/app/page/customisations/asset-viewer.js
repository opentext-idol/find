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

    function getFile(e) {
        return $(e.target).closest('.asset').attr('data-id') || null;
    }

    return Backbone.View.extend({

        template: _.template(template),
        assetTemplate: _.template(assetTemplate),

        events: {
            'click .previous:not(.disabled)': function() {
                this.page--;
                this.changePage();
            },
            'click .next:not(.disabled)': function() {
                this.page++;
                this.changePage();
            },
            'click .apply-asset': function(e) {
                const file = getFile(e);

                const body = {
                    assets: {}
                };

                // undefined drops it from the JSON
                body.assets[this.type] = file;

                $.ajax('../api/admin/customisation/config', {
                    contentType: "application/json",
                    data: JSON.stringify(body),
                    dataType: 'json',
                    method: 'POST',
                    error: function() {
                        // TODO handle validation errors
                    },
                    success: function() {
                        this.currentAsset = file;

                        this.$('.asset i').addClass('hide');

                        if (file) {
                            this.$('.asset[data-id="' + file +'"] i').removeClass('hide');
                        }
                        else {
                            this.$('.asset:first-child i').removeClass('hide');
                        }
                    }.bind(this)
                })
            },
            'click .delete-asset': function(e) {
                const file = getFile(e);

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
            this.type = options.type;

            this.pageCollection = new Backbone.Collection();

            this.currentAsset = configuration().assetsConfig.assets[options.type];

            this.$headerHtml = $(defaultAssetTemplate({
                currentAsset: this.currentAsset,
                height: this.height,
                imageClass: options.imageClass,
                i18n: i18n,
                width: this.width,
                data: {
                    id: null,
                    deletable: false,
                    url: '/static-' + configuration().commit + options.defaultImage
                }
            }));

            this.assetList = new ListView({
                collection: this.pageCollection,
                headerHtml: this.$headerHtml,
                itemOptions: {
                    className: 'asset',
                    template: this.assetTemplate,
                    templateOptions: {
                        currentAsset: this.currentAsset,
                        height: this.height,
                        imageClass: options.imageClass,
                        i18n: i18n,
                        width: this.width,
                        url: _.result(this.collection, 'url')
                    }
                }
            });

            this.listenTo(this.collection, 'update', function() {
                const currentAssetExists = Boolean(this.collection.find(function(model) {
                    return this.currentAsset === model.id
                }, this));

                this.$headerHtml.find('i').toggleClass('hide', currentAssetExists);

                this.page = 0;

                this.changePage();
            });

            this.listenTo(this.collection, 'remove', function(model) {
                if (this.currentAsset === model.id) {
                    this.$('.asset:first-child i').removeClass('hide');
                }

                this.pageCollection.remove(model);
            });
        },

        render: function() {
            this.$el.html(this.template());

            this.assetList.render();
            this.$('.asset-list').append(this.assetList.$el);
        },

        changePage: function() {
            this.pageCollection.reset();

            this.pageCollection.add(this.collection.slice(this.page * PAGE_SIZE, (this.page + 1) * PAGE_SIZE));

            this.$('.previous').toggleClass('disabled', this.page === 0);
            this.$('.next').toggleClass('disabled', this.page === Math.floor(Math.max(this.collection.length - 1, 0) / PAGE_SIZE));
        }

    });

});
