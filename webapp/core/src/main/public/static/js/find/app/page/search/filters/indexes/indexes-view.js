/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'databases-view/js/databases-view',
    './index-item-view',
    'i18n!find/nls/indexes',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/indexes/index-list.html'
], function(_, $, Backbone, DatabasesView, IndexItemView, i18nIndexes, i18n, listTemplate) {
    'use strict';

    const CHECKED_CLASS = 'hp-icon hp-fw hp-check';
    const INDETERMINATE_CLASS = 'hp-icon hp-fw hp-minus';
    const DISABLED_CLASS = 'disabled';

    const ICON_SELECTOR = '> span > .database-icon';

    const DEBOUNCE_TIMEOUT = 500;

    return DatabasesView.extend({
        // will be overridden
        getIndexCategories: null,
        databaseHelper: null,

        categoryTemplate: _.template(listTemplate),

        events: {
            'click li[data-id]:not(disabled-index)': function(e) {
                e.stopPropagation();

                const $target = $(e.currentTarget).find('.database-input');
                //noinspection JSUnresolvedFunction
                const args = this.findInCurrentSelectionArguments($target);
                const $i = $target.find('i');
                const checked = $i.hasClass('hp-check');

                this.selectDatabase(args, !checked, 'pendingSelection');

                this.updateWithPendingSelection();
            },
            'click .category-input': function(e) {
                e.stopPropagation();

                const $currentTarget = $(e.currentTarget);
                const category = $currentTarget.attr('data-category-id');
                const checked = $currentTarget.find('i').hasClass('hp-check');

                this.selectCategory(category, !checked, 'pendingSelection');

                this.updateWithPendingSelection();
            },
            'click .category-input [data-target]': function(e) {
                e.stopPropagation();

                const dataTarget = $(e.target).attr('data-target');

                const $target = this.$(dataTarget);
                $target.collapse('toggle');
            },
            'show.bs.collapse': function(e) {
                e.stopPropagation();

                const $parent = $(e.target).parent();
                $parent.find('> span > i[data-target]').removeClass('collapsed');
                $parent.find('> span[data-target]').removeClass('collapsed');
            },
            'hide.bs.collapse': function(e) {
                e.stopPropagation();

                const $parent = $(e.target).parent();
                $parent.find('> span > i[data-target]').addClass('collapsed');
                $parent.find('> span[data-target]').addClass('collapsed');
            }
        },

        initialize: function(options) {
            const indexCategories = this.getIndexCategories();

            DatabasesView.prototype.initialize.call(this, _.extend({
                databasesCollection: options.indexesCollection,
                emptyMessage: i18nIndexes['search.indexes.empty'],
                topLevelDisplayName: i18nIndexes['search.indexes.all'],
                childCategories: indexCategories.length < 2
                    ? null
                    : indexCategories,
                databaseHelper: this.databaseHelper,
                listViewOptions: {
                    ItemView: IndexItemView,
                    useCollectionChange: {
                        deleted: 'updateDeleted'
                    }
                }
            }, options));

            this.pendingSelection = _.clone(this.currentSelection);

            this.updateWithPendingSelection = _.debounce(this.updateWithPendingSelection, DEBOUNCE_TIMEOUT);
        },

        getTemplateOptions: function() {
            return {loading: i18n['app.loading']};
        },

        updateWithPendingSelection: function() {
            this.currentSelection = _.clone(this.pendingSelection);

            this.updateCheckedOptions();
            this.updateSelectedDatabases();
        },

        updateSelectedDatabases: function() {
            DatabasesView.prototype.updateSelectedDatabases.apply(this, arguments);

            this.pendingSelection = _.clone(this.currentSelection);
        },

        check: function($input) {
            $input.find(ICON_SELECTOR).removeClass(INDETERMINATE_CLASS).addClass(CHECKED_CLASS);
        },

        uncheck: function($input) {
            $input.find(ICON_SELECTOR).removeClass(CHECKED_CLASS).removeClass(INDETERMINATE_CLASS);
        },

        enable: function($input) {
            $input.find(ICON_SELECTOR).removeClass(DISABLED_CLASS);
        },

        disable: function($input) {
            $input.find(ICON_SELECTOR).addClass(DISABLED_CLASS);
        },

        determinate: function($input) {
            $input.find(ICON_SELECTOR).removeClass(INDETERMINATE_CLASS);
        },

        indeterminate: function($input) {
            $input.find(ICON_SELECTOR).addClass(INDETERMINATE_CLASS);
        }
    });
});
