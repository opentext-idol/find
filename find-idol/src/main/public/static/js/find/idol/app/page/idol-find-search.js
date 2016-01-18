/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/find-search',
    'find/idol/app/page/search/idol-service-view'
], function(FindSearch, ServiceView) {
    'use strict';

    return FindSearch.extend({
        ServiceView: ServiceView
    });
});
