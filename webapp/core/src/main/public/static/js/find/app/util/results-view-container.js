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
    'i18n!find/nls/bundle',
    'text!find/templates/app/util/content-container.html'
], function(_, $, Backbone, i18n, contentContainerTemplate) {
    'use strict';

    return Backbone.View.extend({
        contentContainerTemplate: _.template(contentContainerTemplate, {variable: 'data'}),

        initialize: function(options) {
            this.views = options.views;
            this.model = options.model;

            this.listenTo(this.model, 'change:selectedTab', this.selectTab);
        },

        render: function() {
            this.$tabContent = $('<div class="tab-content"></div>');

            const selectedTab = this.model.get('selectedTab');

            _.each(this.views, function(viewData) {
                const $viewElement = $(this.contentContainerTemplate(viewData))
                    .toggleClass('active', viewData.id === selectedTab)
                    .appendTo(this.$tabContent);

                viewData.content = new viewData.Constructor(viewData.constructorArguments);

                _.each(viewData.events, function(listener, eventName) {
                    this.listenTo(viewData.content, eventName, listener);
                }, this);

                viewData.content.setElement($viewElement);
            }, this);

            this.$el.html(this.$tabContent);
            this.selectTab();
        },

        selectTab: function() {
            const tabId = this.model.get('selectedTab');
            const viewData = _.findWhere(this.views, {id: tabId});

            // Deactivate all tabs and activate the selected tab
            this.$tabContent.find('> .active').removeClass('active');
            this.$tabContent.find('#' + viewData.uniqueId).addClass('active');

            if(viewData) {
                if(!viewData.rendered) {
                    viewData.content.render();
                    viewData.rendered = true;
                }
                else if(viewData.content.update) {
                    viewData.content.update();
                }
            }
        },

        updateTab: function() {
            const tabId = this.model.get('selectedTab');
            const viewData = _.findWhere(this.views, {id: tabId});
            if(viewData.content.update) {
                viewData.content.update();
            }
        },

        remove: function() {
            _.chain(this.views)
                .pluck('content')
                .invoke('remove');

            Backbone.View.prototype.remove.call(this);
        }
    });
});
