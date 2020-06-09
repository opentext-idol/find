/*
 * (c) Copyright 2016 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
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
