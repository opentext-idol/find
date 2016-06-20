/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/abstract-query-left-side-view',
    'find/idol/app/page/search/filters/indexes/idol-indexes-view',
    'find/idol/app/page/search/filters/parametric/idol-numeric-parametric-view'
], function(AbstractQueryLeftSideView, IdolIndexesView, IdolNumericParametricView) {

    return AbstractQueryLeftSideView.extend({
        IndexesView: IdolIndexesView,
        NumericParametricView: IdolNumericParametricView
    });

});
