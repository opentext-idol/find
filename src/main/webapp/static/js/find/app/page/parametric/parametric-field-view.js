define([
    'backbone',
    'underscore',
    'jquery',
    'js-whatever/js/list-view',
    'find/app/util/collapsible',
    'find/app/page/parametric/parametric-value-view'
], function(Backbone, _, $, ListView, Collapsible, ValueView) {

    var ValuesView = Backbone.View.extend({
        className: 'table',
        tagName: 'table',

        initialize: function() {
            this.listView = new ListView({
                collection: this.collection,
                tagName: 'tbody',
                ItemView: ValueView,
                collectionChangeEvents: {
                    count: 'updateCount',
                    selected: 'updateSelected'
                }
            });
        },

        render: function() {
            this.$el.empty().append(this.listView.render().$el);
        },

        remove: function() {
            this.listView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

    return Backbone.View.extend({
        className: 'animated fadeIn',

        initialize: function() {
            this.$el.attr('data-field', this.model.id);

            this.collapsible = new Collapsible({
                title: this.model.get('displayName'),
                view: new ValuesView({collection: this.model.fieldValues}),
                collapsed: false
            });
        },

        render: function() {
            this.$el.empty().append(this.collapsible.$el);
            this.collapsible.render();
        },

        remove: function() {
            Backbone.View.prototype.remove.call(this);
            this.collapsible.remove();
        }
    });
});