/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.ValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractFieldsControllerTest<R extends FieldsRequest, E extends Exception, S extends Serializable, Q extends QueryRestrictions<S>, P extends ParametricRequest<S>> {
    @Mock
    protected FieldsService<R, E> service;

    @Mock
    protected ParametricValuesService<P, S, E> parametricValuesService;

    private FieldsController<R, E, S, Q, P> controller;

    protected abstract FieldsController<R, E, S, Q, P> constructController();

    protected abstract R createRequest();

    @Before
    public void setUp() throws E {
        controller = constructController();
    }

    @Test
    public void getParametricFields() throws E {
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, ImmutableList.of(new TagName("NumericField"), new TagName("ParametricNumericField")));
        response.put(FieldTypeParam.NumericDate, ImmutableList.of(new TagName("DateField"), new TagName("ParametricDateField")));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(new TagName("ParametricField"), new TagName("ParametricNumericField"), new TagName("ParametricDateField")));
        when(service.getFields(Matchers.<R>any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.Numeric), eq(FieldTypeParam.NumericDate))).thenReturn(response);

        final List<TagName> fields = controller.getParametricFields(createRequest());
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(new TagName("ParametricField"))));
    }

    @Test
    public void getParametricNumericFields() throws E {
        final String fieldName = "ParametricNumericField";

        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, ImmutableList.of(new TagName("NumericField"), new TagName(fieldName)));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(new TagName("ParametricField"), new TagName(fieldName), new TagName("ParametricDateField")));
        when(service.getFields(Matchers.<R>any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.Numeric))).thenReturn(response);

        final ValueDetails valueDetails = new ValueDetails.Builder()
                .setMin(1.4)
                .setMax(2.5)
                .setAverage(1.9)
                .setSum(10.8)
                .setTotalValues(25)
                .build();

        final Map<TagName, ValueDetails> valueDetailsOutput = ImmutableMap.<TagName, ValueDetails>builder()
                .put(new TagName(fieldName), valueDetails)
                .build();

        when(parametricValuesService.getValueDetails(Matchers.<P>any())).thenReturn(valueDetailsOutput);

        final List<FieldAndValueDetails> fields = controller.getParametricNumericFields(createRequest());
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(new FieldAndValueDetails("ParametricNumericField", "ParametricNumericField", 1.4, 2.5, 25))));
    }

    @Test
    public void getParametricDateFields() throws E {
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.NumericDate, ImmutableList.of(new TagName("DateField"), new TagName("ParametricDateField")));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(new TagName("ParametricField"), new TagName("ParametricNumericField"), new TagName("ParametricDateField")));
        when(service.getFields(Matchers.<R>any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.NumericDate))).thenReturn(response);

        final ValueDetails valueDetails = new ValueDetails.Builder()
                .setMin(146840000d)
                .setMax(146860000d)
                .setAverage(146850000d)
                .setSum(1046850000d)
                .setTotalValues(1000)
                .build();

        final ValueDetails autnDateValueDetails = new ValueDetails.Builder()
                .setMin(100000000d)
                .setMax(150000000d)
                .setAverage(130000000d)
                .setSum(1050000000d)
                .setTotalValues(15000)
                .build();

        final Map<TagName, ValueDetails> valueDetailsOutput = ImmutableMap.<TagName, ValueDetails>builder()
                .put(new TagName("ParametricDateField"), valueDetails)
                .put(new TagName(ParametricValuesService.AUTN_DATE_FIELD), autnDateValueDetails)
                .build();

        when(parametricValuesService.getValueDetails(Matchers.<P>any())).thenReturn(valueDetailsOutput);

        final List<FieldAndValueDetails> fields = controller.getParametricDateFields(createRequest());
        assertThat(fields, hasSize(2));
        assertThat(fields, hasItem(is(new FieldAndValueDetails("ParametricDateField", "ParametricDateField", 146840000d, 146860000d, 1000))));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(ParametricValuesService.AUTN_DATE_FIELD, ParametricValuesService.AUTN_DATE_FIELD, 100000000d, 150000000d, 15000))));
    }
}
