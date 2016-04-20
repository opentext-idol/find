/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/search-filters-collection',
    'i18n!find/nls/indexes'
], function(SearchFiltersCollection, i18n) {
    'use strict';

    return SearchFiltersCollection.extend({
        getDatabasesFilterText: function() {
            var databaseFilter = this.selectedIndexesCollection.map(function (model) {
                var displayName = this.indexesCollection.findWhere({name: model.get('name'), domain: model.get('domain')}).get('displayName');
                return displayName || model.get('name');
            }, this);

            return i18n['search.indexes'] + ': ' + databaseFilter.join(', ');
        }
    });

});
