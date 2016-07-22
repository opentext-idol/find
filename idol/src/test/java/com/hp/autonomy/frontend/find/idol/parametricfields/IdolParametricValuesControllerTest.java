/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IdolParametricValuesControllerTest extends AbstractParametricValuesControllerTest<IdolParametricValuesController, IdolQueryRestrictions, IdolParametricRequest, String, AciErrorException> {
    @Override
    protected IdolParametricValuesController newControllerInstance() {
        return new IdolParametricValuesController(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory);
    }

    @Override
    @Before
    public void setUp() {
        when(queryRestrictionsBuilderFactory.createBuilder()).thenReturn(new IdolQueryRestrictions.Builder());
        when(parametricRequestBuilderFactory.getObject()).thenReturn(new IdolParametricRequest.Builder());
        super.setUp();
    }

    @Test
    public void getParametricValues() throws AciErrorException {
        parametricValuesController.getParametricValues(Collections.singletonList("SomeParametricField"));
        verify(parametricValuesService).getAllParametricValues(Matchers.<IdolParametricRequest>any());
    }
}
