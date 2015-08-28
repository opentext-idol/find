define([
    'backbone',
    'text!find/templates/app/util/collapsible.html'
], function(Backbone, collapsibleTemplate) {

    return Backbone.View.extend({
        template: _.template(collapsibleTemplate, {variable: 'data'}),

        events: {
            'show.bs.collapse .collapsible-header': function() {
                this.collapsed = false;
                this.updateHeaderState();
            },
            'hide.bs.collapse .collapsible-header': function() {
                this.collapsed = true;
                this.updateHeaderState();
            }
        },

        initialize: function(options) {
            this.view = options.view;
            this.collapsed = options.collapsed || false;
            this.title = options.title;
        },

        render: function() {
            this.$el.html(this.template({
                contentState: this.collapsed ? '' : 'in',
                title: this.title
            }));

            this.$header = this.$('.collapsible-header');
            this.updateHeaderState();

            this.view.render();
            this.$('.collapse').append(this.view.$el);
        },

        remove: function() {
            this.view.remove();
            Backbone.View.prototype.remove.call(this);
        },

        updateHeaderState: function() {
            // The "collapsed" class controls the icons with class "rotating-chevron"
            this.$header.toggleClass('collapsed', this.collapsed);
        }
    });
});
