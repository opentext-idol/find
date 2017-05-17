/*
 * Copyright 2015-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/indexes/indexes-view',
    'databases-view/js/hod-database-helper',
    'js-whatever/js/escape-hod-identifier',
    'find/app/configuration',
    'i18n!find/nls/indexes'
], function(IndexesView, databaseHelper, escapeHodIdentifier, configuration, i18n) {
    'use strict';

    function getPublicIndexIds(enabled) {
        return enabled ?
            [{
                name: 'public',
                displayName: i18n['search.indexes.publicIndexes'],
                className: 'list-unstyled',
                filter: function(model) {
                    return model.get('domain') === 'PUBLIC_INDEXES';
                }
            }]
            : [];
    }

    return IndexesView.extend({
        databaseHelper: databaseHelper,

        getIndexCategories: function() {
            return [{
                name: 'private',
                displayName: i18n['search.indexes.privateIndexes'],
                className: 'list-unstyled',
                filter: function(model) {
                    return model.get('domain') !== 'PUBLIC_INDEXES';
                }
            }].concat(getPublicIndexIds(configuration().publicIndexesEnabled));
        }
    });
});
