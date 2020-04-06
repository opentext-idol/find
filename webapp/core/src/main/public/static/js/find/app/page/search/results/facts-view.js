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
    'find/app/model/document-model',
    'find/app/model/parametric-collection',
    'find/app/model/entity-fact-collection',
    'find/app/page/search/results/field-selection-view',
    'find/app/util/generate-error-support-message',
    'text!find/templates/app/page/search/results/facts-view.html'
], function(
    _, Backbone, $, configuration, i18n,
    DocumentModel, ParametricCollection, EntityFactCollection,
    FieldSelectionView, generateErrorHtml,
    template
) {
    'use strict';

    const FACT_ENTITY_FIELD = 'FACTS/FACT_EXTRACT_/ENTITIES/VALUE';
    const MAX_ENTITIES = 100;
    const MAX_FACTS = 30;

    return Backbone.View.extend({
        template: _.template(template),

        initialize: function (options) {
            this.documentRenderer = options.documentRenderer;
            this.queryModel = options.queryModel;
            this.parametricCollection = options.parametricCollection;
            this.previewModeModel = options.previewModeModel;

            // fact entity values
            this.factsParametricCollection = new ParametricCollection([], {
                url: 'api/public/parametric/values'
            });
            // listed facts - replaced on update
            this.entityFactCollection = new EntityFactCollection();
            // ongoing facts fetch
            this.entityFactCollectionRequest = null;
            // the previewed document - replaced on update
            this.previewDocModel = new DocumentModel();
            // the selected entity - owned by `entitySelector`
            this.entitySelectionModel = null;
            // `FieldSelectionView`
            this.entitySelector = null;

            this.listenTo(this.queryModel, 'change', this.update);
            this.listenTo(this.parametricCollection, 'sync', this.update);
            this.listenTo(this.factsParametricCollection, 'sync', this.updateEntitySelector);
            this.listenTo(this.factsParametricCollection, 'error', this.showEntitiesError);
            this.listenTo(this.entityFactCollection, 'sync', this.showFacts);
            this.listenTo(this.entityFactCollection, 'error', this.showFactsError);
            this.listenTo(this.previewModeModel, 'change', this.updateSelectedFact);
            this.listenTo(this.previewDocModel, 'sync', this.showDocPreview);
            this.listenTo(this.previewDocModel, 'error', this.showPreviewError);
        },

        events: {
            'click .facts-list .fact-sentence[data-factid]': function (e) {
                const $fact = $(e.target);
                if ($fact.hasClass('selected-fact')) {
                    this.previewModeModel.set({ mode: null });
                } else {
                    this.showFactPreview($fact);
                }
            }
        },

        render: function () {
            this.$el.html(this.template({
                i18n: i18n
            }));
            this.update();
        },

        update: function() {
            if (!this.$el.is(':visible')) {
                return;
            }

            if (this.queryModel.get('indexes').length) {
                this.factsParametricCollection.fetchFromQueryModel(this.queryModel, {
                    fieldNames: [FACT_ENTITY_FIELD],
                    maxValues: MAX_ENTITIES
                });
            }
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

            const parametricModel = this.factsParametricCollection
                .findWhere({ type: 'Parametric', id: FACT_ENTITY_FIELD });
            // entities are already sorted by descending count
            const entities = (
                (parametricModel && parametricModel.get('values')) || []
            ).map(function (entity) {
                return { id: entity.value, displayName: entity.displayValue };
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
                width: "50%"
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

        updateSelectedFact: function () {
            this.$('.facts-list [data-factid]').removeClass('selected-fact');
            if (this.previewModeModel.get('mode') === 'fact') {
                const selectedFact = this.previewModeModel.get('fact');
                if (selectedFact) {
                    this.$('.facts-list [data-factid="' + selectedFact.get('fact').source + '"]')
                        .addClass('selected-fact');
                }
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
                    indexes: this.queryModel.get('indexes'),
                    maxResults: MAX_FACTS
                }
            });
        },

        /**
         * Show a preview for a document.
         */
        previewDoc: function (index, reference) {
            this.previewDocModel.fetch({ data: {
                database: index,
                reference: reference
            } });
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
         * Show an error because retrieving entity values failed.  Collection 'error' event handler.
         */
        showEntitiesError: function (_0, xhr) {
            if (xhr.status === 0 && xhr.statusText === 'abort') {
                // canceled
                return;
            }
            this.resetView();
            this.showError(xhr, 'entities');
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
                    function (factModel) {
                        return factModel.toJSON();
                    }));
                document.set('fields', []);

                this.$('.facts-list').html(this.documentRenderer.renderEntityFacts(document));
                this.$('.facts-list').removeClass('hide');
            }
        },

        /**
         * Show preview for the given clicked fact sentence (via `ResultsViewAugmentation`).
         *
         * @param $fact
         */
        showFactPreview: function ($fact) {
            const factModel = _.find(this.entityFactCollection.models, function (factModel) {
                return factModel.get('fact').source === $fact.data('factid');
            });

            if (factModel) {
                this.$('.facts-error').addClass('hide');
                this.previewModeModel.set({
                    mode: 'fact',
                    fact: factModel,
                    factsView: this
                });
                // keep the clicked fact visible
                $fact[0].scrollIntoView({ behavior: 'smooth', block: 'nearest' });
            }
        },

        /**
         * Show preview for already-retrieved document (via `ResultsViewAugmentation`).
         */
        showDocPreview: function () {
            this.$('.facts-error').addClass('hide');
            this.previewModeModel.set({ mode: 'summary', document: this.previewDocModel });
        }

    });

});
