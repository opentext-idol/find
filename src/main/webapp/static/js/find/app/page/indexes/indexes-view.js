define([
    'backbone',
    'underscore',
    'find/app/model/indexes-collection',
    'text!find/templates/app/page/index-list.html',
    'text!find/templates/app/page/index-item.html',
    'js-whatever/js/list-view'
], function(Backbone, _, IndexesCollection, listTemplate, itemTemplate, ListView) {

    return Backbone.View.extend({
        listTemplate: _.template(listTemplate),
        itemTemplate: _.template(itemTemplate),
        className: 'table',
        tagName: 'table',

        events: {
            'click tr': function(e) {
                var toggledIndex = $(e.currentTarget).find('[data-id]').data('id');

                this.changeIndex(toggledIndex);
            }
        },

        initialize: function (options) {
            this.queryModel = options.queryModel;

            this.indexesCollection = new IndexesCollection();

            this.indexes = {};
            this.indexesCollection.fetch();

            this.listenTo(this.indexesCollection, 'sync', function() {
                this.selectAll();

                this.listView = new ListView({
                    collection: this.indexesCollection,
                    itemOptions: {
                        tagName: 'tr',
                        className: 'clickable',
                        template: this.itemTemplate
                    }
                });

                this.trigger('sync');

                this.listView.setElement(this.$el).render();

            }, this);

            this.listenTo(this.queryModel, 'change:indexes', function(model, queryModelIndexes) {
                this.indexes = {};

                _.each(queryModelIndexes, function(index) {
                    this.indexes[index] = true;
                }, this);

                this.update();
            });
        },

        render: function() {
            this.$el.html(this.listTemplate());
        },

        selectedIndexes: function() {
            return _.chain(this.indexes).map(function(value, key) {
                return (value ? key : undefined); // Return names of selected indexes and undefined for unselected ones
            }).compact().value();
        },

        changeIndex: function(toggledIndex) {
            this.indexes[toggledIndex] = !this.indexes[toggledIndex];

            this.updateQueryModel(this.selectedIndexes());
        },

        update: function() {
            this.$('i').addClass('hide');

            _.each(this.indexes, function(value, key) {
                var checkbox = this.$("[data-id='" + key + "']").parent().find('i');

                checkbox.toggleClass('hide', !value);
            }, this);

            var selectedIndexes = this.selectedIndexes();

            if(selectedIndexes.length === 1) {
                this.$('[data-id="'+selectedIndexes[0]+'"]').parent().addClass('disabled-index');
            } else {
                this.$('[data-id]').parent().removeClass('disabled-index');
            }
        },

        selectAll: function() {
            this.indexesCollection.each(function(indexModel) {
                this.indexes[indexModel.get('index')] = true;
            }, this);

            this.updateQueryModel(this.selectedIndexes());
        },

        updateQueryModel: function(selectedIndexes) {
            this.queryModel.set({
                indexes: selectedIndexes,
                allIndexesSelected: this.indexesCollection.length === selectedIndexes.length
            });
        }
    });
});