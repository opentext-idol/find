define([
    'backbone',
    'jquery',
    'underscore',
    'text!find/templates/app/page/search/filters/parametric/parametric-select-modal-view.html',
    'iCheck'
], function(Backbone, $, _, template) {

    var fieldTemplate = _.template('<div id="<%-field.id%>" class="tab-pane <%- currentFieldGroup === field.id ? \'active\' : \'\'%>" role="tabpanel"></div>');
    var valueTemplate = _.template('<div class="i-check checkbox parametric-field-label clickable shorten" data-field-id="<%-field.id%>"><label><input type="checkbox" <%-model.get(\'selected\') ? \'checked\' : \'\'%>> <span class="field-value"><%-model.id%></span> <% if(model.get(\'count\')) { %>(<%-model.get(\'count\')%>)<% } %></label></div>');

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

            var $fragment = $(document.createDocumentFragment());

            this.parametricDisplayCollection.each(function(field) {
                var $field = $(fieldTemplate({currentFieldGroup: this.currentFieldGroup, field: field}));

                _.each(field.fieldValues.models, function (model) {
                    var $value = $(valueTemplate({field: field, model: model}));
                    $field.append($value);
                    $value.iCheck({checkboxClass: 'icheckbox-hp'});
                });

                $fragment.append($field);
            }, this);

            this.$('.tab-content').html($fragment);
        }
    });
});
