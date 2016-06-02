define([
    'backbone',
    'jquery',
    'underscore',
    'text!find/templates/app/page/search/filters/parametric/parametric-select-modal-view.html',
    'iCheck'
], function(Backbone, $, _, template) {

    return Backbone.View.extend({
        template: _.template(template),
        className: 'full-height',

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
            this.currentFieldGroup = options.currentFieldGroup;
        },

        render: function() {
            this.$el.html(this.template({
                parametricDisplayCollection: this.parametricDisplayCollection,
                currentFieldGroup: this.currentFieldGroup
            }));

            this.$('.i-check').iCheck({
                checkboxClass: 'icheckbox-hp'
            });
        }
    });
});
