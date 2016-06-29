/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.parametricfields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.RangeInfo;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class IdolParametricValuesControllerTest extends AbstractParametricValuesControllerTest<IdolParametricValuesController, IdolQueryRestrictions, IdolParametricRequest, String, AciErrorException> {
    @Override
    protected IdolParametricValuesController newControllerInstance() {
        return new IdolParametricValuesController(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory);
    }

    @Override
    @Before
    public void setUp() {
        when(queryRestrictionsBuilderFactory.createBuilder()).thenReturn(new IdolQueryRestrictions.Builder());
        when(parametricRequestBuilderFactory.getObject()).thenReturn(new IdolParametricRequest.Builder());
        super.setUp();
    }

    @Test
    public void getParametricValues() throws AciErrorException {
        parametricValuesController.getParametricValues(Collections.singletonList("SomeParametricField"));
        verify(parametricValuesService).getAllParametricValues(Matchers.<IdolParametricRequest>any());
    }

    @Test
    public void getNumericParametricValuesInBuckets() throws AciErrorException {
        callGetNumericParametricValuesInBuckets(null, null);

        final HashMap<String, BucketingParams> expectedBucketingParamsPerField = new HashMap<>();
        expectedBucketingParamsPerField.put(PARAMETRIC_FIELD, new BucketingParams(TARGET_NUMBER_OF_BUCKETS, null, null));

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<IdolParametricRequest>any(), eq(expectedBucketingParamsPerField));
    }

    @Test
    public void getNumericParametricValuesInBucketsWithoutMin() throws AciErrorException {
        callGetNumericParametricValuesInBuckets(null, Collections.singletonList(2.5));

        final HashMap<String, BucketingParams> expectedBucketingParamsPerField = new HashMap<>();
        expectedBucketingParamsPerField.put(PARAMETRIC_FIELD, new BucketingParams(TARGET_NUMBER_OF_BUCKETS, null, 2.5));

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<IdolParametricRequest>any(), eq(expectedBucketingParamsPerField));
    }

    @Test
    public void getNumericParametricValuesInBucketsWithoutMax() throws AciErrorException {
        callGetNumericParametricValuesInBuckets(Collections.singletonList(1.5), null);

        final HashMap<String, BucketingParams> expectedBucketingParamsPerField = new HashMap<>();
        expectedBucketingParamsPerField.put(PARAMETRIC_FIELD, new BucketingParams(TARGET_NUMBER_OF_BUCKETS, 1.5, null));

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<IdolParametricRequest>any(), eq(expectedBucketingParamsPerField));
    }

    @Test
    public void getNumericParametricValuesInBucketsForField() {
        when(parametricValuesService.getNumericParametricValuesInBuckets(Matchers.<IdolParametricRequest>any(), Matchers.<Map<String, BucketingParams>>any()))
                .thenReturn(Collections.singletonList(mock(RangeInfo.class)));

        parametricValuesController.getNumericParametricValuesInBucketsForField("SomeField", 30, 10.0, 40.0);

        verify(parametricValuesService).getNumericParametricValuesInBuckets(Matchers.<IdolParametricRequest>any(), Matchers.<Map<String, BucketingParams>>any());
    }

    private void callGetNumericParametricValuesInBuckets(final List<Double> bucketMin, final List<Double> bucketMax) throws AciErrorException {
        parametricValuesController.getNumericParametricValuesInBuckets(
                Collections.singletonList("SomeNumericParametricField"),
                Collections.singletonList(35),
                bucketMin,
                bucketMax
        );
    }
}
