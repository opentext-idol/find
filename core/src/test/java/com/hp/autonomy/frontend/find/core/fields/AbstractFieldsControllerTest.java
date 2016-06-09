/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.google.common.collect.ImmutableList;
import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractFieldsControllerTest<R extends FieldsRequest, E extends Exception> {
    @Mock
    protected FieldsService<R, E> service;

    protected FieldsController<R, E> controller;

    protected abstract FieldsController<R, E> constructController();

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
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, ImmutableList.of(new TagName("NumericField"), new TagName("ParametricNumericField")));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(new TagName("ParametricField"), new TagName("ParametricNumericField"), new TagName("ParametricDateField")));
        when(service.getFields(Matchers.<R>any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.Numeric))).thenReturn(response);

        final List<TagName> fields = controller.getParametricNumericFields(createRequest());
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(new TagName("ParametricNumericField"))));
    }

    @Test
    public void getParametricDateFields() throws E {
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.NumericDate, ImmutableList.of(new TagName("DateField"), new TagName("ParametricDateField")));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(new TagName("ParametricField"), new TagName("ParametricNumericField"), new TagName("ParametricDateField")));
        when(service.getFields(Matchers.<R>any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.NumericDate))).thenReturn(response);
        final List<TagName> fields = controller.getParametricDateFields(createRequest());
        assertThat(fields, hasSize(2));
        assertThat(fields, hasItem(is(new TagName("ParametricDateField"))));
    }
}
