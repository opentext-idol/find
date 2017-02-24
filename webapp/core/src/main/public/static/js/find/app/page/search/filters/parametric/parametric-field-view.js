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
], function (Backbone, _, i18n, ListView, Collapsible, ParametricModal, ValueView) {
    'use strict';

    const MAX_SIZE = 5;

    function mapSelectedValues(values) {
        return values.map(function (value) {
            return {id: value}
        });
    }

    const ValuesView = Backbone.View.extend({
        className: 'table parametric-fields-table',
        tagName: 'table',
        seeAllButtonTemplate: _.template('<tr class="show-all clickable"><td></td>' +
            '<td> <span class="toggle-more-text text-muted"><%-i18n["app.seeAll"]%></span></td></tr>'),

        events: {
            'click .show-all': function () {
                new ParametricModal({
                    initialField: this.model.id,
                    queryModel: this.queryModel,
                    parametricFieldsCollection: this.parametricFieldsCollection,
                    selectedParametricValues: this.selectedParametricValues,
                    indexesCollection: this.indexesCollection
                });
            }
        },

        initialize: function (options) {
            this.selectedParametricValues = options.selectedParametricValues;
            this.indexesCollection = options.indexesCollection;
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.queryModel = options.queryModel;

            this.listView = new ListView({
                collection: this.collection,
                footerHtml: this.seeAllButtonTemplate({i18n: i18n}),
                ItemView: ValueView,
                maxSize: MAX_SIZE,
                tagName: 'tbody',
                itemOptions: {
                    selectedValuesCollection: options.selectedValuesCollection,
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

        initialize: function (options) {
            this.parametricCollection = options.parametricCollection;
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.selectedParametricValues = options.selectedParametricValues;
            this.indexesCollection = options.indexesCollection;
            this.queryModel = options.queryModel;

            this.collapseModel = new Backbone.Model({
                collapsed: Boolean(_.isFunction(options.collapsed)
                    ? options.collapsed(options.model)
                    : options.collapsed)
            });

            this.selectedValuesCollection = new Backbone.Collection([]);

            this.parametricValuesCollection = new Backbone.Collection();
            this.collapsible = new Collapsible({
                collapseModel: this.collapseModel,
                subtitle: null,
                title: this.model.get('displayName') + ' (' + this.calculateSelectedCount() + ')',
                view: new ValuesView({
                    collection: this.parametricValuesCollection,
                    selectedValuesCollection: this.selectedValuesCollection,
                    model: this.model,
                    parametricFieldsCollection: this.parametricFieldsCollection,
                    queryModel: this.queryModel,
                    selectedParametricValues: this.selectedParametricValues,
                    indexesCollection: this.indexesCollection
                })
            });

            this.listenTo(this.parametricCollection, 'update change reset', this.onParametricChange);
            this.listenTo(this.selectedParametricValues, 'update change reset', this.onSelectedValueChange);
            this.listenTo(this.selectedValuesCollection, 'update change reset', this.updateTitle);

            this.listenTo(this.collapsible, 'toggle', function (newState) {
                this.trigger('toggle', this.model, newState)
            });
        },

        render: function () {
            this.$el
                .attr('data-field', this.model.id)
                .attr('data-field-display-name', this.model.get('displayName'))
                .empty()
                .append(this.collapsible.$el);

            this.collapsible.render();

            this.onSelectedValueChange();
            this.onParametricChange();
        },

        updateTitle: function () {
            this.collapsible.setTitle(
                this.model.get('displayName') + ' (' + this.calculateSelectedCount() + ')'
            );
        },

        calculateSelectedCount: function () {
            const selectedCount = this.getFieldSelectedValuesLength();
            const parametricModel = this.parametricCollection.get(this.model.id);
            const totalCount = parametricModel ? parametricModel.get('totalValues') : 0;
            return selectedCount
                ? selectedCount + ' / ' + totalCount
                : totalCount;
        },

        getFieldSelectedValuesLength: function () {
            return this.selectedValuesCollection.length;
        },

        remove: function () {
            this.collapsible.remove();
            Backbone.View.prototype.remove.call(this);
        },

        onParametricChange: function () {
            const parametricModel = this.parametricCollection.get(this.model.id);
            const parametricValues = parametricModel ? parametricModel.get('values') : [];
            this.parametricValuesCollection.reset(parametricValues);
            this.updateTitle();
        },

        onSelectedValueChange: function () {
            const selectedValues = this.selectedParametricValues.toFieldsAndValues()[this.model.id];
            this.selectedValuesCollection.reset(selectedValues ? mapSelectedValues(selectedValues.values) : []);
        }
    });
});
