/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([], function () {
    "use strict";

    return {
        resolveDatabaseNameForModel: function (model) {
            return encodeURIComponent(model.get('name'));
        },

        resolveDatabaseNameForDocumentModel: function (model) {
            return encodeURIComponent(model.get('index'));
        }
    }
});
