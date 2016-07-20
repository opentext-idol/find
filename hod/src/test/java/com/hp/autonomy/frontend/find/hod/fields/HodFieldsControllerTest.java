/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.fields;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.fields.AbstractFieldsControllerTest;
import com.hp.autonomy.frontend.find.core.fields.FieldAndValueDetails;
import com.hp.autonomy.frontend.find.core.fields.FieldsController;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.ValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.junit.Test;
import org.mockito.Matchers;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HodFieldsControllerTest extends AbstractFieldsControllerTest<HodFieldsRequest, HodErrorException, ResourceIdentifier, HodQueryRestrictions, HodParametricRequest> {
    @Override
    protected FieldsController<HodFieldsRequest, HodErrorException, ResourceIdentifier, HodQueryRestrictions, HodParametricRequest> constructController() {
        @SuppressWarnings("unchecked")
        final ObjectFactory<ParametricRequest.Builder<HodParametricRequest, ResourceIdentifier>> requestBuilderFactory = mock(ObjectFactory.class);

        final ParametricRequest.Builder<HodParametricRequest, ResourceIdentifier> builder = new HodParametricRequest.Builder();
        when(requestBuilderFactory.getObject()).thenReturn(builder);

        return new HodFieldsController(service, parametricValuesService, requestBuilderFactory);
    }

    @Override
    protected HodFieldsRequest createRequest() {
        return new HodFieldsRequest.Builder().setDatabases(Collections.singleton(ResourceIdentifier.WIKI_ENG)).build();
    }

    // TODO: Remove this override once we can support autn date in HOD (FIND-180)
    @Override
    @Test
    public void getParametricDateFields() throws HodErrorException {
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.NumericDate, ImmutableList.of(new TagName("DateField"), new TagName("ParametricDateField")));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(new TagName("ParametricField"), new TagName("ParametricNumericField"), new TagName("ParametricDateField")));
        when(service.getFields(Matchers.<HodFieldsRequest>any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.NumericDate))).thenReturn(response);

        final ValueDetails valueDetails = new ValueDetails.Builder()
                .setMin(146840000d)
                .setMax(146860000d)
                .setAverage(146850000d)
                .setSum(1046850000d)
                .setTotalValues(1000)
                .build();

        final Map<TagName, ValueDetails> valueDetailsOutput = ImmutableMap.<TagName, ValueDetails>builder()
                .put(new TagName("ParametricDateField"), valueDetails)
                .build();

        when(parametricValuesService.getValueDetails(Matchers.<HodParametricRequest>any())).thenReturn(valueDetailsOutput);

        final List<FieldAndValueDetails> fields = controller.getParametricDateFields(createRequest());
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(new FieldAndValueDetails("ParametricDateField", "ParametricDateField", 146840000d, 146860000d, 1000))));
    }
}
