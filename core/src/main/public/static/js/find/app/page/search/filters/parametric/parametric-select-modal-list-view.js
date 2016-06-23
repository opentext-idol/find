define([
    'backbone',
    'jquery',
    'underscore',
    'js-whatever/js/list-view',
    'find/app/page/search/filters/parametric/parametric-select-modal-item-view',
    'text!find/templates/app/page/search/filters/parametric/parametric-select-modal-view.html',
    'iCheck'
], function(Backbone, $, _, ListView, ItemView, template) {

    var increment = 30;

    function checkScroll() {
        var resultsPresent = this.fieldValues.size() > 0;

        if (resultsPresent && this.el.scrollTop + this.el.offsetHeight === this.el.scrollHeight) {
            this.shown += increment;
            this.filteredCollection.add(this.getModels());
        }
    }

    return Backbone.View.extend({
        template: _.template(template),
        className: 'full-height',
        shown: increment,

        events: {
            'ifClicked .parametric-field-label': function(e) {
                var $currentTarget = $(e.currentTarget);
                var fieldName = $currentTarget.data('field-id');
                var fieldValue = $currentTarget.find('.field-value').text();

                var parametricDisplayModel = this.parametricDisplayCollection.get(fieldName);

                // checked is the old value
                var selected = !$(e.target).prop('checked');

                this.selectCollection.add(_.defaults({
                    field: fieldName,
                    selected: selected,
                    value: fieldValue
                }, parametricDisplayModel.omit('id')), {merge: true});
            }
        },

        initialize: function(options) {
            this.parametricDisplayCollection = options.parametricDisplayCollection;
            this.selectCollection = options.selectCollection;
            this.fieldValues = options.field.fieldValues;
            this.filteredCollection = new Backbone.Collection();
            this.listView = new ListView({
                collection: this.filteredCollection,
                ItemView: ItemView,
                itemOptions: {
                    field: options.field
                }
            });
            this.checkScroll = checkScroll.bind(this);
            this.filteredCollection.add(this.getModels());
        },

        render: function() {
            this.$el.append(this.listView.render().$el);
            this.$el.scroll(this.checkScroll);

            return this;
        },

        getModels: function() {
            return this.fieldValues.filter(function(field, index) {
                return index < this.shown && index >= this.filteredCollection.length
            }, this);
        }
    });
});
