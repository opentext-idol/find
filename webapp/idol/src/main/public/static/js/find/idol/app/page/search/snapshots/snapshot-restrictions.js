/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
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
    'find/app/page/search/filters/parametric/numeric-range-rounder',
    'i18n!find/nls/bundle',
    'i18n!find/nls/indexes',
    'i18n!find/idol/nls/snapshots',
    'underscore',
    'moment',
    'find/app/model/document-selection-model'
], function(rounder, i18n, indexesI18n, snapshotsI18n, _, moment, DocumentSelectionModel) {

    const DATE_FORMAT = 'YYYY/MM/DD HH:mm';

    function dateRestriction(title, date) {
        return date ? {title: title, content: date.format(DATE_FORMAT)} : null;
    }

    function formatEpoch(x) {
        return moment(x).format(DATE_FORMAT);
    }

    function relatedConcepts(concepts) {
        return concepts.length ? {
            title: snapshotsI18n['restrictions.relatedConcepts'],
            content: concepts.join(', ')
        } : null;
    }

    function documentSelection (savedSearchModel) {
        const documentSelectionModel =
            new DocumentSelectionModel(savedSearchModel.toDocumentSelectionModelAttributes());
        return documentSelectionModel.isDefault() ? null : {
            title: snapshotsI18n['restrictions.documentSelection'],
            content: documentSelectionModel.describe()
        };
    }

    /**
     * Target attributes and an attributes processor for the "Query Restrictions" {@link DataPanelView}.
     */
    return {
        targetAttributes: [
            'queryText',
            'relatedConcepts',
            'indexes',
            'parametricValues',
            'minDate',
            'maxDate',
            'documentSelectionIsWhitelist',
            'documentSelection',
            'parametricRanges'
        ],

        /**
         * @type {DataPanelAttributesProcessor}
         */
        processAttributes: function(savedSearchModel, attributes) {
            const indexesContent = _.pluck(attributes.indexes, 'name').join(', ');

            const parametricRestrictions = _.map(_.groupBy(attributes.parametricValues, 'field'), function (items) {
                return {
                    title: items[0].displayName,
                    content: _.pluck(items, 'displayValue').join(', ')
                };
            });

            const numericRestrictions = _.map(attributes.parametricRanges, function (range) {
                const formatFunction = range.type === 'Date' ? formatEpoch : rounder().round;

                return {
                    title: range.displayName,
                    content: formatFunction(range.min, range.min, range.max) + ' \u2013 ' + formatFunction(range.max, range.min, range.max)
                };
            });

            return [
                relatedConcepts(attributes.relatedConcepts),
                {
                    title: indexesI18n['search.indexes'],
                    content: indexesContent
                },
                dateRestriction(snapshotsI18n['restrictions.minDate'], attributes.minDate),
                dateRestriction(snapshotsI18n['restrictions.maxDate'], attributes.maxDate),
                documentSelection(savedSearchModel),
            ].concat(
                parametricRestrictions,
                numericRestrictions);
        }
    };
});
