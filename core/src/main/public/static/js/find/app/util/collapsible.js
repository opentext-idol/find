define([
    'backbone',
    'text!find/templates/app/util/collapsible.html',
    'bootstrap'
], function(Backbone, collapsibleTemplate) {

    return Backbone.View.extend({
        template: _.template(collapsibleTemplate, {variable: 'data'}),

        events: {
            'show.bs.collapse': function() {
                this.collapsed = false;
                this.updateHeaderState();
                if (this.renderOnOpen) {
                    this.view.render();
                }
            },
            'hide.bs.collapse': function() {
                this.collapsed = true;
                this.updateHeaderState();
            }
        },

        initialize: function(options) {
            this.view = options.view;
            this.collapsed = options.collapsed || false;
            this.title = options.title;
            this.subtitle = options.subtitle;
            this.renderOnOpen = options.renderOnOpen || false;
        },

        render: function() {
            this.$el.html(this.template({
                contentState: this.collapsed ? '' : 'in',
                title: this.title,
                subtitle: this.subtitle
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
        },
        
        setSubTitle: function(subtitle) {
            this.subtitle = subtitle;
            this.$('.collapsible-subtitle').text(subtitle).removeClass('hide');
        }
    });
});
