/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.google.common.collect.ImmutableList;
import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.types.requests.idol.actions.tags.TagResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractFieldsControllerTest<R extends FieldsRequest, E extends Exception> {
    @Mock
    protected FieldsService<R, E> service;

    @Mock
    private TagResponse tagResponse;

    private FieldsController<R, E> controller;

    protected abstract FieldsController<R, E> constructController();

    protected abstract R createRequest();

    @Before
    public void setUp() throws E {
        when(tagResponse.getNumericTypeFields()).thenReturn(ImmutableList.of("NumericField", "ParametricNumericField"));
        when(tagResponse.getParametricTypeFields()).thenReturn(ImmutableList.of("ParametricField", "ParametricNumericField"));
        when(service.getFields(Matchers.<R>any(), anyCollectionOf(String.class))).thenReturn(tagResponse);

        controller = constructController();
    }

    @Test
    public void getParametricFields() throws E {
        final List<String> fields = controller.getParametricFields(createRequest());
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is("ParametricField")));
    }

    @Test
    public void getParametricNumericFields() throws E {
        final List<String> fields = controller.getParametricNumericFields(createRequest());
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is("ParametricNumericField")));
    }
}
