/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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
        // TODO: Copypasted from userErrors in IdolGlobalExceptionHandler.java to enable lookup by
        // generate-error-support-message.js. This is used on the frontend to decide if it is
        // a user error and if a 'contact support' message should be displayed. Establish what these errors mean and
        // provide prettified user messages.
        // 'error.code.AXEQUERY504': '',
        // 'error.code.DAHQUERY504': '',
        // 'error.code.AXEQUERY505': '',
        // 'error.code.DAHQUERY505': '',
        // 'error.code.AXEQUERY507': '',
        // 'error.code.DAHQUERY507': '',
        // 'error.code.AXEQUERY508': '',
        // 'error.code.DAHQUERY508': '',
        // 'error.code.AXEQUERY509': '',
        // 'error.code.DAHQUERY509': '',
        // 'error.code.AXEQUERY511': '',
        // 'error.code.DAHQUERY511': '',
        // 'error.code.AXEQUERY513': '',
        // 'error.code.DAHQUERY513': '',
        // 'error.code.AXEGETQUERYTAGVALUES507': '',
        // 'error.code.DAHGETQUERYTAGVALUES507': '',
        // 'error.code.AXEGETQUERYTAGVALUES508': '',
        // 'error.code.DAHGETQUERYTAGVALUES508': '',
        // 'error.code.AXEGETQUERYTAGVALUES519': '',
        // 'error.code.DAHGETQUERYTAGVALUES519': '',
        // 'error.code.AXEGETQUERYTAGVALUES520': '',
        // 'error.code.DAHGETQUERYTAGVALUES520': '',
        // 'error.code.AXEGETQUERYTAGVALUES522': '',
        // 'error.code.DAHGETQUERYTAGVALUES522': '',
        // 'error.code.AXEGETQUERYTAGVALUES538': '',
        // 'error.code.DAHGETQUERYTAGVALUES538': '',
        // 'error.code.QMSQUERY-2147435967': '',
        // 'error.code.QMSQUERY-2147435888': '',
        // 'error.code.QMSGETQUERYTAGVALUES-2147483377': '',
        // 'error.code.AXEGETQUERYTAGVALUES504': '',
        // 'error.code.DAHGETQUERYTAGVALUES504': '',
        // 'error.code.AXEGETQUERYTAGVALUES509': '',
        // 'error.code.DAHGETQUERYTAGVALUES509': '',
        // 'error.code.AXEGETQUERYTAGVALUES513': '',
        // 'error.code.DAHGETQUERYTAGVALUES513': '',
        'error.code.DAHGETQUERYTAGVALUES502': 'Invalid query text: all terms were stopwords, too short, or incorrectly formatted',
        'error.code.AXEGETQUERYTAGVALUES502': 'Invalid query text: all terms were stopwords, too short, or incorrectly formatted',
        'error.code.DAHQUERY502': 'Invalid query text: all terms were stopwords, too short, or incorrectly formatted',
        'error.code.AXEQUERY502': 'Invalid query text: all terms were stopwords, too short, or incorrectly formatted',
        'error.code.DAHGETQUERYTAGVALUES512': 'Find did not understand your search text',
        'error.code.AXEGETQUERYTAGVALUES512': 'Find did not understand your search text',
        'error.code.DAHQUERY512': 'Find did not understand your search text',
        'error.code.AXEQUERY512': 'Find did not understand your search text'
    });
});
