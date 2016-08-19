/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.databases;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.databases.AbstractDatabasesControllerTest;
import com.hp.autonomy.searchcomponents.idol.databases.IdolDatabasesRequest;
import com.hp.autonomy.types.idol.Database;
import org.junit.Before;

public class IdolDatabasesControllerTest extends AbstractDatabasesControllerTest<Database, IdolDatabasesRequest, AciErrorException> {
    @Before
    public void setUp() {
        databasesController = new IdolDatabasesController(databasesService);
    }
}
