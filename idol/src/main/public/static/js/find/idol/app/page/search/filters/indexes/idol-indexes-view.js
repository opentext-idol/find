/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/filters/indexes/indexes-view',
    'databases-view/js/idol-database-helper'
], function (IndexesView, databaseHelper) {
    'use strict';

    return IndexesView.extend({
        databaseHelper: databaseHelper,
        
        getIndexCategories: function () {
            return null;
        }
    });
});
