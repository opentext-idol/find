/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/abstract-query-left-side-view',
    'find/hod/app/page/search/filters/indexes/hod-indexes-view',
    'find/hod/app/page/search/filters/parametric/hod-numeric-parametric-view'
], function(AbstractQueryLeftSideView, HodIndexesView, HodNumericParametricView) {

    return AbstractQueryLeftSideView.extend({
        IndexesView: HodIndexesView,
        NumericParametricView: HodNumericParametricView
    });

});
