/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */
define([
    'js-whatever/js/substitution'
], function(substitution) {

    return substitution({
        'snapshot': 'Snapshot',
        'detail.dateCreated': 'Date Created',
        'detail.resultCount': 'Result Count',
        'detailTitle': 'Snapshot',
        'restrictions.maxDate': 'Until Date',
        'restrictions.minDate': 'From Date',
        'restrictions.queryText': 'Query Text',
        'restrictions.relatedConcepts': 'Also searching for',
        'restrictionsTitle': 'Query Restrictions'
    });

});
