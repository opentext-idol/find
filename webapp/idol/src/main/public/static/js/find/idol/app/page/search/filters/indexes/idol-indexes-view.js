/*
 * Copyright 2015-2017 Open Text.
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

const IndexesView = require('find/app/page/search/filters/indexes/indexes-view');
const databaseHelper = require('databases-view/js/idol-database-helper');

module.exports = IndexesView.extend({
        databaseHelper: databaseHelper,

        getIndexCategories: function() {
            return [];
        }
    });

