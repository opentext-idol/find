/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/substitution'
], function (substitution) {
    'use strict';

    return substitution({
        'search.error.noIndexes': 'The list of indexes has not yet been retrieved',
        'search.indexes': 'Indexes',
        'search.indexes.all': 'All',
        'search.indexes.publicIndexes': 'Public Indexes',
        'search.indexes.privateIndexes': 'Private Indexes',
        'search.indexes.empty': 'No Available Indexes'
    });
});
