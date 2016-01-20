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

    var CHECKED_CLASS = 'hp-check';
    var INDETERMINATE_CLASS = 'hp-remove';
    var DISABLED_CLASS = 'disabled';

    var ICON_SELECTOR = '> span > .database-icon';

    return DatabasesView.extend({
        // will be overridden
        getIndexCategories: $.noop,

        template: _.template(template),
        categoryTemplate: _.template(listTemplate),
        databaseTemplate: _.template(itemTemplate),
        seeMoreButtonTemplate: _.template('<li class="toggle-more clickable"><i class="hp-icon hp-plus col-md-1"></i> <span class="toggle-more-text inline-block"><%-i18n["app.seeMore"]%></span></li>'),

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

                this.toggleIndexes($(e.currentTarget).hasClass('more'));
            }
        },

        initialize: function (options) {
            var childCategories = this.getIndexCategories();
            DatabasesView.prototype.initialize.call(this, {
                databasesCollection: options.indexesCollection,
                emptyMessage: i18n['search.indexes.empty'],
                selectedDatabasesCollection: options.selectedDatabasesCollection,
                topLevelDisplayName: i18n['search.indexes.all'],
                childCategories: childCategories
            });

            var setInitialSelection = _.bind(function() {
                var privateIndexes = options.indexesCollection.reject({domain: 'PUBLIC_INDEXES'});

                if(privateIndexes.length > 0) {
                    _.each(privateIndexes, function(index) {
                        this.selectDatabase(index.get('name'), index.get('domain'), true);
                    }, this);
                }
                else {
                    _.each(options.indexesCollection.where({domain: 'PUBLIC_INDEXES'}), function(index) {
                        this.selectDatabase(index.get('name'), index.get('domain'), true);
                    }, this);
                }
            }, this);

            if(options.indexesCollection.isEmpty()) {
                options.indexesCollection.once('update', setInitialSelection);
            }
            else {
                setInitialSelection();
            }
        },

        render: function() {
            DatabasesView.prototype.render.call(this);

            if(this.$('[data-category-id="public"] ul li').length > 5) {
                this.$('[data-category-id="public"] ul').append(this.seeMoreButtonTemplate({i18n:findI18n}));
                this.toggleIndexes(true);
            }
        },

        toggleIndexes: function(toggle) {
            var lastIndexes = this.$('[data-category-id="public"] ul li').slice(5);
            lastIndexes.toggleClass('hide', toggle);

            //unhiding see more or see less buttons
            this.$('.toggle-more').removeClass('hide');
            this.$('.toggle-more').toggleClass('more', !toggle);
            this.$('.toggle-more i').toggleClass('hp-minus', !toggle);
            this.$('.toggle-more i').toggleClass('hp-add', toggle);
            this.$('.toggle-more-text').text(toggle ? findI18n["app.seeMore"] : findI18n["app.seeLess"]);
        },

        check: function($input) {
            $input.find(ICON_SELECTOR).addClass(CHECKED_CLASS).removeClass(INDETERMINATE_CLASS);
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
