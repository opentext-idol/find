/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractDocumentsControllerTest;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.junit.Before;
import org.junit.Test;

public class IdolDocumentsControllerTest extends AbstractDocumentsControllerTest<String, IdolSearchResult, AciErrorException> {
    @Before
    public void setUp() {
        documentsController = new IdolDocumentsController(documentsService, new IdolQueryRestrictionsBuilder());
        databaseType = String.class;
    }

    @Override
    protected IdolSearchResult sampleResult() {
        return new IdolSearchResult.Builder().build();
    }

    @Test(expected = AciErrorException.class)
    public void getDocumentContentNotFound() throws AciErrorException {
        documentsController.getDocumentContent("Some Reference", null);
    }
}
