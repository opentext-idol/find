define([
    'backbone',
    'underscore',
    'jquery',
    'find/app/page/search/search-tab-item-view',
    'find/app/vent',
    'js-whatever/js/list-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/tabbed-search-view.html'
], function(Backbone, _, $, TabItemView, vent, ListView, i18n, template) {

    'use strict';

    var html = _.template(template)({i18n: i18n});
    var startNewSearchHtml = _.template('<li class="start-new-search m-t-md m-l-sm m-r-sm" data-toggle="tooltip" data-placement="bottom" title="<%-i18n[\'search.newTab.tooltip\']%>"><a><i class="hp-icon hp-add"></i></a></li>')({i18n: i18n});
    var shownTabsSelector = '.nav-tabs > li';
    var menuTabsSelector = '.dropdown-menu > li';

    return Backbone.View.extend({
        events: {
            'click .search-tab': function(event) {
                this.model.set('selectedSearchCid', $(event.currentTarget).find('[data-search-cid]').attr('data-search-cid'));
            },
            'click .start-new-search': function() {
                this.trigger('startNewSearch');
            }
        },

        initialize: function(options) {
            this.savedSearchCollection = options.savedSearchCollection;

            this.tabListView = new ListView({
                collection: this.savedSearchCollection,
                ItemView: TabItemView,
                headerHtml: startNewSearchHtml,
                itemOptions: {
                    queryStates: options.queryStates
                }
            });

            this.hiddenTabListView = new ListView({
                collection: this.savedSearchCollection,
                ItemView: TabItemView,
                itemOptions: {
                    queryStates: options.queryStates
                }
            });

            // Update the displayed tabs after the current event loop, but only once
            this.checkTabSize = _.debounce(_.bind(function () {
                this.showHideTabs();

                var $activeTab = this.$(shownTabsSelector + '.active');
                if ($activeTab.length > 0) {
                    this.showSelectedTab($activeTab);

                    //Affect drop-down button
                    this.$('.tab-drop').toggleClass('invisible', this.$(menuTabsSelector + ':not(.hide)').length === 0); // use invisible rather than hide to keep size for width calculations
                }
            }, this), 0);

            this.listenTo(this.model, 'change:selectedSearchCid', this.updateSelectedTab);
            this.listenTo(this.savedSearchCollection, 'update', this.checkTabSize);
        },

        showHideTabs: function () {
            var tabAreaWidth = this.getTabAreaWidths();
            var tabWidths = 0;

            _.each(this.$(shownTabsSelector), function (el) {
                tabWidths += $(el).outerWidth(); //padding
                this.getMenuItem(el).toggleClass('hide', tabWidths <= tabAreaWidth);
            }, this);
        },

        showSelectedTab: function ($activeTab) {
            var activeCid = $activeTab.find('a').data('search-cid');
            var $activeMenuTab = this.$(menuTabsSelector).find('[data-search-cid="' + activeCid + '"]').parent();

            if (!$activeMenuTab.hasClass('hide')) {
                var $prevDropDownTab = this.$(menuTabsSelector + ':not(.hide)').first();
                var prevTabCid = $prevDropDownTab.find('a').attr('data-search-cid');
                var $prevTab = this.$(shownTabsSelector).find('[data-search-cid="' + prevTabCid + '"]').parent();
                while (!$activeMenuTab.hasClass('hide') && !$activeTab.prev().hasClass('start-new-search')) {
                    $activeTab.insertBefore($prevTab);
                    $activeMenuTab.insertBefore($prevDropDownTab);
                    $prevTab = $activeTab.prev();
                    $prevDropDownTab = $activeMenuTab.prev();
                    this.showHideTabs();
                }
            }
        },

        getTabAreaWidths: function () {
            var dropDownButtonWidth = this.$('.tab-drop').width();
            return this.$('.nav-tabs').width() - dropDownButtonWidth;
        },

        getMenuItem: function(el) {
            var cid = $(el.firstChild).data('search-cid');
            return this.$(menuTabsSelector).find('[data-search-cid="' + cid + '"]').parent()
        },

        render: function() {
            this.$el.html(html);

            this.tabListView.setElement(this.$('.search-tabs-list')).render();
            this.hiddenTabListView.setElement(this.$('.dropdown-menu')).render();
            this.updateSelectedTab();
            this.listenTo(vent, 'vent:resize', this.checkTabSize);
            this.$('.start-new-search').tooltip({delay: 100});
        },

        updateSelectedTab: function() {
            var cid = this.model.get('selectedSearchCid');
            this.$('.search-tab').removeClass('active');

            if (cid) {
                this.$('[data-search-cid="' + cid + '"]').closest('.search-tab').addClass('active');
                this.checkTabSize();
            }
        }
    });

});
