/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
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
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.ValueDetails;
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
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;

import java.io.Serializable;
import java.util.Collections;
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

    @Autowired
    protected TagNameFactory tagNameFactory;

    protected F config;
    protected C controller;
    private FieldsService<R, E> service;
    private ParametricValuesService<P, Q, E> parametricValuesService;

    protected abstract C constructController();

    protected abstract FieldsService<R, E> constructService();

    protected abstract ParametricValuesService<P, Q, E> constructParametricValuesService();

    protected abstract List<FieldAndValueDetails> getParametricFields(final FieldTypeParam... fieldTypes) throws E;

    protected abstract F mockConfig();

    @Before
    public void setUp() throws E {
        config = mockConfig();
        when(configService.getConfig()).thenReturn(config);
        when(config.getUiCustomization()).thenReturn(UiCustomization.builder().parametricAlwaysShow(Collections.emptyList()).build());

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

        final List<FieldAndValueDetails> fields = getParametricFields(FieldTypeParam.Parametric, FieldTypeParam.Numeric, FieldTypeParam.NumericDate);
        assertThat(fields, hasSize(6));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.getFieldPath("parametric_field").getNormalisedPath(), "Parametric Field", 0d, 0d, 0L, FieldTypeParam.Parametric))));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.getFieldPath("numeric_field").getNormalisedPath(), "Numeric Field", 0d, 0d, 0L, FieldTypeParam.Numeric))));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.getFieldPath("parametric_numeric_field").getNormalisedPath(), "Parametric Numeric Field", 0d, 0d, 0L, FieldTypeParam.Numeric))));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.getFieldPath("date_field").getNormalisedPath(), "Date Field", 0d, 0d, 0L, FieldTypeParam.NumericDate))));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.getFieldPath("parametric_date_field").getNormalisedPath(), "Parametric Date Field", 0d, 0d, 0L, FieldTypeParam.NumericDate))));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.getFieldPath(ParametricValuesService.AUTN_DATE_FIELD).getNormalisedPath(), "Autn Date", 0d, 0d, 0L, FieldTypeParam.NumericDate))));
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

        final List<FieldAndValueDetails> output = getParametricFields(FieldTypeParam.NumericDate);
        assertThat(output, is(empty()));
    }

    @Test
    public void getParametricNumericFieldsTest() throws E {
        final String fieldName = "parametric_numeric_field";

        final Map<FieldTypeParam, Set<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, ImmutableSet.of(tagNameFactory.buildTagName(fieldName)));
        when(service.getFields(any())).thenReturn(response);

        final ValueDetails valueDetails = new ValueDetails.Builder()
                .setMin(1.4)
                .setMax(2.5)
                .setAverage(1.9)
                .setSum(10.8)
                .setTotalValues(25)
                .build();

        final Map<TagName, ValueDetails> valueDetailsOutput = ImmutableMap.<TagName, ValueDetails>builder()
                .put(tagNameFactory.buildTagName(fieldName), valueDetails)
                .build();

        when(parametricValuesService.getValueDetails(any())).thenReturn(valueDetailsOutput);

        final List<FieldAndValueDetails> fields = getParametricFields(FieldTypeParam.Numeric);
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.getFieldPath("parametric_numeric_field").getNormalisedPath(), "Parametric Numeric Field", 1.4, 2.5, 25, FieldTypeParam.Numeric))));
    }

    @Test
    public void getParametricDateFieldsTest() throws E {
        final Map<FieldTypeParam, Set<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.NumericDate, ImmutableSet.of(tagNameFactory.buildTagName("parametric_date_field")));
        when(service.getFields(any())).thenReturn(response);

        final ValueDetails valueDetails = new ValueDetails.Builder()
                .setMin(146840000d)
                .setMax(146860000d)
                .setAverage(146850000d)
                .setSum(1046850000d)
                .setTotalValues(1000)
                .build();

        final ValueDetails autnDateValueDetails = new ValueDetails.Builder()
                .setMin(100000000d)
                .setMax(150000000d)
                .setAverage(130000000d)
                .setSum(1050000000d)
                .setTotalValues(15000)
                .build();

        final Map<TagName, ValueDetails> valueDetailsOutput = ImmutableMap.<TagName, ValueDetails>builder()
                .put(tagNameFactory.buildTagName("parametric_date_field"), valueDetails)
                .put(tagNameFactory.buildTagName(ParametricValuesService.AUTN_DATE_FIELD), autnDateValueDetails)
                .build();

        when(parametricValuesService.getValueDetails(any())).thenReturn(valueDetailsOutput);

        final List<FieldAndValueDetails> fields = getParametricFields(FieldTypeParam.NumericDate);
        assertThat(fields, hasSize(2));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.getFieldPath("parametric_date_field").getNormalisedPath(), "Parametric Date Field", 146840000d, 146860000d, 1000, FieldTypeParam.NumericDate))));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.getFieldPath(ParametricValuesService.AUTN_DATE_FIELD).getNormalisedPath(), "Autn Date", 100000000d, 150000000d, 15000, FieldTypeParam.NumericDate))));
    }

    @Test
    public void getParametricFieldsWithAlwaysShowList() throws E {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricAlwaysShowItem(tagNameFactory.getFieldPath("ParametricField1"))
                .parametricAlwaysShowItem(tagNameFactory.getFieldPath("ParametricField2"))
                .build());

        final List<FieldAndValueDetails> fields = getParametricFields(FieldTypeParam.Parametric);
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

        final List<FieldAndValueDetails> fields = getParametricFields(FieldTypeParam.Parametric);
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

        final List<FieldAndValueDetails> fields = getParametricFields(FieldTypeParam.Parametric);
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath()))));
    }

    @Test
    public void getParametricFieldsWithDefaultSorting() throws E {
        mockSimpleParametricResponse();

        final List<FieldAndValueDetails> fields = getParametricFields(FieldTypeParam.Parametric);
        assertThat(fields.get(0), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath())));
        assertThat(fields.get(1), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath())));
        assertThat(fields.get(2), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath())));
    }

    @Test
    public void getParametricFieldsWithExplicitOrder() throws E {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField3"))
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField2"))
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField1"))
                .build());

        final List<FieldAndValueDetails> fields = getParametricFields(FieldTypeParam.Parametric);
        assertThat(fields.get(0), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath())));
        assertThat(fields.get(1), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath())));
        assertThat(fields.get(2), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath())));
    }

    @Test
    public void getParametricFieldsWithSomeExplicitOrdering() throws E {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricOrderItem(tagNameFactory.getFieldPath("ParametricField3"))
                .build());

        final List<FieldAndValueDetails> fields = getParametricFields(FieldTypeParam.Parametric);
        assertThat(fields.get(0), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField3").getNormalisedPath())));
        assertThat(fields.get(1), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField1").getNormalisedPath())));
        assertThat(fields.get(2), hasProperty("id", is(tagNameFactory.getFieldPath("ParametricField2").getNormalisedPath())));
    }

    private void mockSimpleParametricResponse() throws E {
        final Map<FieldTypeParam, Set<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, Collections.emptySet());
        response.put(FieldTypeParam.NumericDate, Collections.emptySet());
        response.put(FieldTypeParam.Parametric, ImmutableSet.of(tagNameFactory.buildTagName("ParametricField1"), tagNameFactory.buildTagName("ParametricField2"), tagNameFactory.buildTagName("ParametricField3")));
        when(service.getFields(any())).thenReturn(response);
    }
}
