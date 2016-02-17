/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.Entity;
import com.hp.autonomy.hod.client.error.HodErrorException;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class HodRelatedConceptsControllerTest extends AbstractRelatedConceptsControllerTest<Entity, ResourceIdentifier, HodErrorException> {
    @Before
    public void setUp() {
        relatedConceptsController = new HodRelatedConceptsController(relatedConceptsService, new HodQueryRestrictionsBuilder());
    }
}
