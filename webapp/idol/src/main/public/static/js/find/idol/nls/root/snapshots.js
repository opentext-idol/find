/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */
define([
    'js-whatever/js/substitution'
], function(substitution) {

    return substitution({
        'snapshot': 'Snapshot',
        'detail.dateCreated': 'Date Created',
        'detail.resultCount': 'Result Count',
        'detail.owner': 'Owner',
        'detailTitle.snapshot': 'Snapshot',
        'detailTitle.readonly': 'Read Only',
        'restrictions.maxDate': 'Until Date',
        'restrictions.minDate': 'From Date',
        'restrictions.relatedConcepts': 'Concepts',
        'restrictions.documentSelection': 'Document Selection',
        'restrictionsTitle': 'Query Restrictions',
        'openEdit.create': 'Save as snapshot',
        'openEdit.edit': 'Save as snapshot'
    });

});
