/*
 * Copyright 2016 Hewlett-Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'jquery',
    'underscore',
    'js-whatever/js/list-view',
    'find/app/metrics',
    'find/app/page/search/filters/parametric/parametric-field-view',
    'find/app/page/search/filters/parametric/proxy-view',
    'find/app/page/search/filters/parametric/numeric-parametric-field-collapsible-view',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/parametric-view.html'
], function(Backbone, $, _, ListView, metrics, FieldView, ProxyView, CollapsibleNumericFieldView, i18n, template) {
    'use strict';

    const TARGET_NUMBER_OF_PIXELS_PER_BUCKET = 10;

    return Backbone.View.extend({
        template: _.template(template)({i18n: i18n}),

        events: {
            'click [data-field] [data-value]': function (e) {
                const $target = $(e.currentTarget);
                const $field = $target.closest('[data-field]');

                const attributes = {
                    field: $field.attr('data-field'),
                    value: $target.attr('data-value')
                };

                if (this.selectedParametricValues.get(attributes)) {
                    this.selectedParametricValues.remove(attributes);
                } else {
                    this.selectedParametricValues.add(attributes);
                }
            }
        },

        initialize: function (options) {
            this.parametricFieldsCollection = options.parametricFieldsCollection;
            this.parametricCollection = options.parametricCollection;
            this.selectedParametricValues = options.queryState.selectedParametricValues;
            this.displayCollection = options.displayCollection;
            this.filterModel = options.filterModel;

            //ToDo : We are currently only monitoring parametricCollection for loading and error. Need to fix as part of FIND-618.
            this.model = new Backbone.Model({
                processing: Boolean(this.parametricCollection.currentRequest),
                error: false,
                empty: this.collection.isEmpty()
            });

            this.listenTo(this.model, 'change:processing', this.updateProcessing);
            this.listenTo(this.model, 'change:error', this.updateError);
            this.listenTo(this.model, 'change', this.updateEmpty);

            this.listenTo(this.parametricCollection, 'request', function() {
                this.model.set({processing: true, error: false});
            });

            this.listenTo(this.parametricCollection, 'error', function(collection, xhr) {
                if (xhr.status === 0) {
                    this.model.set({processing: Boolean(this.parametricCollection.currentRequest)});
                } else {
                    // The request was not aborted, so there isn't another request in flight
                    this.model.set({error: true, processing: false});
                }
            });

            this.listenTo(this.parametricCollection, 'sync', function() {
                this.model.set({processing: false});

                if (!this.parametricCollection.isEmpty() && !this.parametricValuesLoaded) {
                    this.parametricValuesLoaded = true;
                    metrics.addTimeSincePageLoad('parametric-values-first-loaded');
                }
            });

            this.listenTo(this.collection, 'update reset', function() {
                this.model.set('empty', this.collection.isEmpty());
            });

            const collapsed = {};

            const isCollapsed = function (model) {
                if (this.filterModel && this.filterModel.get('text')) {
                    return false;
                } else {
                    return _.isUndefined(collapsed[model.id]) || collapsed[model.id];
                }
            }.bind(this);

            this.fieldNamesListView = new ListView({
                collection: this.collection,
                proxyEvents: ['toggle'],
                collectionChangeEvents: false,
                ItemView: ProxyView,
                itemOptions: {
                    typeAttribute: 'dataType',
                    viewTypes: {
                        date: {
                            Constructor: CollapsibleNumericFieldView,
                            options: 'numericViewItemOptions'
                        },
                        numeric: {
                            Constructor: CollapsibleNumericFieldView,
                            options: 'numericViewItemOptions'
                        },
                        parametric: {
                            Constructor: FieldView,
                            options: 'parametricViewItemOptions'
                        }
                    },
                    parametricViewItemOptions: {
                        collapsed: isCollapsed,
                        queryModel: options.queryModel,
                        indexesCollection: options.indexesCollection,
                        parametricFieldsCollection: this.parametricFieldsCollection,
                        selectedParametricValues: this.selectedParametricValues
                    },
                    numericViewItemOptions: {
                        inputTemplate: options.inputTemplate,
                        queryModel: options.queryModel,
                        filterModel: options.filterModel,
                        timeBarModel: options.timeBarModel,
                        selectedParametricValues: this.selectedParametricValues,
                        pixelsPerBucket: TARGET_NUMBER_OF_PIXELS_PER_BUCKET,
                        numericRestriction: options.numericRestriction,
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
            this.listenTo(this.fieldNamesListView, 'item:toggle', function (model, newState) {
                collapsed[model.id] = newState;
            });
        },

        render: function() {
            this.$el.html(this.template).prepend(this.fieldNamesListView.$el);
            this.fieldNamesListView.render();

            this.$emptyMessage = this.$('.parametric-empty');
            this.$errorMessage = this.$('.parametric-error');
            this.$processing = this.$('.parametric-processing-indicator');

            this.updateProcessing();
            return this;
        },

        remove: function () {
            this.fieldNamesListView.remove();
            this.displayCollection.stopListening();
            Backbone.View.prototype.remove.call(this);
        },

        updateEmpty: function () {
            if (this.$emptyMessage) {
                const showEmptyMessage = this.model.get('empty') && this.collection.isEmpty() && !(this.model.get('error') || this.model.get('processing'));
                this.$emptyMessage.toggleClass('hide', !showEmptyMessage);
            }
        },

        updateProcessing: function() {
            if (this.$processing) {
                this.$processing.toggleClass('hide', !this.model.get('processing'));
            }
        },

        updateError: function() {
            if (this.$errorMessage) {
                this.$errorMessage.toggleClass('hide', !this.model.get('error'));
            }
        }
    });
});
