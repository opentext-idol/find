define([
    'backbone',
    'underscore',
    'find/app/model/indexes-collection',
    'text!find/templates/app/page/indexes/indexes-popover-button.html',
    'text!find/templates/app/page/index-popover.html',
    'text!find/templates/app/page/index-popover-contents.html'
], function(Backbone, _, IndexesCollection, indexesContainTemplate, popoverTemplate, contentTemplate) {
    return Backbone.View.extend({
        indexesContain: _.template(indexesContainTemplate),
        popover: _.template(popoverTemplate),
        content: _.template(contentTemplate),

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
                    var htmlTemplateOutput = $(this.content({
                        index: model.get('index')
                    }));

                    this.$indexesDisplay.find('.indexes-list').append(htmlTemplateOutput);

                    if (this.indexes[model.get('index')]) { // If index is selected, set the checkbox to checked
                        htmlTemplateOutput.find('input').prop('checked', true);
                    }
                }, this);

                this.$('.list-indexes').popover({
                    html: true,
                    content: this.$indexesDisplay,
                    placement: 'bottom'
                });
            }, this);
        },

        render: function() {
            this.$el.html(this.indexesContain());
            this.$indexesDisplay = $(this.popover());
        },

        selectedIndexes: function() {
            return _.chain(this.indexes).map(function(value, key) {
                return (value ? key : undefined); // Return names of selected indexes and undefined for unselected ones
            }).compact().value();
        }
    });
});