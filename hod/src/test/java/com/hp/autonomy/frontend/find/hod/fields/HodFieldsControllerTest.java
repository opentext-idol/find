/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.hp.autonomy.frontend.find.core.fields.AbstractFieldsControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import org.junit.Before;

import java.util.Collections;

public class HodFieldsControllerTest extends AbstractFieldsControllerTest<HodFieldsRequest, HodErrorException> {
    @Before
    public void setUp() {
        controller = new HodFieldsController(service);
    }

    @Override
    protected HodFieldsRequest createRequest() {
        return new HodFieldsRequest.Builder().setDatabases(Collections.singleton(ResourceIdentifier.WIKI_ENG)).build();
    }
}
