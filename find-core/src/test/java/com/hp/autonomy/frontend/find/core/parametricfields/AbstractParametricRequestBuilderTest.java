/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.core.parametricvalues.ParametricRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Collections;

import static org.junit.Assert.assertNotNull;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractParametricRequestBuilderTest<R extends ParametricRequest<S>, S extends Serializable> {
    protected final ParametricRequestBuilder<R, S> parametricRequestBuilder;

    protected AbstractParametricRequestBuilderTest(final ParametricRequestBuilder<R, S> parametricRequestBuilder) {
        this.parametricRequestBuilder = parametricRequestBuilder;
    }

    @Test
    public void buildRequest() {
        assertNotNull(parametricRequestBuilder.buildRequest(Collections.<S>emptySet(), Collections.singleton("SomeField"), "Some query text", null));
    }
}
