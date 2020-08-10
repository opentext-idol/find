/*
 * (c) Copyright 2017 Micro Focus or one of its affiliates.
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
    'backbone',
    'find/app/page/search/document/document-detail-tabs',
    'find/app/util/events',
    'find/app/util/url-manipulator',
    'find/app/util/view-server-client',
    'find/app/page/search/document/document-preview-helper',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/document/document-detail-content-view.html'
], function(_, Backbone, tabs, events, urlManipulator, viewClient, DocumentPreviewHelper, i18n,
            template) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(template),

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
            this.documentRenderer = options.documentRenderer;
            this.mmapTab = options.mmapTab;
            this.documentModel = options.documentModel;

            this.tabs = this.filterTabs(tabs);
        },

        render: function() {
            const url = this.documentModel.get('url');
            const documentHref = url
                ? urlManipulator.addSpecialUrlPrefix(
                    this.documentModel.get('contentType'),
                    this.documentModel.get('url'))
                : viewClient.getHref(this.documentModel, false, true);

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
            DocumentPreviewHelper.showPreview(
                this.$('.document-detail-view-container'), this.documentModel, null);
        },

        renderTabContent: function() {
            const $tabContentContainer = this.$('.document-detail-tabs-content');

            _.each(this.tabs, function(tab) {
                tab.view = new (tab.TabContentConstructor)({
                    tab: tab,
                    model: this.documentModel,
                    indexesCollection: this.indexesCollection,
                    documentRenderer: this.documentRenderer
                });

                $tabContentContainer.append(tab.view.$el);
            }, this);

            if(this.tabs.length !== 0) {
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
