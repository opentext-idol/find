/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'backbone',
    'underscore',
    'jquery',
    'js-whatever/js/list-view',
    'js-whatever/js/filtering-collection',
    'find/app/page/search/filters/parametric/parametric-field-view',
    'find/app/util/model-any-changed-attribute-listener',
    'fieldtext/js/field-text-parser',
    'parametric-refinement/display-collection',
    'i18n!find/nls/bundle',
    'text!find/templates/app/page/search/filters/parametric/parametric-view.html'
], function(Backbone, _, $, ListView, FilteringCollection, FieldView, addChangeListener, parser, DisplayCollection, i18n, template) {

    return Backbone.View.extend({
        template: _.template(template)({i18n: i18n}),

        events: {
            'click [data-field] [data-value]': function(e) {
                var $target = $(e.currentTarget);
                var $field = $target.closest('[data-field]');

                var attributes = {
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

        initialize: function(options) {
            this.selectedParametricValues = options.queryState.selectedParametricValues;

            this.model = new Backbone.Model({
                processing: Boolean(options.parametricCollection.currentRequest),
                error: false,
                empty: options.parametricCollection.isEmpty()
            });

            this.listenTo(this.model, 'change:processing', this.updateProcessing);
            this.listenTo(this.model, 'change:error', this.updateError);
            this.listenTo(this.model, 'change', this.updateEmpty);

            this.listenTo(options.parametricCollection, 'request', function() {
                this.model.set({processing: true, error: false});
            });

            this.listenTo(options.parametricCollection, 'error', function(collection, xhr) {
                if (xhr.status !== 0) {
                    // The request was not aborted, so there isn't another request in flight
                    this.model.set({error: true, processing: false});
                } else {
                    this.model.set({processing: Boolean(collection.currentRequest)});
                }
            });

            this.listenTo(options.parametricCollection, 'sync', function() {
                this.model.set({processing: false});
            });
            
            this.listenTo(options.parametricCollection, 'update reset', function() {
                this.model.set('empty', options.parametricCollection.isEmpty());
            });

            this.displayCollection = new DisplayCollection([], {
                parametricCollection: options.parametricCollection,
                selectedParametricValues: this.selectedParametricValues
            });

            this.fieldNamesListView = new ListView({
                collection: this.displayCollection,
                ItemView: FieldView
            });
        },

        render: function() {
            this.$el.html(this.template).prepend(this.fieldNamesListView.render().$el);

            this.$emptyMessage = this.$('.parametric-empty');
            this.$errorMessage = this.$('.parametric-error');
            this.$processing = this.$('.parametric-processing-indicator');

            this.updateEmpty();
            this.updateError();
            this.updateProcessing();

            return this;
        },

        remove: function() {
            this.fieldNamesListView.remove();
            this.displayCollection.stopListening();
            Backbone.View.prototype.remove.call(this);
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
        },
        
        updateEmpty: function() {
            if (this.$emptyMessage) {
                var showEmptyMessage = this.model.get('empty') && !(this.model.get('error') || this.model.get('processing'));
                this.$emptyMessage.toggleClass('hide', !showEmptyMessage);
            }
        }
    });

});
