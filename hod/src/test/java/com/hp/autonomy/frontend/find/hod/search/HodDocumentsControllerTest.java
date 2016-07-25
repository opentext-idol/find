/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.find.core.search.AbstractDocumentsControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodDocumentsControllerTest extends AbstractDocumentsControllerTest<ResourceIdentifier, HodQueryRestrictions, HodSearchResult, HodErrorException> {
    @Before
    public void setUp() {
        when(queryRestrictionsBuilderFactory.createBuilder()).thenReturn(new HodQueryRestrictions.Builder());
        documentsController = new HodDocumentsController(documentsService, queryRestrictionsBuilderFactory);
        databaseType = ResourceIdentifier.class;
    }

    @Override
    protected HodSearchResult sampleResult() {
        return new HodSearchResult.Builder().build();
    }

    @Test(expected = HodErrorException.class)
    public void getDocumentContentNotFound() throws HodErrorException {
        documentsController.getDocumentContent("Some Reference", null);
    }
}
