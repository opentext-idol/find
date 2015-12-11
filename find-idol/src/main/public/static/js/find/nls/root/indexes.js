/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/substitution'
], function (substitution) {
    'use strict';

    return substitution({
        'search.error.noIndexes': 'The list of databases has not yet been retrieved',
        'search.indexes': 'Databases',
        'search.indexes.all': 'All',
        'search.indexes.empty': 'No Available Databases'
    });
});
