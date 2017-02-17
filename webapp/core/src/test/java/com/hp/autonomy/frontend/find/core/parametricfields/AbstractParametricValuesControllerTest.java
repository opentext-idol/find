/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.RangeInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import lombok.Data;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest.ParametricRequestMatcher.matchesParametricRequest;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
public abstract class AbstractParametricValuesControllerTest<
        C extends ParametricValuesController<Q, R, S, E>,
        PS extends ParametricValuesService<R, Q, E>,
        Q extends QueryRestrictions<S>,
        QB extends QueryRestrictionsBuilder<Q, S, QB>,
        R extends ParametricRequest<Q>,
        RB extends ParametricRequestBuilder<R, Q, RB>,
        S extends Serializable,
        E extends Exception
> {
    @ClassRule
    public static final SpringClassRule SCR = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private final Function<ControllerArguments<PS, R, RB, Q, QB, S, E>, C> constructController;
    private final Supplier<PS> mockService;

    @Autowired
    private TagNameFactory tagNameFactory;

    @Autowired
    private ObjectFactory<RB> parametricRequestBuilderFactory;

    @Autowired
    private ObjectFactory<QB> queryRestrictionsBuilderFactory;

    private PS parametricValuesService;
    private C parametricValuesController;

    protected AbstractParametricValuesControllerTest(final Function<ControllerArguments<PS, R, RB, Q, QB, S, E>, C> constructController, final Supplier<PS> mockService) {
        this.constructController = constructController;
        this.mockService = mockService;
    }

    @Before
    public void setUp() {
        parametricValuesService = mockService.get();
        parametricValuesController = constructController.apply(new ControllerArguments<>(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory));
    }

    @Test
    public void getParametricValues() throws E {
        final List<TagName> fieldNames = Stream.of("CATEGORY", "AUTHOR").map(tagNameFactory::buildTagName).collect(Collectors.toList());

        parametricValuesController.getParametricValues(
                fieldNames,
                1,
                10,
                "cat",
                "MATCH{ANIMAL}:CATEGORY",
                Collections.emptyList(),
                null,
                null,
                null,
                null
        );

        verify(parametricValuesService).getParametricValues(argThat(matchesParametricRequest(fieldNames, "cat", "MATCH{ANIMAL}:CATEGORY")));
    }

    @Test
    public void getDependentParametricValues() throws E {
        parametricValuesController.getDependentParametricValues(
                Collections.singletonList(tagNameFactory.buildTagName("SomeParametricField")),
                "Some query text",
                null,
                Collections.emptyList(),
                null,
                null,
                0,
                null
        );

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
            return expectedBucketingParams.equals(bucketingParams)
                    ? Collections.singletonList(rangeInfo)
                    : Collections.emptyList();
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

    @Data
    public static class ControllerArguments<
            PS extends ParametricValuesService<R, Q, E>,
            R extends ParametricRequest<Q>,
            RB extends ParametricRequestBuilder<R, Q, RB>,
            Q extends QueryRestrictions<S>,
            QB extends QueryRestrictionsBuilder<Q, S, QB>,
            S extends Serializable,
            E extends Exception
    > {
        private final PS parametricValuesService;
        private final ObjectFactory<QB> queryRestrictionsBuilderFactory;
        private final ObjectFactory<RB> parametricRequestBuilderFactory;
    }

    static class ParametricRequestMatcher<R extends ParametricRequest<?>> extends BaseMatcher<R> {
        private final List<TagName> expectedFieldNames;
        private final String expectedQueryText;
        private final String expectedFieldText;

        private ParametricRequestMatcher(final List<TagName> expectedFieldNames, final String expectedQueryText, final String expectedFieldText) {
            this.expectedFieldNames = expectedFieldNames;
            this.expectedQueryText = expectedQueryText;
            this.expectedFieldText = expectedFieldText;
        }

        @Override
        public boolean matches(final Object item) {
            if (!(item instanceof ParametricRequest)){
                return false;
            }

            final ParametricRequest<?> request = (ParametricRequest<?>) item;

            return request.getFieldNames().equals(expectedFieldNames)
                    && request.getQueryRestrictions().getQueryText().equals(expectedQueryText)
                    && request.getQueryRestrictions().getFieldText().equals(expectedFieldText);
        }

        @Override
        public void describeTo(final Description description) {
            description.appendText("matchesParametricRequest(" + expectedFieldNames + ", " + expectedQueryText + ", " + expectedFieldText + ')');
        }

        static <R extends ParametricRequest<?>> ParametricRequestMatcher<R> matchesParametricRequest(
                final List<TagName> expectedFieldNames,
                final String expectedQueryText,
                final String expectedFieldText
        ) {
            return new ParametricRequestMatcher<>(expectedFieldNames, expectedQueryText, expectedFieldText);
        }
    }
}
