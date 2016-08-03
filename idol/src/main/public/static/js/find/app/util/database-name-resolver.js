/*
 * Copyright 2015-2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
