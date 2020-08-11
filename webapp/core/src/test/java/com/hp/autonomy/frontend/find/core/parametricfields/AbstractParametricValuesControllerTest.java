/*
 * (c) Copyright 2015-2017 Micro Focus or one of its affiliates.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Micro Focus and its affiliates
 * and licensors ("Micro Focus") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Micro Focus shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.core.parametricfields;

import com.google.common.collect.ImmutableMap;
import com.hp.autonomy.frontend.find.core.fields.FieldComparatorFactory;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.core.parametricvalues.BucketingParams;
import com.hp.autonomy.searchcomponents.core.parametricvalues.DependentParametricField;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.DateRangeInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.DateValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import com.hp.autonomy.types.requests.idol.actions.tags.NumericRangeInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.NumericValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.QueryTagInfo;
import com.hp.autonomy.types.requests.idol.actions.tags.params.SortParam;
import lombok.Data;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hp.autonomy.frontend.find.core.parametricfields.AbstractParametricValuesControllerTest.ParametricRequestMatcher.matchesParametricRequest;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.when;

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

    @MockBean
    private FieldComparatorFactory fieldComparatorFactory;

    @SuppressWarnings("SpringJavaAutowiringInspection")
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
        when(fieldComparatorFactory.parametricFieldAndValuesComparator()).thenReturn(Comparator.comparing(QueryTagInfo::getId));

        parametricValuesService = mockService.get();
        parametricValuesController = constructController.apply(new ControllerArguments<>(parametricValuesService, queryRestrictionsBuilderFactory, parametricRequestBuilderFactory, fieldComparatorFactory));
    }

    @Test
    public void getParametricValues() throws E {
        final List<FieldPath> fieldNames = Stream.of("CATEGORY", "AUTHOR").map(tagNameFactory::getFieldPath).collect(Collectors.toList());

        when(parametricValuesService.getParametricValues(argThat(matchesParametricRequest(fieldNames, "cat", "MATCH{ANIMAL}:CATEGORY"))))
                .thenReturn(Collections.singleton(QueryTagInfo.builder().build()));
        assertThat(parametricValuesController.getParametricValues(
                fieldNames,
                1,
                10,
                null,
                "cat",
                "MATCH{ANIMAL}:CATEGORY",
                Collections.emptyList(),
                null,
                null,
                null,
                null,
                SortParam.DocumentCount
        ), not(empty()));
    }

    @Test
    public void getDependentParametricValues() throws E {
        when(parametricValuesService.getDependentParametricValues(Matchers.any())).thenReturn(Collections.singletonList(DependentParametricField.builder().build()));
        assertThat(parametricValuesController.getDependentParametricValues(
                Collections.singletonList(tagNameFactory.getFieldPath("SomeParametricField")),
                "Some query text",
                null,
                Collections.emptyList(),
                null,
                null,
                0,
                null
        ), not(empty()));
    }

    @Test
    public void getNumericValueDetails() throws E {
        final FieldPath field = tagNameFactory.getFieldPath("SomeNumericField");
        when(parametricValuesService.getNumericValueDetails(Matchers.any())).thenReturn(ImmutableMap.of(field, NumericValueDetails.builder().build()));
        assertNotNull(parametricValuesController.getNumericValueDetails(
                field,
                "Some query text",
                null, Collections.emptyList(),
                null,
                null,
                0,
                null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNumericValueDetails_missingField() throws E {
        final FieldPath field = tagNameFactory.getFieldPath("SomeNumericField");
        when(parametricValuesService.getNumericValueDetails(Matchers.any()))
            .thenReturn(Collections.emptyMap());
        parametricValuesController.getNumericValueDetails(
            field, "Some query text", null, Collections.emptyList(), null, null, 0, null);
    }

    @Test
    public void getDateValueDetails() throws E {
        final FieldPath field = tagNameFactory.getFieldPath("SomeDateField");
        when(parametricValuesService.getDateValueDetails(Matchers.any())).thenReturn(ImmutableMap.of(field, DateValueDetails.builder().build()));
        assertNotNull(parametricValuesController.getDateValueDetails(
                field,
                "Some query text",
                null, Collections.emptyList(),
                null,
                null,
                0,
                null));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getDateValueDetails_missingField() throws E {
        final FieldPath field = tagNameFactory.getFieldPath("SomeDateField");
        when(parametricValuesService.getDateValueDetails(Matchers.any()))
            .thenReturn(Collections.emptyMap());
        parametricValuesController.getDateValueDetails(
            field, "Some query text", null, Collections.emptyList(), null, null, 0, null);
    }

    @Test
    public void getNumericParametricValuesInBuckets() throws UnsupportedEncodingException, E {
        final String fieldName = "birth&death";

        final NumericRangeInfo rangeInfo = NumericRangeInfo.builder().build();
        final BucketingParams<Double> expectedBucketingParams = new BucketingParams<>(5, -0.5, 0.5);

        when(parametricValuesService.getNumericParametricValuesInBuckets(Matchers.any(), Matchers.any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked") final Map<FieldPath, BucketingParams<Double>> bucketingParamsPerField = invocation.getArgumentAt(1, Map.class);

            final BucketingParams<Double> bucketingParams = bucketingParamsPerField.get(tagNameFactory.getFieldPath(fieldName));
            return expectedBucketingParams.equals(bucketingParams)
                    ? Collections.singletonList(rangeInfo)
                    : Collections.emptyList();
        });

        final NumericRangeInfo output = parametricValuesController.getNumericParametricValuesInBucketsForField(
                tagNameFactory.getFieldPath(fieldName),
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

    @Test
    public void getDateParametricValuesInBuckets() throws UnsupportedEncodingException, E {
        final String fieldName = "birth&death";

        final DateRangeInfo rangeInfo = DateRangeInfo.builder().build();
        final BucketingParams<ZonedDateTime> expectedBucketingParams = new BucketingParams<>(5, ZonedDateTime.now().minusMinutes(5), ZonedDateTime.now());

        when(parametricValuesService.getDateParametricValuesInBuckets(Matchers.any(), Matchers.any())).thenAnswer(invocation -> {
            @SuppressWarnings("unchecked") final Map<FieldPath, BucketingParams<ZonedDateTime>> bucketingParamsPerField = invocation.getArgumentAt(1, Map.class);

            final BucketingParams<ZonedDateTime> bucketingParams = bucketingParamsPerField.get(tagNameFactory.getFieldPath(fieldName));
            return expectedBucketingParams.equals(bucketingParams)
                    ? Collections.singletonList(rangeInfo)
                    : Collections.emptyList();
        });

        final DateRangeInfo output = parametricValuesController.getDateParametricValuesInBucketsForField(
                tagNameFactory.getFieldPath(fieldName),
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
        private final FieldComparatorFactory fieldComparatorFactory;
    }

    static class ParametricRequestMatcher<R extends ParametricRequest<?>> extends BaseMatcher<R> {
        private final List<FieldPath> expectedFieldNames;
        private final String expectedQueryText;
        private final String expectedFieldText;

        private ParametricRequestMatcher(final List<FieldPath> expectedFieldNames, final String expectedQueryText, final String expectedFieldText) {
            this.expectedFieldNames = expectedFieldNames;
            this.expectedQueryText = expectedQueryText;
            this.expectedFieldText = expectedFieldText;
        }

        static <R extends ParametricRequest<?>> ParametricRequestMatcher<R> matchesParametricRequest(
                final List<FieldPath> expectedFieldNames,
                final String expectedQueryText,
                final String expectedFieldText
        ) {
            return new ParametricRequestMatcher<>(expectedFieldNames, expectedQueryText, expectedFieldText);
        }

        @Override
        public boolean matches(final Object item) {
            if (!(item instanceof ParametricRequest)) {
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
    }
}
