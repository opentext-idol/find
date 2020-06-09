/*
 * (c) Copyright 2015 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.core.fields;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.find.core.configuration.FindConfig;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.searchcomponents.core.fields.FieldsRequest;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.core.fields.TagNameFactory;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricRequest;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.types.requests.idol.actions.tags.DateValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.FieldPath;
import com.hp.autonomy.types.requests.idol.actions.tags.NumericValueDetails;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.Serializable;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@JsonTest
@AutoConfigureJsonTesters(enabled = false)
public abstract class AbstractFieldsControllerTest<C extends FieldsController<R, E, Q, P>, R extends FieldsRequest, E extends Exception, S extends Serializable, Q extends QueryRestrictions<S>, P extends ParametricRequest<Q>, F extends FindConfig<?, ?>> {
    @ClassRule
    public static final SpringClassRule SCR = new SpringClassRule();
    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Mock
    protected ConfigFileService<F> configService;

    @MockBean
    protected FieldComparatorFactory fieldComparatorFactory;

    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    protected TagNameFactory tagNameFactory;

    protected F config;
    protected C controller;
    private FieldsService<R, E> service;
    private ParametricValuesService<P, Q, E> parametricValuesService;

    protected abstract C constructController();

    protected abstract FieldsService<R, E> constructService();

    protected abstract ParametricValuesService<P, Q, E> constructParametricValuesService();

    protected abstract List<FieldAndValueDetails<?>> getParametricFields(final FieldTypeParam... fieldTypes) throws E;

    protected abstract F mockConfig();

    @Before
    public void setUp() throws E {
        config = mockConfig();
        when(configService.getConfig()).thenReturn(config);
        when(config.getUiCustomization()).thenReturn(UiCustomization.builder().parametricAlwaysShow(Collections.emptyList()).build());

        when(fieldComparatorFactory.parametricFieldComparator()).thenReturn(Comparator.comparing(FieldAndValueDetails::getId));

        controller = constructController();
        service = constructService();
        parametricValuesService = constructParametricValuesService();
    }

    @Test
    public void getParametricFieldsTest() throws E {
        final Map<FieldTypeParam, Set<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, ImmutableSet.of(tagNameFactory.buildTagName("numeric_field"), tagNameFactory.buildTagName("parametric_numeric_field")));
        response.put(FieldTypeParam.NumericDate, ImmutableSet.of(tagNameFactory.buildTagName("date_field"), tagNameFactory.buildTagName("parametric_date_field")));
        response.put(FieldTypeParam.Parametric, ImmutableSet.of(tagNameFactory.buildTagName("parametric_field"), tagNameFactory.buildTagName("parametric_numeric_field"), tagNameFactory.buildTagName("parametric_date_field")));
        when(service.getFields(any())).thenReturn(response);

        final List<FieldAndValueDetails<?>> fields = getParametricFields(FieldTypeParam.Parametric, FieldTypeParam.Numeric, FieldTypeParam.NumericDate);
        assertThat(fields, hasSize(6));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("parametric_field").getNormalisedPath()))));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("numeric_field").getNormalisedPath()))));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("parametric_numeric_field").getNormalisedPath()))));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("date_field").getNormalisedPath()))));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("parametric_date_field").getNormalisedPath()))));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath(ParametricValuesService.AUTN_DATE_FIELD).getNormalisedPath()))));
    }

    @Test
    public void getParametricDateFieldsWithNeverShowList() throws E {
        final Map<FieldTypeParam, Set<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.NumericDate, Collections.emptySet());
        response.put(FieldTypeParam.Parametric, Collections.emptySet());
        when(service.getFields(any())).thenReturn(response);

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricNeverShowItem(tagNameFactory.getFieldPath(ParametricValuesService.AUTN_DATE_FIELD))
                .build());

        final List<FieldAndValueDetails<?>> output = getParametricFields(FieldTypeParam.NumericDate);
        assertThat(output, is(empty()));
    }

    @Test
    public void getParametricNumericFieldsTest() throws E {
        final String fieldName = "parametric_numeric_field";

        final Map<FieldTypeParam, Set<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, ImmutableSet.of(tagNameFactory.buildTagName(fieldName)));
        when(service.getFields(any())).thenReturn(response);

        final NumericValueDetails valueDetails = NumericValueDetails.builder()
                .min(1.4)
                .max(2.5)
                .average(1.9)
                .sum(10.8)
                .totalValues(25)
                .build();

        final Map<FieldPath, NumericValueDetails> valueDetailsOutput = ImmutableMap.<FieldPath, NumericValueDetails>builder()
                .put(tagNameFactory.getFieldPath(fieldName), valueDetails)
                .build();

        when(parametricValuesService.getNumericValueDetails(any())).thenReturn(valueDetailsOutput);

        final List<FieldAndValueDetails<?>> fields = getParametricFields(FieldTypeParam.Numeric);
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(FieldAndValueDetails.<Double>builder()
                .id(tagNameFactory.getFieldPath("parametric_numeric_field").getNormalisedPath())
                .displayName("Parametric Numeric Field")
                .min(1.4)
                .max(2.5)
                .totalValues(25)
                .type(FieldTypeParam.Numeric)
                .build())));
    }

    @Test
    public void getParametricDateFieldsTest() throws E {
        final Map<FieldTypeParam, Set<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.NumericDate, ImmutableSet.of(tagNameFactory.buildTagName("parametric_date_field")));
        when(service.getFields(any())).thenReturn(response);

        final DateValueDetails valueDetails = DateValueDetails.builder()
                .min(epochToDate(146840000))
                .max(epochToDate(146860000))
                .average(epochToDate(146850000))
                .sum(1046850000)
                .totalValues(1000)
                .build();

        final DateValueDetails autnDateValueDetails = DateValueDetails.builder()
                .min(epochToDate(100000000))
                .max(epochToDate(150000000))
                .average(epochToDate(130000000))
                .sum(1050000000d)
                .totalValues(15000)
                .build();

        final Map<FieldPath, DateValueDetails> valueDetailsOutput = ImmutableMap.<FieldPath, DateValueDetails>builder()
                .put(tagNameFactory.getFieldPath("parametric_date_field"), valueDetails)
                .put(tagNameFactory.getFieldPath(ParametricValuesService.AUTN_DATE_FIELD), autnDateValueDetails)
                .build();

        when(parametricValuesService.getDateValueDetails(any())).thenReturn(valueDetailsOutput);

        final List<FieldAndValueDetails<?>> fields = getParametricFields(FieldTypeParam.NumericDate);
        assertThat(fields, hasSize(2));
        assertThat(fields, hasItem(is(FieldAndValueDetails.<ZonedDateTime>builder()
                .id(tagNameFactory.getFieldPath("parametric_date_field").getNormalisedPath())
                .displayName("Parametric Date Field")
                .min(epochToDate(146840000))
                .max(epochToDate(146860000))
                .totalValues(1000)
                .type(FieldTypeParam.NumericDate)
                .build())));
        assertThat(fields, hasItem(is(FieldAndValueDetails.<ZonedDateTime>builder()
                .id(tagNameFactory.getFieldPath(ParametricValuesService.AUTN_DATE_FIELD).getNormalisedPath())
                .displayName("Autn Date")
                .min(epochToDate(100000000))
                .max(epochToDate(150000000))
                .totalValues(15000)
                .type(FieldTypeParam.NumericDate)
                .build())));
    }

    private ZonedDateTime epochToDate(final int epoch) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epoch), ZoneOffset.UTC);
    }

    @Test
    public void getParametricFieldsWithAlwaysShowList() throws E {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricAlwaysShowItem(tagNameFactory.getFieldPath("ParametricField1"))
                .parametricAlwaysShowItem(tagNameFactory.getFieldPath("ParametricField2"))
                .build());

        final List<FieldAndValueDetails<?>> fields = getParametricFields(FieldTypeParam.Parametric);
        assertThat(fields, hasSize(2));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath()))));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath()))));
    }

    @Test
    public void getParametricFieldsWithNeverShowList() throws E {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricNeverShowItem(tagNameFactory.getFieldPath("ParametricField1"))
                .parametricNeverShowItem(tagNameFactory.getFieldPath("ParametricField2"))
                .build());

        final List<FieldAndValueDetails<?>> fields = getParametricFields(FieldTypeParam.Parametric);
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath()))));
    }

    @Test
    public void getParametricFieldsWithAlwaysShowListAndNeverShowList() throws E {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricAlwaysShowItem(tagNameFactory.getFieldPath("ParametricField1"))
                .parametricAlwaysShowItem(tagNameFactory.getFieldPath("ParametricField2"))
                .parametricNeverShowItem(tagNameFactory.getFieldPath("ParametricField1"))
                .build());

        final List<FieldAndValueDetails<?>> fields = getParametricFields(FieldTypeParam.Parametric);
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath()))));
    }

    private void mockSimpleParametricResponse() throws E {
        final Map<FieldTypeParam, Set<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, Collections.emptySet());
        response.put(FieldTypeParam.NumericDate, Collections.emptySet());
        response.put(FieldTypeParam.Parametric, ImmutableSet.of(tagNameFactory.buildTagName("ParametricField1"), tagNameFactory.buildTagName("ParametricField2"), tagNameFactory.buildTagName("ParametricField3")));
        when(service.getFields(any())).thenReturn(response);
    }
}
