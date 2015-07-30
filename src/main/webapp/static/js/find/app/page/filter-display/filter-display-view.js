define([
    'backbone',
    'js-whatever/js/list-view',
    'find/app/model/search-filters-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/filter-display/filter-display.html',
    'text!find/templates/app/page/filter-display/filter-display-item.html'
], function(Backbone, ListView, SearchFiltersCollection, i18n, template, itemTemplate) {

    // Each of the collection's models should have an id and a text attribute
    return Backbone.View.extend({
        template: _.template(template),
        itemTemplate: _.template(itemTemplate),

        events: {
            'click .filters-remove-icon': function(e) {
                var id = $(e.currentTarget).closest('[data-id]').attr('data-id');
                var metatype = $(e.currentTarget).closest('[data-metatype]').attr('data-metatype');
                var type = $(e.currentTarget).closest('[data-type]').attr('data-type');

                if(metatype && metatype === SearchFiltersCollection.metaFilterTypes.date) {
                    if(type === SearchFiltersCollection.FilterTypes.dateRange) {
                        this.datesFilterModel.setDateRange(null);
                    }
                    if(type === SearchFiltersCollection.FilterTypes.minDate) {
                        this.datesFilterModel.setMinDate(null);
                    }
                    if(type === SearchFiltersCollection.FilterTypes.maxDate) {
                        this.datesFilterModel.setMaxDate(null);
                    }
                } else {
                    this.collection.remove(id);
                }


            }
        },

        initialize: function(options) {
            this.datesFilterModel = options.datesFilterModel;

            this.listView = new ListView({
                collection: this.collection,
                itemOptions: {
                    className: 'label filter-label border filters-margin inline-block',
                    template: this.itemTemplate
                }
            });

            this.listenTo(this.collection, 'reset update', this.updateVisibility);
        },

        render: function() {
            this.$el.html(this.template({
                i18n: i18n
            }));

            this.updateVisibility();

            this.listView.render();
            this.$el.append(this.listView.$el);

            return this;
        },

        updateVisibility: function() {
            this.$el.toggleClass('hide', this.collection.isEmpty());
        }
    });

});