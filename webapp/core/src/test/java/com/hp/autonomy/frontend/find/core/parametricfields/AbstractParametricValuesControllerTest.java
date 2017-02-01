/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.RangeInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
public abstract class AbstractParametricValuesControllerTest<C extends ParametricValuesController<Q, R, S, E>, Q extends QueryRestrictions<S>, R extends ParametricRequest<Q>, S extends Serializable, E extends Exception> {
    @ClassRule
    public static final SpringClassRule SCR = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    protected TagNameFactory tagNameFactory;

    private ParametricValuesService<R, Q, E> parametricValuesService;
    protected C parametricValuesController;

    protected abstract C newControllerInstance();

    protected abstract ParametricValuesService<R, Q, E> newParametricValuesService();

    @Before
    public void setUp() {
        parametricValuesController = newControllerInstance();
        parametricValuesService = newParametricValuesService();
    }

    @Test
    public void getDependentParametricValues() throws E {
        parametricValuesController.getDependentParametricValues(Collections.singletonList(tagNameFactory.buildTagName("SomeParametricField")), "Some query text", null, Collections.emptyList(), null, null, 0, null);
        verify(parametricValuesService).getDependentParametricValues(Matchers.any());
    }

    @Test
    public void getParametricValuesInBuckets() throws UnsupportedEncodingException, E {
        final String fieldName = "birth&death";

        final RangeInfo rangeInfo = mock(RangeInfo.class);
        final BucketingParams expectedBucketingParams = new BucketingParams(5, -0.5, 0.5);

        when(parametricValuesService.getNumericParametricValuesInBuckets(Matchers.any(), Matchers.any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            final Map<TagName, BucketingParams> bucketingParamsPerField = invocation.getArgumentAt(1, Map.class);

            final BucketingParams bucketingParams = bucketingParamsPerField.get(tagNameFactory.buildTagName(fieldName));
            return expectedBucketingParams.equals(bucketingParams) ? Collections.singletonList(rangeInfo) : Collections.emptyList();
        });

        final RangeInfo output = parametricValuesController.getNumericParametricValuesInBucketsForField(
                tagNameFactory.buildTagName(fieldName),
                expectedBucketingParams.getTargetNumberOfBuckets(),
                expectedBucketingParams.getMin(),
                expectedBucketingParams.getMax(),
                "*",
                "",
                Collections.emptyList(),
                null,
                null,
                0
        );

        assertThat(output, is(rangeInfo));
    }
}
