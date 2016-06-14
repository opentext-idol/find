/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;

import java.io.Serializable;
import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractParametricValuesControllerTest<C extends ParametricValuesController<Q, R, S, E>, Q extends QueryRestrictions<S>, R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {
    protected static final String PARAMETRIC_FIELD = "SomeNumericParametricField";
    protected static final int TARGET_NUMBER_OF_BUCKETS = 35;

    @Mock
    protected ParametricValuesService<R, S, E> parametricValuesService;
    @Mock
    protected ObjectFactory<QueryRestrictions.Builder<Q, S>> queryRestrictionsBuilderFactory;
    @Mock
    protected ObjectFactory<ParametricRequest.Builder<R, S>> parametricRequestBuilderFactory;

    protected C parametricValuesController;

    protected abstract C newControllerInstance();

    @Before
    public void setUp() {
        parametricValuesController = newControllerInstance();
    }

    @Test
    public void getDependentParametricValues() throws E {
        parametricValuesController.getDependentParametricValues(Collections.singletonList("SomeParametricField"), "Some query text", null, Collections.<S>emptyList(), null, null, 0, null);
        verify(parametricValuesService).getDependentParametricValues(Matchers.<R>any());
    }
}
