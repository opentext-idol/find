/*
 * Copyright 2016-2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/abstract-query-left-side-view',
    'find/hod/app/page/search/filters/indexes/hod-indexes-view'
], function(AbstractQueryLeftSideView, HodIndexesView) {
    'use strict';

    return AbstractQueryLeftSideView.extend({
        IndexesView: HodIndexesView
    });
});
