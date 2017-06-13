/*
 * Copyright 2017 Hewlett Packard Enterprise Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/model/find-base-collection'
], function (FindBaseCollection) {
    'use strict';

    return FindBaseCollection.Model.extend({
        url: 'api/public/parametric/date/value-details'
    });
});
