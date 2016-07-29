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
    'use strict';

    var MAX_SIZE = 5;

    var ValuesView = Backbone.View.extend({
        className: 'table parametric-fields-table',
        tagName: 'table',
        seeAllButtonTemplate: _.template('<tr class="show-all clickable"><td></td><td> <span class="toggle-more-text text-muted"><%-i18n["app.seeAll"]%></span></td></tr>'),

        events: {
            'click .show-all': function() {
                new ParametricModal({
                    collection: this.model.fieldValues,
                    currentFieldGroup: this.model.id,
                    parametricCollection: this.parametricCollection,
                    parametricDisplayCollection: this.parametricDisplayCollection,
                    selectedParametricValues: this.selectedParametricValues
                });
            }
        },

        initialize: function(options) {
            this.parametricDisplayCollection = options.parametricDisplayCollection;
            this.selectedParametricValues = options.selectedParametricValues;
            this.parametricCollection = options.parametricCollection;

            this.listView = new ListView({
                collection: this.collection,
                footerHtml: this.seeAllButtonTemplate({i18n:i18n}),
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

        initialize: function(options) {
            this.$el.attr('data-field', this.model.id);
            this.$el.attr('data-field-display-name', this.model.get('displayName'));
            this.parametricDisplayCollection = options.parametricDisplayCollection;
            this.selectedParametricValues = options.selectedParametricValues;
            this.parametricCollection = options.parametricCollection;

            var collapsed;

            if (_.isFunction(options.collapsed)) {
                collapsed = options.collapsed(options.model);
            }
            else {
                collapsed = options.collapsed;
            }

            this.collapsible = new Collapsible({
                collapsed: collapsed,
                subtitle: null,
                title: this.model.get('displayName') + ' (' + this.calculateSelectedCount() + ')',
                view: new ValuesView({
                    collection: this.model.fieldValues,
                    model: this.model,
                    parametricCollection:this.parametricCollection,
                    parametricDisplayCollection: this.parametricDisplayCollection,
                    selectedParametricValues: this.selectedParametricValues
                })
            });

            this.listenTo(this.model.fieldValues, 'update change', function() {
                this.collapsible.setTitle(this.model.get('displayName') + ' (' + this.calculateSelectedCount() + ')');
            });

            this.listenTo(this.collapsible, 'toggle', function(newState) {
                this.trigger('toggle', this.model, newState)
            })
        },

        render: function() {
            this.$el.empty().append(this.collapsible.$el);
            this.collapsible.render();
        },

        calculateSelectedCount: function() {
            var selectedCount = this.getFieldSelectedValuesLength();
            return selectedCount ? selectedCount + ' / ' + this.model.fieldValues.length : this.model.fieldValues.length;
        },

        getFieldSelectedValuesLength: function() {
            return this.model.fieldValues.where({selected: true}).length;
        },

        remove: function() {
            this.collapsible.remove();

            Backbone.View.prototype.remove.call(this);
        }
    });
});