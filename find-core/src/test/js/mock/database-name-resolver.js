/*
 * Copyright 2016 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

define([], function () {
    return jasmine.createSpyObj('databaseNameResolver', ['constructDatabaseString', 'resolveDatabaseNameForModel', 'resolveDatabaseNameForDocumentModel']);
});
