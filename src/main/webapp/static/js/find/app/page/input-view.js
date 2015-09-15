define([
    'backbone',
    'jquery',
    'underscore',
    'text!find/templates/app/page/input-view.html'
], function(Backbone, $, _, template) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'keyup .find-input': _.debounce(function() {
                var findInput = this.$('.find-input').val();
                this.queryModel.refresh(findInput);
            }, 500),
            'submit .find-form': function(e) {
                e.preventDefault();
            }
        },

        initialize: function(options) {
            this.queryModel = options.queryModel;
        },

        render: function() {
            this.$el.html(this.template);
        }
    })
});
