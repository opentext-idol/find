/*
 * Copyright 2014-2017 Open Text.
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

const selectLocale = require('find/nls/select-locale');

// Locales resolve at BUILD time. To add one, create the directory and add a line here.
module.exports = selectLocale({
    root: require('find/nls/root/indexes'),
    'en-gb': require('find/nls/en-gb/indexes')
});
