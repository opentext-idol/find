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
    'find/app/page/search/search-tab-item-view',
    'find/app/vent',
    'js-whatever/js/list-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/tabbed-search-view.html',
    'text!find/templates/app/page/search/saved-search-dropdown.html'
], function(_, $, Backbone, TabItemView, vent, ListView, i18n, template, savedSearchDropdownTemplate) {
    'use strict';

    const html = _.template(template)({i18n: i18n});
    const startNewSearchHtml = _.template('<li class="start-new-search m-t-xs m-l-sm m-r-sm" data-toggle="tooltip" data-placement="bottom" title="<%-i18n[\'search.newTab.tooltip\']%>"><a><i class="hp-icon hp-add"></i></a></li>')({i18n: i18n});
    const shownTabsSelector = '.nav-tabs > li';
    const menuTabsSelector = '.js-saved-searches-tabs-dropdown .dropdown-menu > li';
    const savedSearchDropdown = _.template(savedSearchDropdownTemplate)({i18n:i18n});
    const emptySharedSearchDropdown = _.template('<li class="js-no-shared-searches"><a class="no-shared-searches-message"><%-i18n["search.savedSearchControl.sharedByOthers.empty"]%></a></li>')({i18n:i18n});

    return Backbone.View.extend({
        events: {
            'click .search-tab': function(event) {
                const $currentTarget = $(event.currentTarget);
                const currentModelCid = $currentTarget.find('[data-search-cid]').attr('data-search-cid');
                const currentQueryModel = $currentTarget.parent().hasClass('js-shared-by-others-dropdown-list')
                    ? this.sharedSavedSearchCollection.get(currentModelCid)
                    : this.savedSearchCollection.get(currentModelCid);

                this.model.set('selectedSearchCid', currentModelCid);

                if(currentQueryModel.get('newDocuments') !== 0) {
                    currentQueryModel.trigger('refresh');
                }

                currentQueryModel.set({
                    newDocuments: 0,
                    // disable document selection mode on context switch to avoid confusion on what
                    // mode the user is in
                    editingDocumentSelection: false
                });
            },
            'click .start-new-search': function() {
                vent.navigate('search/query', {trigger: false});
                this.trigger('startNewSearch');
            }
        },

        initialize: function(options) {
            this.savedSearchCollection = options.savedSearchCollection;
            this.sharedSavedSearchCollection = options.sharedSavedSearchCollection;

            this.tabListView = new ListView({
                collection: this.savedSearchCollection,
                ItemView: TabItemView,
                headerHtml: startNewSearchHtml,
                footerHtml: savedSearchDropdown,
                itemOptions: {
                    queryStates: options.queryStates,
                    searchTypes: options.searchTypes
                }
            });

            this.hiddenTabListView = new ListView({
                collection: this.savedSearchCollection,
                ItemView: TabItemView,
                itemOptions: {
                    queryStates: options.queryStates,
                    searchTypes: options.searchTypes
                }
            });

            this.sharedSearchesListView = new ListView({
                collection: this.sharedSavedSearchCollection,
                ItemView: TabItemView,
                footerHtml: emptySharedSearchDropdown,
                itemOptions: {
                    queryStates: options.queryStates,
                    searchTypes: options.searchTypes
                }
            });

            // Update the displayed tabs after the current event loop, but only once
            this.checkTabSize = _.debounce(_.bind(function() {
                this.showHideTabs();

                const $activeTab = this.$(shownTabsSelector + '.active');

                if($activeTab.length > 0) {
                    this.showSelectedTab($activeTab);
                }

                //Affect drop-down button
                this.$('.js-saved-searches-tabs-dropdown').toggleClass('invisible', this.$(menuTabsSelector + ':not(.hide)').length === 0); // use invisible rather than hide to keep size for width calculations
            }, this), 0);

            this.listenTo(this.model, 'change:selectedSearchCid', this.updateSelectedTab);
            this.listenTo(this.savedSearchCollection, 'update', this.checkTabSize);
            this.listenTo(this.sharedSavedSearchCollection, 'add remove', function(changedModel, collection) {
                this.$('.js-no-shared-searches').toggleClass('hide', collection.length > 0);
            });

            this.listenTo(this.savedSearchCollection, 'change:newDocuments', function() {
                const dropdownCids = this.$('.js-saved-searches-tabs-dropdown .dropdown-menu li:not(.hide)').map(function(arg, el) {
                    return $(el).find('a').attr('data-search-cid');
                });

                const totalNewDocuments = this.savedSearchCollection
                    .chain()
                    .filter(function(model) {
                        return _.contains(dropdownCids, model.cid);
                    })
                    .reduce(function(memo, model) {
                        return memo + model.get('newDocuments');
                    }, 0)
                    .value();

                this.$('.tab-drop .new-document-label')
                    .toggleClass('hide', totalNewDocuments === 0)
                    .text(totalNewDocuments);
            });
        },

        showHideTabs: function() {
            const tabAreaWidth = this.getTabAreaWidths();
            let tabWidths = 0;

            _.each(this.$(shownTabsSelector), function(el) {
                tabWidths += $(el).outerWidth(); //padding
                this.getMenuItem(el).toggleClass('hide', tabWidths <= tabAreaWidth);
            }, this);
        },

        showSelectedTab: function($activeTab) {
            const activeCid = $activeTab.find('a').data('search-cid');
            const $activeMenuTab = this.$(menuTabsSelector).find('[data-search-cid="' + activeCid + '"]').parent();

            if(!$activeMenuTab.hasClass('hide')) {
                let $prevDropDownTab = this.$(menuTabsSelector + ':not(.hide)').first();
                let prevTabCid = $prevDropDownTab.find('a').attr('data-search-cid');
                let $prevTab = this.$(shownTabsSelector).find('[data-search-cid="' + prevTabCid + '"]').parent();
                while(!$activeMenuTab.hasClass('hide') && !$activeTab.prev().hasClass('start-new-search')) {
                    $activeTab.insertBefore($prevTab);
                    $activeMenuTab.insertBefore($prevDropDownTab);
                    $prevTab = $activeTab.prev();
                    $prevDropDownTab = $activeMenuTab.prev();
                    this.showHideTabs();
                }
            }
        },

        getTabAreaWidths: function() {
            const dropDownButtonWidth = this.$('.tab-drop').width();
            return this.$('.nav-tabs').width() - dropDownButtonWidth;
        },

        getMenuItem: function(el) {
            const cid = $(el.firstChild).data('search-cid');
            return this.$(menuTabsSelector).find('[data-search-cid="' + cid + '"]').parent()
        },

        render: function() {
            this.$('.start-new-search').tooltip('destroy');
            this.$el.html(html);

            this.tabListView.setElement(this.$('.search-tabs-list')).render();
            this.hiddenTabListView.setElement(this.$('.js-saved-searches-tabs-dropdown .dropdown-menu')).render();
            this.sharedSearchesListView.setElement(this.$('.js-shared-by-others-dropdown .dropdown-menu')).render();

            this.updateSelectedTab();
            this.listenTo(vent, 'vent:resize', this.checkTabSize);
            this.$('.start-new-search').tooltip({delay: 100});
        },

        remove: function() {
            this.$('.start-new-search').tooltip('destroy');
            Backbone.View.prototype.remove.call(this);
        },

        updateSelectedTab: function() {
            const cid = this.model.get('selectedSearchCid');
            let $currentSearchTab = this.$('[data-search-cid="' + cid + '"]').closest('.search-tab');
            this.$('.search-tab').removeClass('active');
            this.$('.js-shared-by-others-dropdown').removeClass('active');

            if(cid) {
                if($currentSearchTab.parent().hasClass('js-shared-by-others-dropdown-list')) {
                    $currentSearchTab.parent().parent().addClass('active');
                }

                $currentSearchTab.addClass('active');
                this.checkTabSize();
            }
        }
    });
});
