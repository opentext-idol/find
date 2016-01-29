/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.search.AbstractRelatedConceptsControllerTest;
import com.hp.autonomy.types.idol.QsElement;
import org.junit.Before;

public class IdolRelatedConceptsControllerTest extends AbstractRelatedConceptsControllerTest<QsElement, String, AciErrorException> {
    @Before
    public void setUp() {
        relatedConceptsController = new IdolRelatedConceptsController(relatedConceptsService, new IdolQueryRestrictionsBuilder());
    }
}
