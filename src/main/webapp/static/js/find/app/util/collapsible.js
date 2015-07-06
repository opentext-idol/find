define([
    'backbone',
    'text!find/templates/app/util/collapsible.html'
], function(Backbone, collapsibleTemplate) {

    return Backbone.View.extend({

        template: _.template(collapsibleTemplate, null, {variable: 'data'}),

        initialize: function(options) {
            this.name = options.name;
            this.header = options.header;
            this.view = options.view;
            this.collapsed = options.collapsed;
        },

        render: function() {
            var headerState, contentState;

            if(this.collapsed) {
                headerState = 'collapsed';
                contentState = '';
            } else {
                headerState = '';
                contentState = 'in';
            }

            this.$el.html(this.template({
                header: this.header,
                name: this.name,
                headerState: headerState,
                contentState: contentState
            }));

            this.view.render();
            this.$('.collapse').append(this.view.$el);
        }

    });
});