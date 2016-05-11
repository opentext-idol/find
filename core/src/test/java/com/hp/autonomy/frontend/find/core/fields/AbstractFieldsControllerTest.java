/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractFieldsControllerTest<R extends FieldsRequest, E extends Exception> {
    @Mock
    protected FieldsService<R, E> service;

    protected FieldsController<R, E> controller;

    protected abstract R createRequest();

    @Test
    public void getParametricFields() throws E {
        when(service.getParametricFields(Matchers.<R>any())).thenReturn(Collections.singletonList("SomeName"));
        assertThat(controller.getParametricFields(createRequest()), hasSize(1));
    }
}
