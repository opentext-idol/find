define([
    'backbone',
    'find/app/model/query-model',
    'text!find/templates/app/page/search/sort-view.html',
    'i18n!find/nls/bundle'
], function(Backbone, QueryModel, template, i18n) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'click [data-sort]': function(e) {
                var sortType = $(e.currentTarget).attr('data-sort');
                this.queryModel.set('sort', sortType);
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
            this.listenTo(this.queryModel, 'change:sort', this.updateCurrentSort);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n,
                sortTypes: QueryModel.Sort
            }));

            this.$currentSort = this.$('.current-search-sort');
            this.updateCurrentSort();
        },

        updateCurrentSort: function() {
            if (this.$currentSort) {
                this.$currentSort.text(i18n['search.resultsSort.' + this.queryModel.get('sort')]);
            }
        }
    });

});