/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
        parametricValueModels, geographyModel, documentSelectionModel
    ) {
        const parametricFieldText = toFieldTextNode(_.map(parametricValueModels, function (model) {
            return model.toJSON();
        }));
        return mergeFieldText([
            parametricFieldText,
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
            new SelectedParametricValuesCollection(model.toSelectedParametricValues().models),
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
        mergeFieldText: mergeFieldText,
        buildMergedFieldText: buildMergedFieldText,
        buildMergedFieldTextWithoutDocumentSelection: buildMergedFieldTextWithoutDocumentSelection
    };
});
