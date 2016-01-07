define([
    'backbone',
    'jquery',
    'underscore',
    'text!find/templates/app/page/search/input-view.html'
], function(Backbone, $, _, template) {

    return Backbone.View.extend({
        template: _.template(template),

        events: {
            'keyup .find-input': _.debounce(function(e) {
                var findInput = this.$('.find-input').val();

                // Keycode 13 is Enter, so where the user wants to refresh the search
                // we call refresh. In all other cases, if the chain of keyup events
                // did not result in the query text changing we are protected from
                // rerunning the search by backbone.
                if (e.which === 13) {
                    this.queryModel.refresh(findInput);
                } else {
                    this.queryModel.set('queryText', findInput);
                }
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
