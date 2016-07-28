define([
    'backbone',
    'underscore',
    'jquery',
    'databases-view/js/databases-view',
    './index-item-view',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/search/filters/indexes/indexes-view.html',
    'text!find/templates/app/page/search/filters/indexes/index-list.html'
], function(Backbone, _, $, DatabasesView, IndexItemView, i18n, template, listTemplate) {

    var CHECKED_CLASS = 'hp-icon hp-fw hp-check';
    var INDETERMINATE_CLASS = 'hp-icon hp-fw hp-minus';
    var DISABLED_CLASS = 'disabled';

    var ICON_SELECTOR = '> span > .database-icon';

    return DatabasesView.extend({
        // will be overridden
        getIndexCategories: $.noop,

        template: _.template(template),
        categoryTemplate: _.template(listTemplate),

        events: {
            'click li[data-id]:not(disabled-index)': function(e) {
                e.stopPropagation();

                var $target = $(e.currentTarget).find('.database-input');
                var index = $target.attr('data-name');
                var domain = $target.attr('data-domain');
                var checked = $target.find('i').hasClass('hp-check');

                this.selectDatabase(index, domain, !checked);
            },
            'click .category-input': function(e) {
                e.stopPropagation();
                var dataTarget = $(e.target).attr('data-target');

                // data-target means they've clicked a chevron, so we want to collapse stuff
                if (dataTarget) {
                    var $target = this.$(dataTarget);
                    $target.collapse('toggle');
                }
                else {
                    var $currentTarget = $(e.currentTarget);
                    var category = $currentTarget.attr('data-category-id');
                    var checked = $currentTarget.find('i').hasClass('hp-check');

                    this.selectCategory(category, !checked);
                }
            },
            'show.bs.collapse': function(e) {
                e.stopPropagation();

                var $parent = $(e.target).parent();
                $parent.find('> span > i[data-target]').removeClass('collapsed');
                $parent.find('> span[data-target]').removeClass('collapsed');
            },
            'hide.bs.collapse': function(e) {
                e.stopPropagation();

                var $parent = $(e.target).parent();
                $parent.find('> span > i[data-target]').addClass('collapsed');
                $parent.find('> span[data-target]').addClass('collapsed');
            }
        },

        initialize: function(options) {
            DatabasesView.prototype.initialize.call(this, _.extend({
                databasesCollection: options.indexesCollection,
                emptyMessage: i18n['search.indexes.empty'],
                topLevelDisplayName: i18n['search.indexes.all'],
                childCategories: this.getIndexCategories(),
                listViewOptions: {
                    ItemView: IndexItemView,
                    useCollectionChange: {
                        deleted: 'updateDeleted'
                    }
                }
            }, options));
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
