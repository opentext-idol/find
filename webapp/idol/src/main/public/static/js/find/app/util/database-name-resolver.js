/*
 * Copyright 2015-2016 Open Text.
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

define(function() {
    'use strict';

    //noinspection JSUnusedGlobalSymbols
    return {
        resolveDatabaseNameForModel: function (model) {
            return encodeURIComponent(model.get('name'));
        },

        resolveDatabaseNameForDocumentModel: function (model) {
            return encodeURIComponent(model.get('index'));
        },

        getDatabaseInfoFromCollection: function (selectedDatabaseCollection) {
            return selectedDatabaseCollection.map(function (model) {
                return model.pick('name');
            });
        },

        getDatabaseDisplayNameFromDocumentModel: function (indexesCollection, documentModel) {
            return documentModel.get('index');
        },

        getDatabaseDisplayNameFromDatabaseModel: function (indexesCollection, databaseModel) {
            return databaseModel.get('name');
        }
    };
    
});
