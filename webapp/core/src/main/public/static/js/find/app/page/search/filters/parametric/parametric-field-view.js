/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'underscore',
    'backbone',
    'i18n!find/nls/bundle',
    'js-whatever/js/list-view',
    'find/app/util/collapsible',
    'find/app/page/search/filters/parametric/parametric-select-modal',
    'find/app/page/search/filters/parametric/parametric-value-view',
    'text!find/templates/app/page/search/filters/parametric/parametric-field-footer.html',
    'text!find/templates/app/page/search/filters/parametric/parametric-field-title.html'
], function(_, Backbone, i18n, ListView, Collapsible, ParametricModal, ValueView, seeAllButtonTemplate, titleTemplate) {
    'use strict';

    const MAX_SIZE = 5;

    function mapSelectedValues(values) {
        return values.map(function(value) {
            return {id: value}
        });
    }

    const STATES = {
        PROCESSING: 'PROCESSING',
        ERROR: 'ERROR',
        SYNCED: 'SYNCED'
    };

    const ValuesView = Backbone.View.extend({
        className: 'table parametric-fields-table',
        tagName: 'table',

        events: {
            'click .show-all': function() {
                new ParametricModal({
                    initialField: this.model.id,
                    queryModel: this.queryModel,
                    parametricFieldsCollection: this.parametricFieldsCollection,
                    selectedParametricValues: this.selectedParametricValues,
                    indexesCollection: this.indexesCollection,
                    parametricValuesSort: this.parametricValuesSort
                });
            }
        },

        initialize: function(options) {
            this.selectedParametricValues = options.selectedParametricValues;
            this.indexesCollection = options.indexesCollection;
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.queryModel = options.queryModel;
            this.parametricValuesSort = options.parametricValuesSort;

            this.listView = new ListView({
                collection: this.collection,
                footerHtml: _.template(seeAllButtonTemplate)({i18n: i18n}),
                ItemView: ValueView,
                maxSize: MAX_SIZE,
                tagName: 'tbody',
                itemOptions: {
                    selectedValuesCollection: options.selectedValuesCollection,
                }
            });
        },

        render: function() {
            this.$el.html(this.listView.render().$el);
        },

        remove: function() {
            this.listView.remove();
            Backbone.View.prototype.remove.call(this);
        }
    });

    return Backbone.View.extend({
        className: 'animated fadeIn',

        initialize: function(options) {
            this.filteredParametricCollection = options.filteredParametricCollection;
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.selectedParametricValues = options.selectedParametricValues;
            this.indexesCollection = options.indexesCollection;
            this.queryModel = options.queryModel;

            this.initializeProcessingBehaviour();

            const shouldBeCollapsed = function shouldBeCollapsedFn() {
                return Boolean(_.isFunction(options.collapsed)
                    ? options.collapsed(options.model)
                    : options.collapsed);
            };
            this.collapseModel = new Backbone.Model({
                collapsed: shouldBeCollapsed()
            });

            this.selectedValuesCollection = new Backbone.Collection([]);

            this.parametricValuesCollection = new Backbone.Collection();

            this.parametricValuesSort = options.parametricValuesSort;

            this.collapsible = new Collapsible({
                collapseModel: this.collapseModel,
                subtitle: null,
                view: new ValuesView({
                    collection: this.parametricValuesCollection,
                    selectedValuesCollection: this.selectedValuesCollection,
                    model: this.model,
                    parametricFieldsCollection: this.parametricFieldsCollection,
                    queryModel: this.queryModel,
                    selectedParametricValues: this.selectedParametricValues,
                    indexesCollection: this.indexesCollection,
                    parametricValuesSort: this.parametricValuesSort
                })
            });

            this.listenTo(this.filteredParametricCollection, 'update change reset', this.onParametricChange);
            this.listenTo(this.selectedParametricValues, 'update change reset', this.onSelectedValueChange);
            this.listenTo(this.selectedValuesCollection, 'update change reset', this.updateTitle);

            this.listenTo(this.collapsible, 'toggle', function(newState) {
                this.collapseModel.set('collapsed', newState);
                this.trigger('toggle', this.model, newState);
            });

            if(options.filterModel) {
                this.listenTo(options.filterModel, 'change', function() {
                    const hasText = options.filterModel.get('text');
                    if(hasText) {
                        this.collapsible.show();
                    } else {
                        this.collapsible.toggle(!shouldBeCollapsed());
                    }
                    this.collapseModel.set('collapsed', !hasText);
                });
            }
        },

        render: function() {
            this.$el
                .attr('data-field', this.model.id)
                .attr('data-field-display-name', this.model.get('displayName'))
                .html(this.collapsible.$el);

            this.collapsible.render();
            this.$('.collapsible-title')
                .html(_.template(titleTemplate)({
                    displayName: this.model.get('displayName'),
                    i18n: i18n
                }));

            this.$valueCounts = this.$('.parametric-value-counts');
            this.$titleProcessing = this.$('.parametric-field-title-processing-indicator');

            this.onSelectedValueChange();
            this.onParametricChange();
            this.onStateChange();
        },

        initializeProcessingBehaviour: function() {
            this.stateModel = new Backbone.Model({
                state: this.filteredParametricCollection.isProcessing()
                    ? STATES.PROCESSING
                    : STATES.SYNCED
            });

            this.listenTo(this.stateModel, 'change:state', this.onStateChange);

            this.listenTo(this.filteredParametricCollection, 'request', function() {
                this.stateModel.set({state: STATES.PROCESSING});
            });

            this.listenTo(this.filteredParametricCollection, 'error', function(collection, xhr) {
                if(xhr.status !== 0) {
                    // The request was not aborted, so there isn't another request in flight
                    this.stateModel.set({state: STATES.ERROR});
                }
            });

            this.listenTo(this.filteredParametricCollection, 'sync', function() {
                this.stateModel.set({state: STATES.SYNCED});
            });
        },

        calculateSelectedCount: function() {
            const selectedCount = this.getFieldSelectedValuesLength();
            const parametricModel = this.filteredParametricCollection.get(this.model.id);
            const totalCount = parametricModel
                ? parametricModel.get('totalValues')
                : 0;
            return selectedCount
                ? selectedCount + ' / ' + totalCount
                : totalCount;
        },

        getFieldSelectedValuesLength: function() {
            return this.selectedValuesCollection.length;
        },

        remove: function() {
            this.collapsible.remove();
            Backbone.View.prototype.remove.call(this);
        },

        onParametricChange: function() {
            const parametricModel = this.filteredParametricCollection.get(this.model.id);
            this.parametricValuesCollection.reset(parametricModel
                ? parametricModel.get('values')
                : []);
        },

        onSelectedValueChange: function() {
            const selectedValues = this.selectedParametricValues.toFieldsAndValues()[this.model.id];
            this.selectedValuesCollection.reset(selectedValues
                ? mapSelectedValues(selectedValues.values)
                : []);
        },

        onStateChange: function() {
            const state = this.stateModel.get('state');

            this.$('.parametric-value-processing-indicator').toggleClass('hide', state !== STATES.PROCESSING);
            this.$('.parametric-value-error').toggleClass('hide', state !== STATES.ERROR);

            if(this.$titleProcessing) {
                this.$titleProcessing.toggleClass('hide', state !== STATES.PROCESSING);
            }

            if(this.$valueCounts) {
                this.updateTitle();
                this.$valueCounts.toggleClass('hide', state !== STATES.SYNCED);
            }
        },

        updateTitle: function() {
            if(this.stateModel.get('state') === STATES.SYNCED) {
                this.$valueCounts.text('(' + this.calculateSelectedCount() + ')');
            }
        },
    });
});
