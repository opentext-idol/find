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
    'jquery',
    'backbone',
    'js-whatever/js/list-view',
    'find/app/metrics',
    'find/app/page/search/filters/parametric/parametric-field-view',
    'find/app/page/search/filters/parametric/proxy-view',
    'find/app/page/search/filters/parametric/numeric-parametric-field-collapsible-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/parametric-view.html'
], function(_, $, Backbone, ListView, metrics, FieldView, ProxyView, CollapsibleNumericFieldView,
            i18n, template) {
    'use strict';

    const TARGET_NUMBER_OF_PIXELS_PER_BUCKET = 10;

    const STATES = {
        PROCESSING: 'PROCESSING',
        ERROR: 'ERROR',
        SYNCED: 'SYNCED'
    };

    return Backbone.View.extend({
        template: _.template(template)({i18n: i18n}),

        events: {
            'click [data-field] [data-value]': function(e) {
                if (String(window.getSelection()).length >= 2) {
                    // If the user is partway selecting text for selection-entity-search, we suppress the click,
                    //   otherwise the preview pane will toggle every time you try and select something.
                    return;
                }

                const $target = $(e.currentTarget);
                const $field = $target.closest('[data-field]');

                const attributes = {
                    field: $field.attr('data-field'),
                    displayName: $field.attr('data-field-display-name'),
                    value: $target.attr('data-value'),
                    displayValue: $target.attr('data-display-value'),
                    type: 'Parametric'
                };

                if(this.selectedParametricValues.get(attributes)) {
                    this.selectedParametricValues.remove(attributes);
                } else {
                    this.selectedParametricValues.add(attributes);
                }
            }
        },

        initialize: function(options) {
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.filteredParametricCollection = options.filteredParametricCollection;
            this.selectedParametricValues = options.queryState.selectedParametricValues;
            this.filterModel = options.filterModel;

            this.initializeProcessingBehaviour();

            const collapsed = {};

            const isCollapsed = function(model) {
                if(this.filterModel && this.filterModel.get('text')) {
                    return false;
                } else {
                    return _.isUndefined(collapsed[model.id]) || collapsed[model.id];
                }
            }.bind(this);

            this.fieldNamesListView = new ListView({
                className: 'parametric-fields-list',
                collection: this.collection,
                proxyEvents: ['toggle'],
                collectionChangeEvents: false,
                ItemView: ProxyView,
                itemOptions: {
                    typeAttribute: 'type',
                    viewTypes: {
                        NumericDate: {
                            Constructor: CollapsibleNumericFieldView,
                            options: 'numericViewItemOptions'
                        },
                        Numeric: {
                            Constructor: CollapsibleNumericFieldView,
                            options: 'numericViewItemOptions'
                        },
                        Parametric: {
                            Constructor: FieldView,
                            options: 'parametricViewItemOptions'
                        }
                    },
                    parametricViewItemOptions: {
                        collapsed: isCollapsed,
                        queryModel: options.queryModel,
                        indexesCollection: options.indexesCollection,
                        parametricFieldsCollection: options.parametricFieldsCollection,
                        filteredParametricCollection: this.filteredParametricCollection,
                        selectedParametricValues: this.selectedParametricValues,
                        filterModel: this.filterModel,
                        parametricValuesSort: options.parametricValuesSort
                    },
                    numericViewItemOptions: {
                        inputTemplate: options.inputTemplate,
                        queryModel: options.queryModel,
                        filterModel: options.filterModel,
                        timeBarModel: options.timeBarModel,
                        selectedParametricValues: this.selectedParametricValues,
                        pixelsPerBucket: TARGET_NUMBER_OF_PIXELS_PER_BUCKET,
                        formatting: options.formatting,
                        selectionEnabled: options.selectionEnabled,
                        zoomEnabled: options.zoomEnabled,
                        buttonsEnabled: options.buttonsEnabled,
                        coordinatesEnabled: options.coordinatesEnabled,
                        collapsed: isCollapsed
                    }
                }
            });

            // Would ideally use model.cid but on refresh the display Collection creates new models with different cids
            this.listenTo(this.fieldNamesListView, 'item:toggle', function(model, newState) {
                collapsed[model.id] = newState;
            });
        },

        render: function() {
            this.$el.html(this.template).prepend(this.fieldNamesListView.$el);
            this.fieldNamesListView.render();

            this.$emptyMessage = this.$('.parametric-fields-empty');
            this.$list = this.$('.parametric-fields-list');
            this.$errorMessage = this.$('.parametric-fields-error');
            this.$processing = this.$('.parametric-fields-processing-indicator');

            this.onStateChange();
            return this;
        },

        remove: function() {
            this.fieldNamesListView.remove();
            Backbone.View.prototype.remove.call(this);
        },

        initializeProcessingBehaviour: function() {
            this.model = new Backbone.Model({
                state: this.collection.isProcessing() ? STATES.PROCESSING : STATES.SYNCED,
                empty: this.parametricFieldsCollection.isEmpty()
            });

            this.listenTo(this.model, 'change:state', this.onStateChange);
            this.listenTo(this.model, 'change', this.updateEmpty);

            this.listenTo(this.collection, 'request', function() {
                this.model.set('state', STATES.PROCESSING);
            });

            this.listenTo(this.collection, 'error', function(collection, xhr) {
                if(xhr.status !== 0) {
                    // The request was not aborted, so there isn't another request in flight
                    this.model.set('state', STATES.ERROR);
                }
            });

            this.listenTo(this.collection, 'sync', function() {
                this.model.set('state', STATES.SYNCED);
            });

            this.listenTo(this.parametricFieldsCollection, 'update reset', function() {
                this.model.set('empty', this.parametricFieldsCollection.isEmpty());
            });
        },

        updateEmpty: function() {
            if(this.$emptyMessage) {
                const showEmptyMessage = this.model.get('empty') &&
                    this.parametricFieldsCollection.isEmpty() &&
                    this.model.get('state') === STATES.SYNCED;
                this.$emptyMessage.toggleClass('hide', !showEmptyMessage);
            }
        },

        onStateChange: function() {
            const state = this.model.get('state');
            if(this.$processing) {
                this.$processing.toggleClass('hide', state !== STATES.PROCESSING);
            }

            if(this.$errorMessage) {
                this.$errorMessage.toggleClass('hide', state !== STATES.ERROR);
            }

            if(this.$list) {
                this.$list.toggleClass('hide', state !== STATES.SYNCED);
            }
        },
    });
});
