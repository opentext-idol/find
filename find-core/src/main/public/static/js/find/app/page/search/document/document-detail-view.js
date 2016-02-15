/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/vent',
    'i18n!find/nls/bundle',
    'find/app/util/view-server-client',
    'js-whatever/js/list-view',
    'find/app/page/search/document/document-detail-tabs',
    'find/app/configuration',
    'text!find/templates/app/page/search/document/document-detail.html',
    'text!find/templates/app/page/search/document/preview-mode-document.html',
    'text!find/templates/app/page/view/media-player.html'
], function(Backbone, _, vent, i18n, viewClient, ListView, tabs, configuration, template, documentTemplate, mediaTemplate) {
    'use strict';

    var isUrlRegex = /^https?:\/\//;
    function isURL(reference) {
        return isUrlRegex.test(reference);
    }

    return Backbone.View.extend({
        template: _.template(template),
        mediaTemplate: _.template(mediaTemplate),
        documentTemplate: _.template(documentTemplate),

        className: 'row document-detail-flex-container',

        events: {
            'click .detail-view-back-button': function() {
                vent.navigate(this.backUrl);
            },
            'click .detail-view-open-button': function () {
                window.open(this.documentHref, encodeURIComponent(this.documentHref));
            }
        },

        initialize: function(options) {
            this.model = options.model;
            this.backUrl = options.backUrl;

            this.tabs = this.filterTabs(tabs);

            var referenceUrl = isURL(this.model.get('reference')) ?  this.model.get('reference') : '';
            this.documentHref = this.model.get('url') || referenceUrl;
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                title: this.model.get('title'),
                href: this.documentHref,
                tabs: this.tabs,
                mmapBaseUrl: configuration().mmapBaseUrl,
                mmapUrl: this.model.get('mmapUrl')
            }));

            this.renderDocument();
            this.renderTabContent();
        },

        filterTabs: function(tabsToFilter) {
            return _.chain(tabsToFilter)
                .filter(function(tab) {
                    return tab.shown(this.model)
                }, this)
                .map(function(tab, index) {
                    return _.extend({ index: index }, tab)
                })
                .value();
        },

        renderDocument: function () {
            var $preview = this.$('.document-detail-view-container');

            if (this.model.isMedia()) {
                $preview.html(this.mediaTemplate({
                    i18n: i18n,
                    model: this.model
                }));
            } else {
                $preview.html(this.documentTemplate({
                    i18n: i18n
                }));

                this.$iframe = this.$('.preview-document-frame');

                this.$iframe.on('load', _.bind(function() {
                    this.$('.view-server-loading-indicator').addClass('hidden');
                    this.$iframe.removeClass('hidden');
                }, this));

                // The src attribute has to be added retrospectively to avoid a race condition
                var url = viewClient.getHref(this.model.get('reference'), this.model);
                this.$iframe.attr('src', url);
            }
        },

        renderTabContent: function () {
            var $tabContentContainer = this.$('.document-detail-tabs-content');
            _.each(this.tabs, function(tab) {
                var tabContentView = new (tab.TabContentConstructor)({
                    tab: tab,
                    model: this.model
                });

                $tabContentContainer.append(tabContentView.$el);
                tabContentView.render();
            }, this);
        }
    });
});
