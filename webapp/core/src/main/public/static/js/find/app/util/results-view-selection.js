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
    'text!find/templates/app/util/selector.html'
], function(_, $, Backbone, i18n, selectorTemplate) {
    'use strict';

    const LIST_RESULTS_VIEW_ID = 'list';

    return Backbone.View.extend({
        selectorTemplate: _.template(selectorTemplate, {variable: 'data'}),

        events: {
            'shown.bs.tab [data-tab-id]': function(event) {
                this.model.set('selectedTab', $(event.target).attr('data-tab-id'));
            }
        },

        initialize: function(options) {
            this.views = options.views;
            this.model = options.model;
            this.queryModel = options.queryModel;

            if (this.queryModel) {
                this.listenTo(this.queryModel,
                    'change:editingDocumentSelection', this.updateTabForDocumentSelection);
                this.listenTo(this.model,
                    'change:selectedTab', this.updateEditingDocumentSelection);
            }
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

        switchTab: function(tab, routeParams) {
            const $tab = this.$('[data-tab-id = "' + tab + '"]');
            if($tab) {
                $tab.tab('show');

                if(routeParams && routeParams.length) {
                    const view = _.find(this.views, {id: tab});
                    if (view && view.content && view.content.setRouteParams) {
                        view.content.setRouteParams(routeParams);
                    }
                }
            }
        },

        updateEditingDocumentSelection: function () {
            // disable document selection mode on context switch to avoid confusion on what mode
            // the user is in
            if (this.model.get('selectedTab') !== LIST_RESULTS_VIEW_ID) {
                this.queryModel.set('editingDocumentSelection', false);
            }
        },

        updateTabForDocumentSelection: function () {
            if (this.queryModel.get('editingDocumentSelection')) {
                this.switchTab(LIST_RESULTS_VIEW_ID);
            }
        }

    });
});
