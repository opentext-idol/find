/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
    'find/app/model/documents-collection',
    'find/app/page/search/document/similar-abstract-tab'
], function(DocumentsCollection, SimilarAbstractTab) {
    'use strict';

    return SimilarAbstractTab.extend({
        createCollection: function() {
            return new DocumentsCollection([], {
                indexes: this.indexesCollection.pluck('id')
            });
        },

        fetchData: function() {
            return {
                text: '*',
                max_results: 5,
                sort: 'relevance',
                summary: 'context',
                indexes: this.indexesCollection.pluck('id'),
                fieldText: 'MATCH{' + this.model.get('sourceType') + '}:SOURCETYPE',
                queryType: 'MODIFIED'
            }
        }
    });
});
