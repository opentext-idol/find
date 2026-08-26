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

const selectLocale = require('find/nls/select-locale');

// Locales resolve at build time. To add one, create the directory and add a line here.
module.exports = selectLocale({
    root: require('find/idol/nls/root/snapshots'),
    'en-gb': require('find/idol/nls/en-gb/snapshots')
});
