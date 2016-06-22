define([
    'js-whatever/js/list-item-view',
    'underscore',
    'text!find/templates/app/page/search/filters/parametric/parametric-select-modal-item-view.html',
    'iCheck'
], function(ListItemView, _, template) {

    return ListItemView.extend({
        template: _.template(template),
        
        initialize: function(options) {
            var field = options.field;

            ListItemView.prototype.initialize.call(this, _.defaults({
                template: this.template,
                templateOptions: {
                    field: field,
                    model: this.model                    
                }
            }, options));
        },

        render: function() {
            ListItemView.prototype.render.apply(this, arguments);

            this.$el.iCheck({checkboxClass: 'icheckbox-hp'})
        }
    });
});
