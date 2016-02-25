/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.Serializable;
import java.util.Collections;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractParametricValuesControllerTest<R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {
    @Mock
    protected ParametricValuesService<R, S, E> parametricValuesService;

    protected ParametricValuesController<R, S, E> parametricValuesController;

    @Test
    public void getParametricValues() throws E {
        parametricValuesController.getParametricValues(Collections.singletonList("SomeField"), "Some query text", null, Collections.<S>emptyList(), null, null, null);
        verify(parametricValuesService).getAllParametricValues(Matchers.<R>any());
    }
}
