/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.hod.beanconfiguration.HavenSearchHodConfiguration;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricValuesService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(classes = HavenSearchHodConfiguration.class, properties = {"mock.authentication=false", "mock.authenticationRetriever=false"}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class HodParametricValuesControllerTest extends AbstractParametricValuesControllerTest<HodParametricValuesController, HodQueryRestrictions, HodParametricRequest, ResourceIdentifier, HodErrorException> {
    @Mock
    private HodParametricValuesService hodParametricValuesService;
    @Mock
    private ObjectFactory<HodQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Mock
    private HodQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private ObjectFactory<HodParametricRequestBuilder> parametricRequestBuilderFactory;

    @Mock
    private HodParametricRequestBuilder parametricRequestBuilder;

    @Override
    protected HodParametricValuesController newControllerInstance() {
        return new HodParametricValuesController(hodParametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory);
    }

    @Override
    protected ParametricValuesService<HodParametricRequest, HodQueryRestrictions, HodErrorException> newParametricValuesService() {
        return hodParametricValuesService;
    }

    @Override
    @Before
    public void setUp() {
        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.queryText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.fieldText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.maxDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minScore(anyInt())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateMatchIds(any())).thenReturn(queryRestrictionsBuilder);

        when(parametricRequestBuilderFactory.getObject()).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.fieldNames(any())).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.queryRestrictions(any())).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.maxValues(anyInt())).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.sort(any())).thenReturn(parametricRequestBuilder);

        super.setUp();
    }

    @Test
    public void getParametricValues() throws HodErrorException {
        parametricValuesController.getParametricValues(Collections.singletonList(tagNameFactory.buildTagName("SomeParametricField")), Collections.emptyList());
        verify(hodParametricValuesService).getAllParametricValues(any());
    }
}
