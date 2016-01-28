/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(['jquery'], function ($) {
    "use strict";

    function constructDatabaseString(domain, index) {
        return _.map([domain, index], encodeURIComponent).join(':');
    }

    return {
        constructDatabaseString: constructDatabaseString,

        resolveDatabaseNameForModel: function (model) {
            return constructDatabaseString(model.get('domain'), model.get('name'));
        },

        resolveDatabaseNameForDocumentModel: function (model) {
            return constructDatabaseString(model.get('domain'), model.get('index'));
        }
    }
});
