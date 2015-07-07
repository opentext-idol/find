define([
    'backbone',
    'underscore',
    'find/app/model/indexes-collection',
    'text!find/templates/app/page/index-list.html',
    'text!find/templates/app/page/index-item.html'
], function(Backbone, _, IndexesCollection, listTemplate, itemTemplate) {
    return Backbone.View.extend({
        listTemplate: _.template(listTemplate),
        itemTemplate: _.template(itemTemplate),

        events: {
            'change .indexCheckbox': function(e) {
                var toggledIndex = $(e.currentTarget).val();
                var checked = $(e.currentTarget).is(':checked');

                this.indexes[toggledIndex] = checked;

                this.queryModel.set('indexes', this.selectedIndexes());
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

                this.queryModel.set('indexes', this.selectedIndexes());

                this.trigger('sync');

                this.indexesCollection.each(function(model) {
                    var htmlTemplateOutput = $(this.itemTemplate({
                        index: model.get('index')
                    }));

                    this.$el.find('.indexes-list').append(htmlTemplateOutput);

                    if (this.indexes[model.get('index')]) { // If index is selected, set the checkbox to checked
                        htmlTemplateOutput.find('input').prop('checked', true);
                    }
                }, this);
            }, this);
        },

        render: function() {
            this.$el.html(this.listTemplate());
        },

        selectedIndexes: function() {
            return _.chain(this.indexes).map(function(value, key) {
                return (value ? key : undefined); // Return names of selected indexes and undefined for unselected ones
            }).compact().value();
        }
    });
});