define([
    'backbone',
    'underscore',
    'find/app/util/collapsible',
    'js-whatever/js/list-item-view',
    'text!find/templates/app/page/parametric/parametric-list-item-view.html',
    'iCheck'
], function(Backbone, _, Collapsible, ListItemView, template) {

    var ValuesView = Backbone.View.extend({
        className: 'table',
        tagName: 'table',
        template: _.template(template, {variable: 'data'}),

        render: function() {
            this.$el.html(this.template({values: this.model.get('values')}));
        }
    });

    return Backbone.View.extend({
        className: 'animated fadeIn',
        setDataIdAttribute: ListItemView.prototype.setDataIdAttribute,

        initialize: function() {
            this.setDataIdAttribute();
            this.checked = [];

            // Parametric collection ID attribute is the name so we don't expect it or the displayName to change
            this.collapsible = new Collapsible({
                title: this.model.get('displayName'),
                view: new ValuesView({model: this.model}),
                collapsed: false
            });
        },

        events: {
            'click .selectable-table-item': function(event) {
                var $targetRow = $(event.currentTarget);
                var id = $targetRow.find('[data-id]').data('id');
                this.trigger('change', $targetRow);

                $targetRow.find('i').toggleClass('hide');

                if ($targetRow.find('i').hasClass('hide')) {
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
            this.$el.empty().append(this.collapsible.$el);
            this.collapsible.render();
            this.setDataIdAttribute();

            this.$checkboxInput && this.$checkboxInput.off();
            this.$checkboxInput = this.$('input');

            _.each(this.checked, function(value) {
                this.$('[data-value="' + value + '"]').find('i').removeClass('hide');
            }, this);
        },

        getChecked: function() {
            return _.clone(this.checked);
        },

        remove: function() {
            Backbone.View.prototype.remove.call(this);
            this.collapsible.remove();
        }
    });
});