define([
    'backbone',
    'underscore',
    'text!find/templates/app/page/index-list.html',
    'text!find/templates/app/page/index-item.html',
    'js-whatever/js/list-view'
], function(Backbone, _, listTemplate, itemTemplate, ListView) {

    return Backbone.View.extend({
        listTemplate: _.template(listTemplate),
        itemTemplate: _.template(itemTemplate),
        className: 'table',
        tagName: 'table',

        events: {
            'click tr': function(e) {
                var toggledIndex = $(e.currentTarget).find('tr[data-id]').data('id');

                this.changeIndex(toggledIndex);
            }
        },

        initialize: function (options) {
            this.queryModel = options.queryModel;
            this.indexesCollection = options.indexesCollection;

            this.indexes = {};

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
                var checkbox = this.$("tr[data-id='" + key + "'] i");

                checkbox.toggleClass('hide', !value);
            }, this);

            var selectedIndexes = this.selectedIndexes();

            if(selectedIndexes.length === 1) {
                this.$('tr[data-id="' + selectedIndexes[0] + '"]').addClass('disabled-index');
            } else {
                this.$('tr[data-id]').removeClass('disabled-index');
            }
        },

        selectAll: function() {
            this.indexesCollection.each(function(indexModel) {
                this.indexes[indexModel.id] = true;
            }, this);

            this.updateQueryModel(this.selectedIndexes());
        },

        updateQueryModel: function(selectedIndexes) {
            this.queryModel.set({
                indexes: selectedIndexes
            });
        }
    });
});
