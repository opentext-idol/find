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
            'ifClicked .filter-checkbox' : function(e) {
                var target = $(e.target);
                var value = $(target).closest('[data-value]').attr('data-value');

                if(target.prop('checked')) {
                    this.checked = _.without(this.checked, value);
                } else {
                    this.checked.push(value);
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

            this.$checkboxInput.iCheck({
                checkboxClass: 'icheckbox_square-red filter-checkbox'
            });

            // icheck won't add the necessary position: relative if the element isn't in the DOM...
            this.$('.filter-checkbox').css('position', 'relative');

            _.each(this.checked, function(value) {
                this.$('[data-value="' + value + '"]').find('input').iCheck('check')
            }, this);
        },

        getChecked: function() {
            return this.checked;
        }

    });
});