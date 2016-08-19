define([
    'backbone',
    'underscore',
    'text!find/templates/app/util/text-input.html'
], function(Backbone, _, template) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'input input': function() {
                this.model.set(this.modelAttribute, this.$input.val());
            },
            'submit': function(e) {
                e.preventDefault();
            }
        },

        initialize: function(options) {
            this.templateOptions = options.templateOptions;
            this.modelAttribute = options.modelAttribute;
        },

        render: function() {
            this.$el.html(this.template(this.templateOptions));
            this.$input = this.$('.text-input');
        }
    });
});
