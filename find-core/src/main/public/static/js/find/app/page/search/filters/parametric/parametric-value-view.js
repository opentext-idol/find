define([
    'backbone',
    'text!find/templates/app/page/search/filters/parametric/parametric-value-view.html'
], function(Backbone, template) {

    return Backbone.View.extend({
        className: 'parametric-value-element selectable-table-item clickable',
        tagName: 'tr',

        initialize: function() {
            this.$el.attr('data-value', this.model.id);
        },

        render: function() {
            this.$el.html(template);

            this.$text = this.$('.parametric-value-text');
            this.$check = this.$('.parametric-value-icon');

            this.updateCount();
            this.updateSelected();
        },

        updateCount: function() {
            if (this.$text) {
                var text = this.model.id;
                var count = this.model.get('count');

                if (count !== null) {
                    text += ' (' + count + ')';
                }

                this.$text.text(text);
            }
        },

        updateSelected: function() {
            if (this.$check) {
                this.$check.toggleClass('hide', !this.model.get('selected'));
            }
        }
    });

});
