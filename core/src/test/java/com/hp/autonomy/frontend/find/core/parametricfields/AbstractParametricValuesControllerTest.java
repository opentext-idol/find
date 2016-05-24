/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
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
public abstract class AbstractParametricValuesControllerTest<Q extends QueryRestrictions<S>, R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {
    @Mock
    protected ParametricValuesService<R, S, E> parametricValuesService;
    @Mock
    protected ObjectFactory<QueryRestrictions.Builder<Q, S>> queryRestrictionsBuilderFactory;
    @Mock
    protected ObjectFactory<ParametricRequest.Builder<R, S>> parametricRequestBuilderFactory;

    protected ParametricValuesController<Q, R, S, E> parametricValuesController;

    @Test
    public void getParametricValues() throws E {
        parametricValuesController.getParametricValues(Collections.singletonList("SomeParametricField"), "Some query text", null, Collections.<S>emptyList(), null, null, 0, null);
        verify(parametricValuesService).getAllParametricValues(Matchers.<R>any());
    }

    @Test
    public void getNumericParametricValues() throws E {
        parametricValuesController.getNumericParametricValues(Collections.singletonList("SomeNumericParametricField"), "Some query text", null, Collections.<S>emptyList(), null, null, 0, null);
        verify(parametricValuesService).getNumericParametricValues(Matchers.<R>any());
    }

    @Test
    public void getDateParametricValues() throws E {
        parametricValuesController.getDateParametricValues(Collections.singletonList("SomeDateParametricField"), "Some query text", null, Collections.<S>emptyList(), null, null, 0, null);
        verify(parametricValuesService).getDateParametricValues(Matchers.<R>any());
    }

    @Test
    public void getDependentParametricValues() throws E {
        parametricValuesController.getDependentParametricValues(Collections.singletonList("SomeParametricField"), "Some query text", null, Collections.<S>emptyList(), null, null, 0, null);
        verify(parametricValuesService).getDependentParametricValues(Matchers.<R>any());
    }
}
