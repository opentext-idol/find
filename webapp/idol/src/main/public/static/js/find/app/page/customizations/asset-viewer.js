/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'find/app/util/confirm-view',
    'js-whatever/js/list-view',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/customizations/asset-viewer.html',
    'text!find/templates/app/page/customizations/asset.html'
], function(_, $, Backbone, Confirm, ListView, configuration, i18n, template, assetTemplate) {
    'use strict';

    const MESSAGE_CLASSES = {
        error: 'text-error',
        success: 'text-success'
    };

    const PAGE_SIZE = 5;

    const defaultAssetTemplate = _.template('<div class="asset">' + assetTemplate + '</div>');

    function getFile(e) {
        // use null over undefined as it's easier to send null to the server
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
            'click .apply-asset:not(.disabled)': function(e) {
                const file = getFile(e);

                const body = {
                    assets: {}
                };

                body.assets[this.type] = file;

                $.ajax('api/admin/customization/config', {
                    contentType: "application/json",
                    data: JSON.stringify(body),
                    dataType: 'json',
                    method: 'POST',
                    error: function(xhr) {
                        this.setMessage(i18n['customizations.apply.error'](file), MESSAGE_CLASSES.error);

                        this.collection.remove(file);
                    }.bind(this),
                    success: function() {
                        this.currentAsset = file;

                        this.$('.asset i').addClass('hide');
                        this.$('.asset .apply-asset').removeClass('disabled');

                        if(file) {
                            this.toggleAsset(true, file);

                            this.setMessage(i18n['customizations.apply.success'](file), MESSAGE_CLASSES.success);
                        } else {
                            this.toggleAsset(true);

                            this.setMessage(i18n['customizations.applyDefault.success'], MESSAGE_CLASSES.success);
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
                    message: i18n['customizations.delete.message'](file),
                    okText: i18n['app.button.delete'],
                    okClass: 'btn-danger',
                    okIcon: '',
                    title: i18n['customizations.delete.title'],
                    okHandler: _.bind(function() {
                        this.collection.get(file).destroy({
                            wait: true
                        });

                        this.setMessage(i18n['customizations.delete.success'](file), MESSAGE_CLASSES.success);
                    }, this)
                });
            }
        },

        initialize: function(options) {
            this.height = options.height;
            this.width = options.width;
            this.type = options.type;

            this.page = 0;

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
                    url: 'static-' + configuration().commit + options.defaultImage
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

                this.toggleAsset(!currentAssetExists);

                this.page = 0;

                this.changePage();
            });

            this.listenTo(this.collection, 'remove', function(model) {
                if(this.currentAsset === model.id) {
                    this.toggleAsset(true);
                }

                this.pageCollection.remove(model);
            });
        },

        render: function() {
            this.$el.html(this.template());

            this.changePage();

            this.assetList.render();
            this.$('.asset-list').append(this.assetList.$el);

            this.$message = this.$('.message');
        },

        changePage: function() {
            this.pageCollection.reset();

            this.pageCollection.add(this.collection.slice(this.page * PAGE_SIZE, (this.page + 1) * PAGE_SIZE));

            this.$('.previous').toggleClass('disabled', this.page === 0);
            this.$('.next').toggleClass('disabled', this.page === Math.floor(Math.max(this.collection.length - 1, 0) / PAGE_SIZE));
        },

        setMessage: function(message, className) {
            this.$message
                .removeClass(_.values(MESSAGE_CLASSES))
                .addClass(className)
                .text(message);
        },

        toggleAsset: function(active, asset) {
            const $el = asset
                ? this.$('[data-id="' + asset + '"]')
                : this.$headerHtml;

            $el.find('i').toggleClass('hide', !active);
            $el.find('.apply-asset').toggleClass('disabled', active);
        }
    });
});
