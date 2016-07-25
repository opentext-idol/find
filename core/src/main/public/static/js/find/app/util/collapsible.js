define([
    'backbone',
    'text!find/templates/app/util/collapsible.html',
    'bootstrap'
], function(Backbone, collapsibleTemplate) {
    'use strict';

    return Backbone.View.extend({
        template: _.template(collapsibleTemplate, {variable: 'data'}),

        events: {
            'click > .collapsible-header': function() {
                this.$collapse.collapse('toggle');

                // other handlers called before this trigger
                this.trigger('toggle', this.collapsed);
            },
            'show.bs.collapse': function() {
                this.collapsed = false;
                this.updateHeaderState();

                this.trigger('show');
            },
            'shown.bs.collapse': function() {
                if (this.renderOnOpen) {
                    this.view.render();
                }

                this.trigger('shown');
            },
            'hide.bs.collapse': function() {
                this.collapsed = true;
                this.updateHeaderState();

                this.trigger('hide');
            }
        },

        initialize: function(options) {
            this.view = options.view;
            this.collapsed = options.collapsed || false;
            this.title = options.title;
            this.subtitle = options.subtitle;
            this.renderOnOpen = options.renderOnOpen || false;

            this.collapseId = _.uniqueId('collapse-');
        },

        render: function() {
            this.$el.html(this.template({
                contentState: this.collapsed ? '' : 'in',
                collapseId: this.collapseId,
                title: this.title,
                subtitle: this.subtitle
            }));

            this.$header = this.$('.collapsible-header');
            this.updateHeaderState();

            // activate plugin manually for greater control of click handlers
            this.$collapse = this.$('.collapse').collapse({
                toggle: false
            });

            // Render after appending to the DOM since graph views must measure element dimensions
            this.$collapse.append(this.view.$el);
            this.view.delegateEvents().render();
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
            this.$('.collapsible-subtitle').text(subtitle);
        },

        toggleSubtitle: function(toggle) {
            this.$('.collapsible-subtitle').toggleClass('hide', !toggle)
        },

        show: function() {
            if (this.collapsed) {
                this.$collapse.collapse('show');
            }
        },

        hide: function() {
            if (!this.collapsed) {
                this.$collapse.collapse('hide');
            }
        },

        toggle: function(state) {
            if (state) {
                this.show();
            } else {
                this.hide();
            }
        }
    });
});
