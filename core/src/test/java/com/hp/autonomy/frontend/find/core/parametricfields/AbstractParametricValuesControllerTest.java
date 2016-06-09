/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
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
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractParametricValuesControllerTest<Q extends QueryRestrictions<S>, R extends ParametricRequest<S>, S extends Serializable, E extends Exception> {
    private static final String PARAMETRIC_FIELD = "SomeNumericParametricField";
    private static final int TARGET_NUMBER_OF_BUCKETS = 35;

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
    public void getNumericParametricValuesInBuckets() throws E {
        callGetNumericParametricValuesInBuckets(null, null);

        final HashMap<String, BucketingParams> expectedBucketingParamsPerField = new HashMap<>();
        expectedBucketingParamsPerField.put(PARAMETRIC_FIELD, new BucketingParams(TARGET_NUMBER_OF_BUCKETS, null, null));

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<R>any(), eq(expectedBucketingParamsPerField));
    }

    @Test
    public void getNumericParametricValuesInBucketsWithoutMin() throws E {
        callGetNumericParametricValuesInBuckets(null, Collections.singletonList(2.5));

        final HashMap<String, BucketingParams> expectedBucketingParamsPerField = new HashMap<>();
        expectedBucketingParamsPerField.put(PARAMETRIC_FIELD, new BucketingParams(TARGET_NUMBER_OF_BUCKETS, null, 2.5));

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<R>any(), eq(expectedBucketingParamsPerField));
    }

    @Test
    public void getNumericParametricValuesInBucketsWithoutMax() throws E {
        callGetNumericParametricValuesInBuckets(Collections.singletonList(1.5), null);

        final HashMap<String, BucketingParams> expectedBucketingParamsPerField = new HashMap<>();
        expectedBucketingParamsPerField.put(PARAMETRIC_FIELD, new BucketingParams(TARGET_NUMBER_OF_BUCKETS, 1.5, null));

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<R>any(), eq(expectedBucketingParamsPerField));
    }

    @Test
    public void getDependentParametricValues() throws E {
        parametricValuesController.getDependentParametricValues(Collections.singletonList("SomeParametricField"), "Some query text", null, Collections.<S>emptyList(), null, null, 0, null);
        verify(parametricValuesService).getDependentParametricValues(Matchers.<R>any());
    }

    private void callGetNumericParametricValuesInBuckets(final List<Double> bucketMin, final List<Double> bucketMax) throws E {
        parametricValuesController.getNumericParametricValuesInBuckets(
                Collections.singletonList("SomeNumericParametricField"),
                "Some query text",
                null,
                Collections.<S>emptyList(),
                null,
                null,
                0,
                null,
                Collections.singletonList(35),
                bucketMin,
                bucketMax
        );
    }
}
