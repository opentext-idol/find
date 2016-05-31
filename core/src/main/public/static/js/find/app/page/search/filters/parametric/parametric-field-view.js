define([
    'backbone',
    'underscore',
    'jquery',
    'i18n!find/nls/bundle',
    'js-whatever/js/list-view',
    'find/app/util/collapsible',
    'find/app/page/search/filters/parametric/parametric-select-modal',
    'find/app/page/search/filters/parametric/parametric-value-view'
], function(Backbone, _, $, i18n, ListView, Collapsible, ParametricModal, ValueView) {

    var ValuesView = Backbone.View.extend({
        className: 'table parametric-fields-table',
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
        seeAllButtonTemplate: _.template('<tr class="show-all clickable"><td></td><td> <span class="toggle-more-text text-muted"><%-i18n["app.seeAll"]%></span></td></tr>'),

        events: {
            'click .show-all': function(e) {
                new ParametricModal({
                    collection: this.model.fieldValues,
                    parametricDisplayCollection: this.parametricDisplayCollection,
                    selectedParametricValues: this.selectedParametricValues,
                    currentFieldGroup: this.model.id
                });
            }
        },

        initialize: function(options) {
            this.$el.attr('data-field', this.model.id);
            this.$el.attr('data-field-display-name', this.model.get('displayName'));
            this.parametricDisplayCollection = options.parametricDisplayCollection;
            this.selectedParametricValues = options.selectedParametricValues;

            this.collapsible = new Collapsible({
                title: this.model.get('displayName') + ' (' + this.model.fieldValues.length +')',
                subtitle: this.getFieldSelectedValuesLength() + ' ' + i18n['app.selected'],
                view: new ValuesView({collection: this.model.fieldValues}),
                collapsed: false
            });

            this.listenTo(this.selectedParametricValues, 'update', function() {
                this.collapsible.$('.collapsible-subtitle').text(this.getFieldSelectedValuesLength() + ' ' + i18n['app.selected'])
            })
        },

        render: function() {
            this.$el.empty().append(this.collapsible.$el);
            this.collapsible.render();

            if(this.collapsible.$('tbody tr').length > 5) {
                this.toggleFacets(true);
            }
            this.collapsible.$('tbody').append(this.seeAllButtonTemplate({i18n:i18n}));
        },

        getFieldSelectedValuesLength: function() {
            return this.selectedParametricValues.where({field: this.model.id}).length;
        },

        toggleFacets: function(toggle) {
            var lastFacets = this.collapsible.$('tbody tr').slice(5);
            lastFacets.toggleClass('hide', toggle);

            this.$('.toggle-more').removeClass('hide');
        },

        remove: function() {
            Backbone.View.prototype.remove.call(this);
            this.collapsible.remove();
        }
    });
});