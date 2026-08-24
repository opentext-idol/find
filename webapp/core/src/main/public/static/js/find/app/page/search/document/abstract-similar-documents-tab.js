/*
 * Copyright 2016-2018 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

const _ = require('underscore');
const vent = require('find/app/vent');
const SimilarDocumentsCollection = require('find/app/model/similar-documents-collection');
const SimilarAbstractTab = require('find/app/page/search/document/similar-abstract-tab');

module.exports = SimilarAbstractTab.extend({
        events: _.extend({
            'click .similar-documents-tab-see-more': function() {
                vent.navigate(vent.suggestRouteForDocument(this.model));
            }
        }, SimilarAbstractTab.prototype.events),

        // overridden
        getIndexes: _.constant([]),

        createCollection: function() {
            return new SimilarDocumentsCollection();
        },

        fetchData: function () {
            return {
                max_results: 3,
                start: 1,
                summary: 'context',
                indexes: this.getIndexes(),
                reference: this.model.get('reference'),
                highlight: true
            };
        }

    });

