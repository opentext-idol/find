/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'i18n!find/nls/bundle',
    'i18n!find/idol/nls/snapshots',
    'find/app/configuration',
    'moment'
], function(i18n, snapshotsI18n, configuration, moment) {

    var DATE_FORMAT = 'YYYY/MM/DD HH:mm';

    /**
     * Target attributes and an attributes processor for the "Snapshot" {@link DataPanelView}.
     */
    return {
        targetAttributes: [
            'resultCount',
            'dateCreated',
            'user'
        ],

        /**
         * @type {DataPanelAttributesProcessor}
         */
        processAttributes: function(savedSearchModel, attributes) {
            var resultCount = attributes.resultCount;

            var searchOwner = attributes.user && attributes.user.username !== configuration().username ? {
                title: snapshotsI18n['detail.owner'],
                content: attributes.user.username
            } : null;

            // Cannot use falsy check here since result count could be 0
            //noinspection EqualityComparisonWithCoercionJS
            var resultCountContent = resultCount != null ? {
                title: snapshotsI18n['detail.resultCount'],
                content: resultCount
            } : null;

            var dateCreated = moment(attributes.dateCreated);

            return _.compact([
                searchOwner,
                {
                    title: snapshotsI18n['detail.dateCreated'],
                    content: dateCreated.format(DATE_FORMAT)
                },
                resultCountContent
            ]);
        }
    };

});
