/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
