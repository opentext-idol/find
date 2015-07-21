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
                var $targetRow = $(e.currentTarget);
                var id = $targetRow.find('[data-id]').data('id');
                this.changeIndex($targetRow);
                $targetRow.find('i').toggleClass('hide');
            }
        },

        initialize: function (options) {
            this.queryModel = options.queryModel;

            this.indexesCollection = new IndexesCollection();

            this.indexes = {};
            this.indexesCollection.fetch();

            this.listenTo(this.indexesCollection, 'sync', function() {
                // Default to searching against all indexes
                this.indexesCollection.each(_.bind(function(indexModel) {
                    this.indexes[indexModel.get('index')] = true;
                }, this));

                this.listView = new ListView({
                    collection: this.indexesCollection,
                    itemOptions: {
                        tagName: 'tr',
                        className: 'clickable',
                        template: this.itemTemplate
                    }
                });

                this.informQueryModel(this.selectedIndexes());

                this.trigger('sync');

                this.listView.setElement(this.$el).render();

            }, this);
        },

        render: function() {
            this.$el.html(this.listTemplate());
        },

        selectedIndexes: function() {
            return _.chain(this.indexes).map(function(value, key) {
                return (value ? key : undefined); // Return names of selected indexes and undefined for unselected ones
            }).compact().value();
        },

        changeIndex: function(index) {
            var toggledIndex = index.find('[data-id]').data('id');
            var isSelected = index.find('i').hasClass('hide');

            this.indexes[toggledIndex] = isSelected;

            var selectedIndexes = this.selectedIndexes();

            this.informQueryModel(selectedIndexes);

            if(selectedIndexes.length === 1) {
                this.$('[data-id="'+selectedIndexes[0]+'"]').parent().addClass('disabled-index');
            } else {
                this.$('[data-id]').parent().removeClass('disabled-index');
            }
        },

        informQueryModel: function(selectedIndexes) {
            this.queryModel.set({
                indexes: selectedIndexes,
                allIndexesSelected : this.indexesCollection.length === selectedIndexes.length
            });
        }
    });
});