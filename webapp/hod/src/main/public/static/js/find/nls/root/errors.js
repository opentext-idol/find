/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
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
    'use strict';

    // Caution: only use this file for errors that the user can understand and solve. If an error is listed here, by default the app will not advise the user to contact support.
    return substitution({
        'error.code.INVALID_QUERY_TEXT': 'Invalid query text: all terms were stopwords, too short, or incorrectly formatted',
        'error.code.NO_IGNORE_SPECIALS': 'Find did not understand your search text'
    });
});
