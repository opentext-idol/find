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

    var MAX_SIZE = 5;

    var ValuesView = Backbone.View.extend({
        className: 'table parametric-fields-table',
        tagName: 'table',

        initialize: function() {
            this.listView = new ListView({
                collection: this.collection,
                ItemView: ValueView,
                maxSize: MAX_SIZE,
                tagName: 'tbody',
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
        subtitleTemplate: _.template('<span class="selection-length"><%-length%></span> <%-i18n["app.selected"]%> <i class="hp-icon hp-warning selected-warning hidden"></i>'),

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
                view: new ValuesView({collection: this.model.fieldValues}),
                collapsed: true,
                subtitle: this.subtitleTemplate({
                    i18n: i18n,
                    length: this.getFieldSelectedValuesLength()
                })
            });

            this.listenTo(this.selectedParametricValues, 'update', function() {
                this.collapsible.$('.selection-length').text(this.getFieldSelectedValuesLength());
                this.toggleWarning();
            })
        },

        render: function() {
            this.$el.empty().append(this.collapsible.$el);
            this.collapsible.render();

            this.collapsible.$('tbody').append(this.seeAllButtonTemplate({i18n:i18n}));

            this.$warning = this.collapsible.$('.selected-warning');

            this.$warning.tooltip({
                title: i18n['search.parametric.selected.notAllVisible']
            });

            this.toggleWarning();
        },

        toggleWarning: function() {
            var currentValues = this.selectedParametricValues.where({field: this.model.id});
            var toggle = true;

            if(currentValues.length > 0) {
                var firstFiveValues = this.model.fieldValues.chain()
                    .first(MAX_SIZE)
                    .pluck('id')
                    .value();

                var fieldsArray = _.invoke(currentValues, 'get', 'value');

                toggle = !_.difference(fieldsArray, firstFiveValues).length;
            }

            this.$warning.toggleClass('hidden', toggle);
        },

        getFieldSelectedValuesLength: function() {
            return this.selectedParametricValues.where({field: this.model.id}).length;
        },

        remove: function() {
            Backbone.View.prototype.remove.call(this);
            this.collapsible.remove();
        }
    });
});