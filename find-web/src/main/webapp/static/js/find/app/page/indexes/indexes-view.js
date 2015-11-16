define([
    '../../../../../bower_components/backbone/backbone',
    'underscore',
    'jquery',
    'databases-view/js/databases-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/indexes/indexes-view.html',
    'text!find/templates/app/page/indexes/index-list.html',
    'text!find/templates/app/page/indexes/index-item.html'
], function(Backbone, _, $, DatabasesView, i18n, template, listTemplate, itemTemplate) {

    var CHECKED_CLASS = 'icon-ok';
    var INDETERMINATE_CLASS = 'icon-minus';
    var DISABLED_CLASS = 'disabled';

    var ICON_SELECTOR = '> span > .database-icon';

    return DatabasesView.extend({
        template: _.template(template),
        categoryTemplate: _.template(listTemplate),
        databaseTemplate: _.template(itemTemplate),

        events: {
            'click li[data-id]': function(e) {
                e.stopPropagation();

                var $target = $(e.currentTarget).find('.database-input');
                var index = $target.attr('data-name');
                var domain = $target.attr('data-domain');
                var checked = $target.find('i').hasClass('icon-ok');

                this.selectDatabase(index, domain, !checked);
            },
            'click .category-input': function(e) {
                // data-target means they've clicked a chevron, so we want to collapse stuff
                if (!$(e.target).attr('data-target')) {
                    e.stopPropagation();

                    var $target = $(e.currentTarget);
                    var category = $target.attr('data-category-id');
                    var checked = $target.find('i').hasClass('icon-ok');

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
            DatabasesView.prototype.initialize.call(this, {
                databasesCollection: options.indexesCollection,
                emptyMessage: i18n['search.indexes.empty'],
                selectedDatabasesCollection: options.selectedDatabasesCollection,
                topLevelDisplayName: i18n['search.indexes.all'],
                childCategories: [
                    {
                        name: 'public',
                        displayName: i18n['search.indexes.publicIndexes'],
                        className: 'list-unstyled',
                        filter: function(model) {
                            return model.get('domain') === 'PUBLIC_INDEXES';
                        }
                    }, {
                        name: 'private',
                        displayName: i18n['search.indexes.privateIndexes'],
                        className: 'list-unstyled',
                        filter: function(model) {
                            return model.get('domain') !== 'PUBLIC_INDEXES';
                        }
                    }
                ]
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
