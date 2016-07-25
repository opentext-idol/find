/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'js-whatever/js/substitution'
], function (substitution) {
    'use strict';

    return substitution({
        'error.default.message.uuid': 'Unknown error with UUID: {0}, please contact support.',
        'error.default.message': 'Unknown error, please contact support.',
        'hod.error.INVALID_QUERY_TEXT': 'All terms were invalid, through being stopwords, too short, or incorrectly formatted',
        'hod.error.NO_IGNORE_SPECIALS': 'Invalid use of special tokens',
        'hod.error.QUERY_PROFILE_NAME_INVALID': 'Query profile name is invalid, please contact support',
        'hod.error.QUERY_PROFILE_NAME_NOT_PROVIDED': 'Query profile name missing, please contact support',
        'hod.error.QUERY_MANIPULATION_INDEX_INVALID': 'Query manipulation index is invalid, please contact support',
        'hod.error.QUERY_MANIPULATION_INDEX_MISSING': 'Query manipulation index is missing, please contact support',
        'hod.error.QUERY_MANIPULATION_RULE_INVALID': 'Query manipulation rule is invalid, please contact support',
        'hod.error.QUERY_MANIPULATION_RULE_MISSING': 'Query manipulation rule is missing, please contact support',
        'hod.error.QUERY_MANIPULATION_TEXT_INVALID': 'Query manipulation text is invalid, please contact support',
        'hod.error.QUERY_MANIPULATION_TEXT_MISSING': 'Query manipulation text is missing, please contact support'
    });
});
