define([
    'backbone',
    'jquery',
    'text!find/templates/app/page/search/filters/parametric/parametric-value-view.html'
], function(Backbone, $, template) {

    return Backbone.View.extend({
        className: 'parametric-value-element selectable-table-item clickable',
        tagName: 'tr',

        initialize: function() {
            this.$el.attr('data-value', this.model.id);
        },

        render: function() {
            this.$el.html(template);

            this.$text = this.$('.parametric-value-text');
            this.$name = this.$('.parametric-value-name');
            this.$count = this.$('.parametric-value-count');
            this.$check = this.$('.parametric-value-icon');

            this.updateCount();
            this.updateSelected();
        },

        updateCount: function() {
            $('.tooltip').remove();

            if (this.$text) {
                var name = this.model.id;
                var count = this.model.get('count');

                if (count !== null) {
                    this.$count.text(' (' + count + ')');
                } else {
                    this.$count.text('');
                }

                this.$name.text(name);

                this.$text.tooltip({
                    placement: 'top',
                    title: name,
                    container: 'body'
                });
            }


        },

        updateSelected: function() {
            if (this.$check) {
                this.$check.toggleClass('hide', !this.model.get('selected'));
            }
        }
    });

});
