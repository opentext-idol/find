/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'i18n!find/nls/bundle',
    'js-whatever/js/list-view',
    'find/app/util/collapsible',
    'find/app/page/search/filters/parametric/parametric-select-modal',
    'find/app/page/search/filters/parametric/parametric-value-view'
], function(Backbone, _, i18n, ListView, Collapsible, ParametricModal, ValueView) {
    'use strict';

    const MAX_SIZE = 5;

    const ValuesView = Backbone.View.extend({
        className: 'table parametric-fields-table',
        tagName: 'table',
        seeAllButtonTemplate: _.template('<tr class="show-all clickable"><td></td>' +
            '<td> <span class="toggle-more-text text-muted"><%-i18n["app.seeAll"]%></span></td></tr>'),

        events: {
            'click .show-all': function () {
                new ParametricModal({
                    currentFieldGroup: this.model.id,
                    queryModel: this.queryModel,
                    parametricFieldsCollection: this.parametricFieldsCollection,
                    selectedParametricValues: this.selectedParametricValues
                });
            }
        },

        initialize: function (options) {
            this.selectedParametricValues = options.selectedParametricValues;
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.queryModel = options.queryModel;

            this.listView = new ListView({
                collection: this.collection,
                footerHtml: this.seeAllButtonTemplate({i18n: i18n}),
                ItemView: ValueView,
                maxSize: MAX_SIZE,
                tagName: 'tbody',
                collectionChangeEvents: {
                    count: 'updateCount',
                    selected: 'updateSelected'
                }
            });
        },

        render: function () {
            this.$el.empty().append(this.listView.render().$el);
        },

        remove: function () {
            this.listView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

    return Backbone.View.extend({
        className: 'animated fadeIn',

        initialize: function(options) {
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.selectedParametricValues = options.selectedParametricValues;
            this.queryModel = options.queryModel;

            this.collapseModel = new Backbone.Model({
                collapsed: Boolean(_.isFunction(options.collapsed)
                    ? options.collapsed(options.model)
                    : options.collapsed)
            });

            this.collapsible = new Collapsible({
                collapseModel: this.collapseModel,
                subtitle: null,
                title: this.model.get('displayName') + ' (' + this.calculateSelectedCount() + ')',
                view: new ValuesView({
                    collection: this.model.fieldValues,
                    model: this.model,
                    parametricFieldsCollection: this.parametricFieldsCollection,
                    selectedParametricValues: this.selectedParametricValues
                })
            });

            this.listenTo(this.model.fieldValues, 'update change', function() {
                this.collapsible.setTitle(
                    this.model.get('displayName') + ' (' + this.calculateSelectedCount() + ')'
                );
            });

            this.listenTo(this.collapsible, 'toggle', function(newState) {
                this.trigger('toggle', this.model, newState)
            })
        },

        render: function() {
            this.$el
                .attr('data-field', this.model.id)
                .attr('data-field-display-name', this.model.get('displayName'))
                .empty()
                .append(this.collapsible.$el);

            this.collapsible.render();
        },

        calculateSelectedCount: function() {
            const selectedCount = this.getFieldSelectedValuesLength();
            return selectedCount
                ? selectedCount + ' / ' + this.model.fieldValues.length
                : this.model.fieldValues.length;
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
