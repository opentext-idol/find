define([
    'backbone',
    'underscore',
    'jquery',
    'databases-view/js/databases-view',
    'i18n!find/nls/indexes',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/indexes/indexes-view.html',
    'text!find/templates/app/page/search/filters/indexes/index-list.html',
    'text!find/templates/app/page/search/filters/indexes/index-item.html'
], function(Backbone, _, $, DatabasesView, i18n, findI18n, template, listTemplate, itemTemplate) {

    var CHECKED_CLASS = 'hp-icon hp-fw hp-check';
    var INDETERMINATE_CLASS = 'hp-icon hp-fw hp-minus';
    var DISABLED_CLASS = 'disabled';
    var SHOW_MORE_CLASS = 'hp-chevron-right';
    var SHOW_LESS_CLASS = 'hp-chevron-up';

    var ICON_SELECTOR = '> span > .database-icon';

    return DatabasesView.extend({
        // will be overridden
        getIndexCategories: $.noop,

        template: _.template(template),
        categoryTemplate: _.template(listTemplate),
        databaseTemplate: _.template(itemTemplate),
        seeMoreButtonTemplate: _.template('<li class="toggle-more clickable"><i class="hp-icon <%-showMoreClass%> col-md-1"></i> <span class="toggle-more-text inline-block"><%-i18n["app.seeMore"]%></span></li>'),

        events: {
            'click li[data-id]': function(e) {
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

                $(e.target).parent().find('> span > i[data-target]').removeClass('collapsed');
            },
            'hide.bs.collapse': function(e) {
                e.stopPropagation();

                $(e.target).parent().find('> span > i[data-target]').addClass('collapsed');
            },
            'click .toggle-more': function(e) {
                e.stopPropagation();

                var $currentTarget = $(e.currentTarget);
                this.toggleIndexes($currentTarget.closest('ul'), $currentTarget.hasClass('more'));
            }
        },

        initialize: function(options) {
            DatabasesView.prototype.initialize.call(this, {
                databasesCollection: options.indexesCollection,
                emptyMessage: i18n['search.indexes.empty'],
                selectedDatabasesCollection: options.selectedDatabasesCollection,
                topLevelDisplayName: i18n['search.indexes.all'],
                childCategories: this.getIndexCategories()
            });
        },

        render: function() {
            DatabasesView.prototype.render.call(this);

            _.each(this.getIndexCategories(), function(category) {
                var $ul = this.$('[data-category-id="' + category.name + '"] ul');

                if ($ul.find('li').length > 5) {
                    $ul.append(this.seeMoreButtonTemplate({
                        i18n: findI18n,
                        showMoreClass: SHOW_MORE_CLASS
                    }));

                    this.toggleIndexes($ul, true);
                }
            }, this);
        },

        toggleIndexes: function($ul, toggle) {
            var lastIndexes = $ul.find('li').slice(5);
            lastIndexes.toggleClass('hide', toggle);

            //unhiding see more or see less buttons
            $ul.find('.toggle-more').removeClass('hide');
            $ul.find('.toggle-more').toggleClass('more', !toggle);
            $ul.find('.toggle-more i').toggleClass(SHOW_LESS_CLASS, !toggle);
            $ul.find('.toggle-more i').toggleClass(SHOW_MORE_CLASS, toggle);
            $ul.find('.toggle-more-text').text(toggle ? findI18n["app.seeMore"] : findI18n["app.seeLess"]);
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
