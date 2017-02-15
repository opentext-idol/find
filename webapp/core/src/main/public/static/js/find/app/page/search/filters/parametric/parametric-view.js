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
    'parametric-refinement/display-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/parametric-view.html'
], function(Backbone, $, _, ListView, metrics, FieldView, ProxyView, CollapsibleNumericFieldView,
            DisplayCollection, i18n, template) {
    'use strict';

    const TARGET_NUMBER_OF_PIXELS_PER_BUCKET = 10;

    return Backbone.View.extend({
        template: _.template(template)({i18n: i18n}),

        events: {
            'click [data-field] [data-value]': function (e) {
                var $target = $(e.currentTarget);
                var $field = $target.closest('[data-field]');

                var attributes = {
                    field: $field.attr('data-field'),
                    value: $target.attr('data-value')
                };

                if ($(e.target).closest('.parametric-value-graph-cell').length) {
                    this.selectedParametricValues.trigger('graph', attributes.field, attributes.value);
                } else if (this.selectedParametricValues.get(attributes)) {
                    this.selectedParametricValues.remove(attributes);
                } else {
                    this.selectedParametricValues.add(attributes);
                }
            }
        },

        initialize: function (options) {
            this.restrictedParametricCollection = options.restrictedParametricCollection;
            this.selectedParametricValues = options.queryState.selectedParametricValues;
            this.displayCollection = options.displayCollection;
            this.filterModel = options.filterModel;
            this.showGraphButtons = options.showGraphButtons;

            //ToDo : We are currently only monitoring restrictedParametricCollection for loading and error. Need to fix as part of FIND-618.
            this.model = new Backbone.Model({
                processing: Boolean(this.restrictedParametricCollection.currentRequest),
                error: false,
                empty: this.collection.isEmpty()
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.model, 'change:processing', this.updateProcessing);
            //noinspection JSUnresolvedFunction
            this.listenTo(this.model, 'change:error', this.updateError);
            //noinspection JSUnresolvedFunction
            this.listenTo(this.model, 'change', this.updateEmpty);

            //noinspection JSUnresolvedFunction
            this.listenTo(this.restrictedParametricCollection, 'request', function() {
                this.model.set({processing: true, error: false});
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.restrictedParametricCollection, 'error', function(collection, xhr) {
                if (xhr.status === 0) {
                    this.model.set({processing: Boolean(this.restrictedParametricCollection.currentRequest)});
                } else {
                    // The request was not aborted, so there isn't another request in flight
                    this.model.set({error: true, processing: false});
                }
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.restrictedParametricCollection, 'sync', function() {
                this.model.set({processing: false});

                if (!this.restrictedParametricCollection.isEmpty() && !this.parametricValuesLoaded) {
                    this.parametricValuesLoaded = true;
                    metrics.addTimeSincePageLoad('parametric-values-first-loaded');
                }
            });

            //noinspection JSUnresolvedFunction
            this.listenTo(this.collection, 'update reset', function() {
                this.model.set('empty', this.collection.isEmpty());
            });

            var collapsed = {};

            var isCollapsed = function (model) {
                if (this.filterModel && this.filterModel.get('text')) {
                    return false;
                } else {
                    //noinspection JSUnresolvedFunction
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
                        parametricCollection: options.parametricCollection,
                        // collection is not passed to the individual views
                        parametricDisplayCollection: this.displayCollection,
                        selectedParametricValues: this.selectedParametricValues,
                        timeBarModel: options.timeBarModel,
                        showGraphButtons: options.showGraphButtons
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
                        collapsed: isCollapsed,
                        showGraphButtons: options.showGraphButtons
                    }
                }
            });

            //noinspection JSUnresolvedFunction
            // Would ideally use model.cid but on refresh the display Collection creates new models with different cids
            this.listenTo(this.fieldNamesListView, 'item:toggle', function (model, newState) {
                collapsed[model.id] = newState;
            });
        },

        render: function() {
            //noinspection JSUnresolvedVariable
            this.$el.html(this.template).prepend(this.fieldNamesListView.$el);
            this.fieldNamesListView.render();

            //noinspection JSUnresolvedFunction
            this.$emptyMessage = this.$('.parametric-empty');
            //noinspection JSUnresolvedFunction
            this.$errorMessage = this.$('.parametric-error');
            //noinspection JSUnresolvedFunction
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
                var showEmptyMessage = this.model.get('empty') && this.collection.isEmpty() && !(this.model.get('error') || this.model.get('processing'));
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
