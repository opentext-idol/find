define([
    'backbone',
    'underscore',
    'js-whatever/js/list-item-view',
    'iCheck'
], function(Backbone, _, ListItemView) {


    return ListItemView.extend({
        initialize: function() {
            ListItemView.prototype.initialize.apply(this, arguments);

            this.checked = [];
        },

        events: {
            'click .selectable-table-item': function(event) {
                var $targetRow = $(event.currentTarget);
                var id = $targetRow.find('[data-id]').data('id');
                this.trigger('change', $targetRow);

                $targetRow.find('i').toggleClass('hide');

                if($targetRow.find('i').hasClass('hide')) {
                    this.checked = _.without(this.checked, id);
                } else {
                    this.checked.push(id);
                    this.checked = _.uniq(this.checked);
                }

                this.trigger('changeFieldText');
            }
        },

        clear: function() {
            this.checked = [];
            this.trigger('changeFieldText');
        },

        render: function() {
            this.$checkboxInput && this.$checkboxInput.off();

            ListItemView.prototype.render.apply(this, arguments);

            this.$checkboxInput = this.$('input');

            _.each(this.checked, function(value) {
                this.$('[data-value="' + value + '"]').find('i').removeClass('hide')
            }, this);
        },

        getChecked: function() {
            return _.clone(this.checked);
        }

    });
});