/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Collections;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(classes = HavenSearchIdolConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdolParametricValuesControllerTest extends AbstractParametricValuesControllerTest<IdolParametricValuesController, IdolQueryRestrictions, IdolParametricRequest, String, AciErrorException> {
    @Mock
    private IdolParametricValuesService idolParametricValuesService;
    @Mock
    private ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Mock
    private IdolQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private ObjectFactory<IdolParametricRequestBuilder> parametricRequestBuilderFactory;

    @Mock
    private IdolParametricRequestBuilder parametricRequestBuilder;

    @Override
    protected IdolParametricValuesController newControllerInstance() {
        return new IdolParametricValuesController(idolParametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory);
    }

    @Override
    protected ParametricValuesService<IdolParametricRequest, IdolQueryRestrictions, AciErrorException> newParametricValuesService() {
        return idolParametricValuesService;
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
    public void getParametricValues() throws AciErrorException {
        parametricValuesController.getParametricValues(Collections.singletonList(tagNameFactory.buildTagName("SomeParametricField")));
        verify(idolParametricValuesService).getParametricValues(any());
    }
}
