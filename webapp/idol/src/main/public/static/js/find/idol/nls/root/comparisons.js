/*
 * Copyright 2016 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

define([
    'js-whatever/js/substitution'
], function(substitution) {

    return substitution({
        'compare': 'Compare',
        'compareSaved': 'Compare saved searches',
        'error.default': 'An error occurred fetching comparison',
        'list.title.first': 'Exclusive to "{0}"',
        'list.title.both': 'Common to both',
        'list.title.second': 'Exclusive to "{0}"',
        'selectedSearch': 'Selected',
        'selectSearchToCompare': 'Choose another search to compare'
    });

});
