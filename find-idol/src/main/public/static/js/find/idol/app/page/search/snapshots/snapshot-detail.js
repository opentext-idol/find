/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'i18n!find/nls/bundle'
], function(i18n) {

    var DATE_FORMAT = 'YYYY/MM/DD HH:mm';

    /**
     * Target attributes and an attributes processor for the "Snapshot" {@link DataPanelView}.
     */
    return {
        targetAttributes: [
            'resultCount',
            'dateCreated'
        ],

        /**
         * @type {DataPanelAttributesProcessor}
         */
        processAttributes: function(attributes) {
            // Cannot use falsy check here since result count could be 0
            //noinspection EqualityComparisonWithCoercionJS
            var resultCountContent = attributes.resultCount != null ? attributes.resultCount : i18n['app.unknown'];

            return [{
                title: i18n['search.snapshot.detail.dateCreated'],
                content: attributes.dateCreated.format(DATE_FORMAT)
            }, {
                title: i18n['search.snapshot.detail.resultCount'],
                content: resultCountContent
            }];
        }
    };

});
