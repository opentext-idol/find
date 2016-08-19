/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([
    'find/app/page/search/document/preview-mode-view'
], function (PreviewViewMode) {
    'use strict';

    return PreviewViewMode.extend({
        generateDetailRoute: function () {
            var database = encodeURIComponent(this.model.get('index'));
            var reference = encodeURIComponent(this.model.get('reference'));
            return 'find/search/document/' + database + '/' + reference;
        }
    });
});
