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
                this.changeIndex($(e.currentTarget));
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

            this.listenTo(this.queryModel, 'change:indexes', function() {
                this.indexes = {};

                var queryModelIndexes = this.queryModel.get('indexes');

                _.each(queryModelIndexes, function(index) {
                    this.indexes[index] = true;
                }, this);

                this.updateUI();
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

        changeIndex: function($targetRow) {
            var toggledIndex = $targetRow.find('[data-id]').data('id');

            this.indexes[toggledIndex] = !this.indexes[toggledIndex];

            this.informQueryModel(this.selectedIndexes());
        },

        updateUI: function() {
            this.$('i').addClass('hide');

            _.each(this.indexes, function(value, key) {
                var checkbox = this.$("[data-id='" + key + "']").parent().find('i');

                checkbox.toggleClass('hide', !value);
            });

            var selectedIndexes = this.selectedIndexes();

            if(selectedIndexes.length === 1) {
                this.$('[data-id="'+selectedIndexes[0]+'"]').parent().addClass('disabled-index');
            } else {
                this.$('[data-id]').parent().removeClass('disabled-index');
            }
        },

        selectAll: function() {
            this.indexesCollection.each(_.bind(function(indexModel) {
                this.indexes[indexModel.get('index')] = true;
            }, this));

            this.informQueryModel(this.selectedIndexes());
        },

        informQueryModel: function(selectedIndexes) {
            this.queryModel.set({
                indexes: selectedIndexes,
                allIndexesSelected : this.indexesCollection.length === selectedIndexes.length
            });
        }
    });
});