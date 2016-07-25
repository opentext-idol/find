/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodParametricValuesControllerTest extends AbstractParametricValuesControllerTest<HodParametricValuesController, HodQueryRestrictions, HodParametricRequest, ResourceIdentifier, HodErrorException> {
    @Override
    protected HodParametricValuesController newControllerInstance() {
        return new HodParametricValuesController(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory);
    }

    @Override
    @Before
    public void setUp() {
        when(queryRestrictionsBuilderFactory.createBuilder()).thenReturn(new HodQueryRestrictions.Builder());
        when(parametricRequestBuilderFactory.getObject()).thenReturn(new HodParametricRequest.Builder());
        super.setUp();
    }

    @Test
    public void getParametricValues() throws HodErrorException {
        parametricValuesController.getParametricValues(Collections.singletonList("SomeParametricField"), Collections.<ResourceIdentifier>emptyList());
        verify(parametricValuesService).getAllParametricValues(Matchers.<HodParametricRequest>any());
    }
}
