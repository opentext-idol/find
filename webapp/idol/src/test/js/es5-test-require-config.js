/*
 * Copyright 2015 Open Text.
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

require.config({
    baseUrl: 'target/classes/static/js',
    paths: {
        'find': '../../../es5-jasmine-test/find',
        'find/templates': 'find/templates',
        'find/idol/templates': 'find/idol/templates',
        'mock': '../../../es5-jasmine-test-specs/mock'
    }
});
