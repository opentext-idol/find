/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define(['underscore'], function(_) {
    'use strict';

    function constructDatabaseString(domain, index) {
        return _.map([domain, index], encodeURIComponent).join(':');
    }

    //noinspection JSUnusedGlobalSymbols
    return {
        constructDatabaseString: constructDatabaseString,

        resolveDatabaseNameForModel: function (model) {
            return constructDatabaseString(model.get('domain'), model.get('name'));
        },

        resolveDatabaseNameForDocumentModel: function (model) {
            return constructDatabaseString(model.get('domain'), model.get('index'));
        },
        
        getDatabaseInfoFromCollection: function (databaseCollection) {
            return databaseCollection.toResourceIdentifiers();
        },
        
        getDatabaseDisplayNameFromDocumentModel: function (indexesCollection, documentModel) {
            var databaseModel = indexesCollection.find({
                name: documentModel.get('index'),
                domain: documentModel.get('domain')
            });
            return databaseModel.get('displayName');
        },

        getDatabaseDisplayNameFromDatabaseModel: function (indexesCollection, selectedDatabaseModel) {
            var databaseModel = indexesCollection.find(selectedDatabaseModel.pick('name', 'domain'));
            return databaseModel.get('displayName');
        }
    };
    
});
