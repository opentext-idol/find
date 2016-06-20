/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.parametricfields;

import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class HodParametricValuesControllerTest extends AbstractParametricValuesControllerTest<HodParametricValuesController, HodQueryRestrictions, HodParametricRequest, ResourceIdentifier, HodErrorException> {

    @Override
    protected HodParametricValuesController newControllerInstance() {
        return new HodParametricValuesController(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory);
    }

    @Before
    public void setUp() {
        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(new HodQueryRestrictions.Builder());
        when(parametricRequestBuilderFactory.getObject()).thenReturn(new HodParametricRequest.Builder());
        super.setUp();
    }

    @Test
    public void getParametricValues() throws HodErrorException {
        parametricValuesController.getParametricValues(Collections.singletonList("SomeParametricField"), Collections.<ResourceIdentifier>emptyList());
        verify(parametricValuesService).getAllParametricValues(Matchers.<HodParametricRequest>any());
    }

    @Test
    public void getNumericParametricValuesInBuckets() throws HodErrorException {
        callGetNumericParametricValuesInBuckets(null, null);

        final HashMap<String, BucketingParams> expectedBucketingParamsPerField = new HashMap<>();
        expectedBucketingParamsPerField.put(PARAMETRIC_FIELD, new BucketingParams(TARGET_NUMBER_OF_BUCKETS, null, null));

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<HodParametricRequest>any(), eq(expectedBucketingParamsPerField));
    }

    @Test
    public void getNumericParametricValuesInBucketsWithoutMin() throws HodErrorException {
        callGetNumericParametricValuesInBuckets(null, Collections.singletonList(2.5));

        final HashMap<String, BucketingParams> expectedBucketingParamsPerField = new HashMap<>();
        expectedBucketingParamsPerField.put(PARAMETRIC_FIELD, new BucketingParams(TARGET_NUMBER_OF_BUCKETS, null, 2.5));

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<HodParametricRequest>any(), eq(expectedBucketingParamsPerField));
    }

    @Test
    public void getNumericParametricValuesInBucketsWithoutMax() throws HodErrorException {
        callGetNumericParametricValuesInBuckets(Collections.singletonList(1.5), null);

        final HashMap<String, BucketingParams> expectedBucketingParamsPerField = new HashMap<>();
        expectedBucketingParamsPerField.put(PARAMETRIC_FIELD, new BucketingParams(TARGET_NUMBER_OF_BUCKETS, 1.5, null));

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<HodParametricRequest>any(), eq(expectedBucketingParamsPerField));
    }

    private void callGetNumericParametricValuesInBuckets(final List<Double> bucketMin, final List<Double> bucketMax) throws HodErrorException {
        parametricValuesController.getNumericParametricValuesInBuckets(
                Collections.singletonList("SomeNumericParametricField"),
                Collections.<ResourceIdentifier>emptyList(),
                Collections.singletonList(35),
                bucketMin,
                bucketMax
        );
    }
}
