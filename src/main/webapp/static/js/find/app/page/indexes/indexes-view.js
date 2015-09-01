define([
    'backbone',
    'underscore',
    'databases-view/js/databases-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/indexes/indexes-view.html',
    'text!find/templates/app/page/indexes/index-list.html',
    'text!find/templates/app/page/indexes/index-item.html'
], function(Backbone, _, DatabasesView, i18n, template, listTemplate, itemTemplate) {

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
                var index = $target.attr('data-id');
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
            this.queryModel = options.queryModel;

            this.on('change', function() {
                this.updateQueryModel(this.getSelection())
            }, this);

            DatabasesView.prototype.initialize.call(this, {
                databasesCollection: options.indexesCollection,
                emptyMessage: i18n['search.indexes.empty'],
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

            this.listenTo(this.queryModel, 'change:indexes', function(model, queryModelIndexes) {
                this.currentSelection = _.map(this.collection.filter(function (model) {
                    return _.contains(queryModelIndexes, model.id);
                }), function (model) {
                    return model.pick('name', 'domain');
                });

                if(!this.forceSelection && this.currentSelection.length === options.indexesCollection.size()) {
                    this.currentSelection = [];
                }

                this.updateCheckedOptions();
            });
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
        },

        updateQueryModel: function(selectedIndexes) {
            this.queryModel.set({
                indexes: _.map(selectedIndexes, function(index) {
                    return this.collection.findWhere(index).id;
                }, this)
            });
        }
    });
});
