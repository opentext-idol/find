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
    'text!find/templates/app/page/search/document/view-mode-document.html',
    'text!find/templates/app/page/search/document/view-media-player.html'
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
            },
            'shown.bs.tab a[data-toggle=tab]': function (event) {
                var tab = this.tabs[$(event.target).parent().index()];
                tab.view.render();
            }
        },

        initialize: function(options) {
            this.model = options.model;
            this.backUrl = options.backUrl;
            this.indexesCollection = options.indexesCollection;

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
                    return tab.shown(this.model);
                }, this)
                .map(function(tab, index) {
                    return _.extend({ index: index }, tab);
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
                    // Cannot execute scripts in iframe or detect error event, so look for attribute on html
                    if(this.$iframe.contents().find('html').data('hpeFindAuthError')) {
                        window.location.reload();
                    }

                    this.$('.view-server-loading-indicator').addClass('hidden');
                    this.$iframe.removeClass('hidden');

                    // View server adds script tags to rendered PDF documents, which are blocked by the application
                    // This replicates their functionality
                    this.$iframe.contents().find('.InvisibleAbsolute').hide();
                }, this));

                // The src attribute has to be added retrospectively to avoid a race condition
                var url = viewClient.getHref(this.model.get('reference'), this.model);
                this.$iframe.attr('src', url);
            }
        },

        renderTabContent: function () {
            var $tabContentContainer = this.$('.document-detail-tabs-content');
            _.each(this.tabs, function(tab) {
                tab.view = new (tab.TabContentConstructor)({
                    tab: tab,
                    model: this.model,
                    indexesCollection: this.indexesCollection
                });

                $tabContentContainer.append(tab.view.$el);
            }, this);

            if (this.tabs.length !== 0) this.tabs[0].view.render();
        },

        remove: function() {
            _.each(this.tabs, function(tab) {
                tab.view && tab.view.remove();
            });

            Backbone.View.prototype.remove.call(this);
        }
    });
});
