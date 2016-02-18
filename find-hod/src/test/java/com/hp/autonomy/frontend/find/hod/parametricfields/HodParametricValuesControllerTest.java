/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.frontend.find.core.parametricfields.ParametricValues;
import com.hp.autonomy.frontend.find.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.TagResponse;
import org.hamcrest.beans.HasPropertyWithValue;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodParametricValuesControllerTest extends AbstractParametricValuesControllerTest<HodParametricRequest, ResourceIdentifier, HodErrorException> {
    @Mock
    private FieldsService<HodFieldsRequest, HodErrorException> fieldsService;

    @Before
    public void setUp() {
        parametricValuesController = new HodParametricValuesController(parametricValuesService, new HodQueryRestrictionsBuilder(), fieldsService);
    }

    @Test
    public void getParametricValues() throws HodErrorException {
        final TagResponse response = mock(TagResponse.class);
        when(response.getParametricTypeFields()).thenReturn(Arrays.asList("field1", "field2"));
        when(response.getNumericTypeFields()).thenReturn(Arrays.asList("field2", "field3"));
        when(fieldsService.getFields(any(HodFieldsRequest.class), anyListOf(String.class))).thenReturn(response);

        final QueryTagInfo parametricResponse = mock(QueryTagInfo.class);
        when(parametricValuesService.getAllParametricValues(argThat(new HasPropertyWithValue<HodParametricRequest>("fieldNames", hasItems("field1"))))).thenReturn(Collections.singleton(parametricResponse));
        final QueryTagInfo numericResponse = mock(QueryTagInfo.class);
        when(parametricValuesService.getAllParametricValues(argThat(new HasPropertyWithValue<HodParametricRequest>("fieldNames", hasItems("field2"))))).thenReturn(Collections.singleton(numericResponse));

        final ParametricValues parametricValues = parametricValuesController.getParametricValues("Some query text", null, Collections.<ResourceIdentifier>emptyList(), null, null);
        assertThat(parametricValues.getParametricValues(), hasSize(1));
        assertThat(parametricValues.getParametricValues(), hasItem(parametricResponse));

        assertThat(parametricValues.getNumericParametricValues(), hasSize(1));
        assertThat(parametricValues.getNumericParametricValues(), hasItem(numericResponse));
    }
}
