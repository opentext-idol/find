/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'find/app/page/search/document/document-detail-tabs',
    'find/app/util/events',
    'find/app/util/url-manipulator',
    'find/app/util/view-server-client',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/document/document-detail-content-view.html',
    'text!find/templates/app/page/search/document/view-mode-document.html',
    'text!find/templates/app/page/search/document/view-media-player.html'
], function(Backbone, _, tabs, events, urlManipulator, viewClient, i18n, template, documentTemplate, mediaTemplate) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),
        mediaTemplate: _.template(mediaTemplate),
        documentTemplate: _.template(documentTemplate),

        events: {
            'click .document-detail-open-original-link': function() {
                // the link itself is responsible for opening the window
                events().original();
            },
            'click .document-detail-mmap-button': function() {
                this.mmapTab.open(this.documentModel.attributes);
            },
            'shown.bs.tab a[data-toggle=tab]': function(event) {
                const tab = this.tabs[$(event.target).parent().index()];
                tab.view.render();
            }
        },

        initialize: function(options) {
            this.indexesCollection = options.indexesCollection;
            this.mmapTab = options.mmapTab;
            this.documentModel = options.documentModel;

            this.tabs = this.filterTabs(tabs);
        },

        render: function() {
            const url = this.documentModel.get('url');
            const documentHref = url ? urlManipulator.addSpecialUrlPrefix(this.documentModel.get('contentType'), this.documentModel.get('url')) : null;

            this.$el.html(this.template({
                i18n: i18n,
                title: this.documentModel.get('title'),
                href: documentHref,
                tabs: this.tabs,
                mmap: this.mmapTab.supported(this.documentModel.attributes)
            }));

            this.renderDocument();
            this.renderTabContent();
        },

        filterTabs: function(tabList) {
            return _.chain(tabList)
                .filter(function(tab) {
                    return tab.shown(this.documentModel);
                }, this)
                .map(function(tab, index) {
                    return _.extend({index: index}, tab);
                })
                .value()
        },

        renderDocument: function() {
            const $preview = this.$('.document-detail-view-container');

            if (this.documentModel.isMedia()) {
                $preview.html(this.mediaTemplate({
                    i18n: i18n,
                    model: this.documentModel
                }));
            } else {
                $preview.html(this.documentTemplate({
                    i18n: i18n
                }));

                const iframe = this.$('.preview-document-frame');

                iframe.on('load', _.bind(function() {
                    // Cannot execute scripts in iframe or detect error event, so look for attribute on html
                    if (iframe.contents().find('html').data('hpeFindAuthError')) {
                        window.location.reload();
                    }

                    this.$('.view-server-loading-indicator').addClass('hidden');
                    iframe.removeClass('hidden');

                    // View server adds script tags to rendered PDF documents, which are blocked by the application
                    // This replicates their functionality
                    iframe.contents().find('.InvisibleAbsolute').hide();
                }, this));

                // The src attribute has to be added retrospectively to avoid a race condition
                const url = viewClient.getHref(this.documentModel.get('reference'), this.documentModel);
                iframe.attr('src', url);
            }
        },

        renderTabContent: function() {
            const $tabContentContainer = this.$('.document-detail-tabs-content');

            _.each(this.tabs, function(tab) {
                tab.view = new (tab.TabContentConstructor)({
                    tab: tab,
                    model: this.documentModel,
                    indexesCollection: this.indexesCollection
                });

                $tabContentContainer.append(tab.view.$el);
            }, this);

            if (this.tabs.length !== 0) {
                this.tabs[0].view.render();
            }
        },

        remove: function() {
            _.each(this.tabs, function(tab) {
                tab.view && tab.view.remove();
            });

            Backbone.View.prototype.remove.call(this);
        }
    });

});
