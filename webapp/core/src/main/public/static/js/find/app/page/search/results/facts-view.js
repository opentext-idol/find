/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

/**
 * List entities related to the current search.  On selecting an entity, view facts involving the
 * entity.
 *
 * The facts entity field must be configured as a parametric field.
 *
 * Dependencies:
 *  - documentRenderer
 *  - queryModel
 *  - parametricCollection
 *  - previewModeModel
 *
 * To be used with `ResultsViewAugmentation` (the `previewModeModel` collection is used to signal
 * that a document preview should be opened).
 */

define([
    'underscore',
    'backbone',
    'jquery',
    'find/app/configuration',
    'i18n!find/nls/bundle',
    'find/app/model/documents-collection',
    'find/app/page/search/results/field-selection-view',
    'find/app/model/entity-fact-collection',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/facts-view.html'
], function(
    _, Backbone, $, configuration, i18n,
    DocumentsCollection, FieldSelectionView, EntityFactCollection, generateErrorHtml,
    template
) {
    'use strict';

    const FACT_ENTITY_FIELD = 'FACTS/FACT_EXTRACT_/ENTITIES/VALUE';

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function (options) {
            this.documentRenderer = options.documentRenderer;
            this.queryModel = options.queryModel;
            this.parametricCollection = options.parametricCollection;
            this.previewModeModel = options.previewModeModel;

            // listed facts - replaced on update
            this.entityFactCollection = new EntityFactCollection();
            // ongoing facts fetch
            this.entityFactCollectionRequest = null;
            // the previewed document - replaced on update
            this.previewDocCollection = new DocumentsCollection();
            // the selected entity - owned by `entitySelector`
            this.entitySelectionModel = null;
            // `FieldSelectionView`
            this.entitySelector = null;

            this.listenTo(this.parametricCollection, 'sync', this.updateEntitySelector);
            this.listenTo(this.entityFactCollection, 'sync', this.showFacts);
            this.listenTo(this.entityFactCollection, 'error', this.showFactsError);
            this.listenTo(this.previewDocCollection, 'sync', this.showPreview);
            this.listenTo(this.previewDocCollection, 'error', this.showPreviewError);
        },

        events: {
            'click .facts-list [data-docref]': function (e) {
                const fieldText = 'MATCH{' + (
                    encodeURIComponent($(e.target).data('docref'))
                ) + '}:' + encodeURIComponent(configuration().referenceField);

                this.previewDocCollection.fetch({
                    reset: true,
                    data: {
                        indexes: this.queryModel.get('indexes'),
                        max_results: 1,
                        text: '*',
                        field_text: fieldText,
                        queryType: 'RAW',
                        summary: 'context',
                    }
                });
            }
        },

        render: function () {
            this.$el.html(this.template({
                i18n: i18n
            }));
            this.update();
        },

        update: function() {
            this.updateEntitySelector();
        },

        /**
         * Initialise or update the entity selection control.
         */
        updateEntitySelector: function () {
            if (!this.$el.is(':visible')) {
                return;
            }

            if (this.entitySelector) {
                this.entitySelector.remove();
            }

            const parametricModel = this.parametricCollection
                .findWhere({ type: 'Parametric', id: FACT_ENTITY_FIELD });
            // entities are already sorted by descending count
            const entities = (
                (parametricModel && parametricModel.get('values')) || []
            ).map(function (entity) {
                const displayName = entity.displayValue +
                    (entity.count == null ? '' : (' (' + entity.count + ')'));
                return { id: entity.value, displayName: displayName };
            });

            if (!this.entitySelectionModel || entities.length === 0) {
                // FieldSelectionView doesn't trigger change when empty
                this.entitySelectionModel = new Backbone.Model();
                this.listenTo(this.entitySelectionModel, 'change:field', this.fetchFacts);
            }

            this.entitySelector = new FieldSelectionView({
                name: 'entity',
                fields: entities,
                model: this.entitySelectionModel,
                width: "175px"
            });

            this.$('.facts-entity-selector').prepend(this.entitySelector.$el);
            this.entitySelector.render();

            if (entities.length === 0) {
                // FieldSelectionView doesn't trigger change when empty
                if (this.entityFactCollectionRequest) {
                    this.entityFactCollectionRequest.abort();
                }
                this.resetView();
            }
        },

        /**
         * Start retrieving facts for the selected entity.
         */
        fetchFacts: function () {
            if (!this.$el.is(':visible')) {
                return;
            }

            const entity = this.entitySelectionModel.get('field');
            this.showLoading();
            this.entityFactCollectionRequest = this.entityFactCollection.fetch({
                reset: true,
                data: {
                    entity: entity,
                    indexes: this.queryModel.get('indexes')
                }
            });
        },

        /**
         * Hide all optional elements of the view.
         */
        resetView: function () {
            this.$('.facts-loading').addClass('hide');
            this.$('.facts-error').addClass('hide');
            this.$('.facts-empty').addClass('hide');
            this.$('.facts-list').addClass('hide');
        },

        /**
         * Show only an indicator that facts are being retrieved.
         */
        showLoading: function () {
            this.resetView();
            this.$('.facts-loading').removeClass('hide');
        },

        /**
         * Show an error because a request failed.  Doesn't hide any other elements.
         *
         * @param xhr - request object
         * @param errorId - ID from which to retrieve the error message string, below
         *                  'search.resultsView.facts.error'
         */
        showError: function (xhr, errorId) {
            this.$('.facts-error').html(generateErrorHtml({
                errorDetails: xhr.responseJSON && xhr.responseJSON.message,
                errorUUID: xhr.responseJSON && xhr.responseJSON.uuid,
                isUserError: xhr.responseJSON && xhr.responseJSON.isUserError,
                messageToUser: i18n['search.resultsView.facts.error.' + errorId]
            }));
            this.$('.facts-error').removeClass('hide');
        },

        /**
         * Show an error because retrieving facts failed.  Collection 'error' event handler.
         */
        showFactsError: function (_0, xhr) {
            if (xhr.status === 0 && xhr.statusText === 'abort') {
                // canceled
                return;
            }
            this.resetView();
            this.showError(xhr, 'facts');
        },

        /**
         * Show an error because retrieving document to preview failed.  Collection 'error' event
         * handler.
         */
        showPreviewError: function (_0, xhr) {
            if (xhr.status === 0 && xhr.statusText === 'abort') {
                // canceled
                return;
            }
            this.showError(xhr, 'preview');
        },

        /**
         * Show already-retrieved facts.
         */
        showFacts: function () {
            this.resetView();
            if (this.entityFactCollection.length === 0) {
                this.$('.facts-empty').removeClass('hide');
            } else {
                const document = new Backbone.Model();
                document.set('facts', this.entityFactCollection.map(
                    function (entityFactModel) {
                        return entityFactModel.toJSON();
                    }));
                document.set('fields', []);

                this.$('.facts-list').html(this.documentRenderer.renderEntityFacts(document));
                this.$('.facts-list').removeClass('hide');
            }
        },

        /**
         * Show already-retrieved document to preview (via `ResultsViewAugmentation`).
         */
        showPreview: function () {
            const docModel = this.previewDocCollection.models[0];
            if (docModel) {
                this.$('.facts-error').addClass('hide');
                this.previewModeModel.set({ document: docModel, mode: 'summary' });
            }
        }

    });

});
