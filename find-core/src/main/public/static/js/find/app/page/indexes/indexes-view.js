define([
    'backbone',
    'underscore',
    'jquery',
    'databases-view/js/databases-view',
    'i18n!find/nls/indexes',
    'text!find/templates/app/page/indexes/indexes-view.html',
    'text!find/templates/app/page/indexes/index-list.html',
    'text!find/templates/app/page/indexes/index-item.html'
], function(Backbone, _, $, DatabasesView, i18n, template, listTemplate, itemTemplate) {

    var CHECKED_CLASS = 'fa-check';
    var INDETERMINATE_CLASS = 'fa-minus';
    var DISABLED_CLASS = 'disabled';

    var ICON_SELECTOR = '> span > .database-icon';

    return DatabasesView.extend({
        // will be overridden
        getIndexCategories: $.noop,

        template: _.template(template),
        categoryTemplate: _.template(listTemplate),
        databaseTemplate: _.template(itemTemplate),

        events: {
            'click li[data-id]': function(e) {
                e.stopPropagation();

                var $target = $(e.currentTarget).find('.database-input');
                var index = $target.attr('data-name');
                var domain = $target.attr('data-domain');
                var checked = $target.find('i').hasClass('fa-check');

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
                    var checked = $currentTarget.find('i').hasClass('fa-check');

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
