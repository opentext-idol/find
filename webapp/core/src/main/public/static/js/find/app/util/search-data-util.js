/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'underscore',
    'parametric-refinement/to-field-text-node',
    'parametric-refinement/selected-values-collection',
    'find/app/model/geography-model',
    'find/app/model/document-selection-model'
], function(
    _, toFieldTextNode, SelectedParametricValuesCollection, GeographyModel, DocumentSelectionModel
) {
    'use strict';

    function wrapInBrackets(concept) {
        return concept
            ? '(' + concept + ')'
            : concept;
    }

    // WARNING: This logic is duplicated in the server-side SavedSearch class
    /**
     * Build query text from the text in the search bar and an array of concept groups (none of which are *).
     * @param {Array.<Array.<string>>} concepts
     * @return {string}
     */
    function makeQueryText(concepts) {
        if(!concepts || concepts.length < 1) {
            return '*';
        }

        return concepts.map(function(concept) {
            return wrapInBrackets(concept.join(' '));
        }).join(' AND ');
    }

    /**
     * Create an array of strings representing the given selected indexes suitable for sending to the server.
     * @param {Array} selectedIndexesArray
     * @return {string[]}
     */
    function buildIndexes(selectedIndexesArray) {
        return _.map(selectedIndexesArray, function(index) {
            return index.domain
                ? encodeURIComponent(index.domain) + ':' + encodeURIComponent(index.name)
                : encodeURIComponent(index.name);
        });
    }

    /**
     * Convert an array of parametric fields and values or ranges to a field text string.
     * @param {Array} parametricValues
     * @return {string} A field text string or null
     */
    function buildFieldText(parametricValues) {
        const fieldTextNode = toFieldTextNode(parametricValues);
        return fieldTextNode && fieldTextNode.toString();
    }

    /**
     * Return a fieldtext node which is the AND-combination of multiple fieldtext nodes, each
     * possibly null.
     */
    function mergeFieldText(nodes) {
        return _.chain(nodes)
            .compact()
            .reduce(function (fieldText, extraFieldText) {
                return fieldText ?
                    (extraFieldText ? fieldText.AND(extraFieldText) : fieldText) :
                    extraFieldText
            }, null)
            .value() || null;
    }

    function buildMergedFieldText(
        selectedParametricValues, geographyModel, documentSelectionModel
    ) {
        return mergeFieldText([
            selectedParametricValues.toFieldTextNode(),
            geographyModel.toFieldText(),
            documentSelectionModel.toFieldText()
        ]);
    }

    function buildMergedFieldTextWithoutDocumentSelection(
        selectedParametricValues, geographyModel
    ) {
        return mergeFieldText([
            selectedParametricValues.toFieldTextNode(),
            geographyModel.toFieldText()
        ]);
    }

    /**
     * Creates query parameters from a saved search model.
     * @param {Backbone.Model} model A model with attributes of type {@link SavedSearchModelAttributes}
     * @return {{minDate: *, maxDate: *, queryText: string, databases, fieldText, anyLanguage: boolean}}
     */
    function buildQuery(model) {
        const fieldTextNode = buildMergedFieldText(
            new SelectedParametricValuesCollection(model.toSelectedParametricValues()),
            new GeographyModel(model.toGeographyModelAttributes()),
            new DocumentSelectionModel(model.toDocumentSelectionModelAttributes())
        );

        return {
            minDate: model.get('minDate'),
            maxDate: model.get('maxDate'),
            queryText: makeQueryText(model.get('relatedConcepts')),
            databases: buildIndexes(model.get('indexes')),
            fieldText: fieldTextNode && fieldTextNode.toString(),
            anyLanguage: true
        };
    }

    return {
        makeQueryText: makeQueryText,
        buildIndexes: buildIndexes,
        buildQuery: buildQuery,
        buildFieldText: buildFieldText,
        buildMergedFieldText: buildMergedFieldText,
        buildMergedFieldTextWithoutDocumentSelection: buildMergedFieldTextWithoutDocumentSelection
    };
});
