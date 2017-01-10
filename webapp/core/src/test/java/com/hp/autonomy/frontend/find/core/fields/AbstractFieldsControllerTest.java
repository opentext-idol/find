/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.core.fields;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
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

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
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

    private FieldsService<R, E> service;
    private ParametricValuesService<P, Q, E> parametricValuesService;
    protected C controller;

    protected abstract C constructController();

    protected abstract FieldsService<R, E> constructService();

    protected abstract ParametricValuesService<P, Q, E> constructParametricValuesService();

    protected abstract List<TagName> getParametricFields() throws E;

    protected abstract List<FieldAndValueDetails> getParametricDateFields() throws E;

    protected abstract List<FieldAndValueDetails> getParametricNumericFields() throws E;

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
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, ImmutableList.of(tagNameFactory.buildTagName("numeric_field"), tagNameFactory.buildTagName("parametric_numeric_field")));
        response.put(FieldTypeParam.NumericDate, ImmutableList.of(tagNameFactory.buildTagName("date_field"), tagNameFactory.buildTagName("parametric_date_field")));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(tagNameFactory.buildTagName("parametric_field"), tagNameFactory.buildTagName("parametric_numeric_field"), tagNameFactory.buildTagName("parametric_date_field")));
        when(service.getFields(any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.Numeric), eq(FieldTypeParam.NumericDate))).thenReturn(response);

        final List<TagName> fields = getParametricFields();
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(tagNameFactory.buildTagName("parametric_field"))));
    }

    @Test
    public void getParametricDateFieldsWithNeverShowList() throws E {
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.NumericDate, Collections.emptyList());
        response.put(FieldTypeParam.Parametric, Collections.emptyList());
        when(service.getFields(any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.NumericDate))).thenReturn(response);

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricNeverShowItem(tagNameFactory.buildTagName(ParametricValuesService.AUTN_DATE_FIELD))
                .build());

        final List<FieldAndValueDetails> output = getParametricDateFields();
        assertThat(output, is(empty()));
    }

    @Test
    public void getParametricNumericFieldsTest() throws E {
        final String fieldName = "parametric_numeric_field";

        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, ImmutableList.of(tagNameFactory.buildTagName("numeric_field"), tagNameFactory.buildTagName(fieldName)));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(tagNameFactory.buildTagName("parametric_field"), tagNameFactory.buildTagName(fieldName), tagNameFactory.buildTagName("parametric_date_field")));
        when(service.getFields(any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.Numeric))).thenReturn(response);

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

        final List<FieldAndValueDetails> fields = getParametricNumericFields();
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.buildTagName("parametric_numeric_field").getId(), "Parametric Numeric Field", 1.4, 2.5, 25))));
    }

    @Test
    public void getParametricDateFieldsTest() throws E {
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.NumericDate, ImmutableList.of(tagNameFactory.buildTagName("date_field"), tagNameFactory.buildTagName("parametric_date_field")));
        response.put(FieldTypeParam.Parametric, ImmutableList.of(tagNameFactory.buildTagName("parametric_field"), tagNameFactory.buildTagName("parametric_numeric_field"), tagNameFactory.buildTagName("parametric_date_field")));
        when(service.getFields(any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.NumericDate))).thenReturn(response);

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

        final List<FieldAndValueDetails> fields = getParametricDateFields();
        assertThat(fields, hasSize(2));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(tagNameFactory.buildTagName("parametric_date_field").getId(), "Parametric Date Field", 146840000d, 146860000d, 1000))));
        assertThat(fields, hasItem(is(new FieldAndValueDetails(ParametricValuesService.AUTN_DATE_FIELD, "Autn Date", 100000000d, 150000000d, 15000))));
    }

    @Test
    public void getParametricFieldsWithAlwaysShowList() throws E {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricAlwaysShowItem(tagNameFactory.buildTagName("ParametricField1"))
                .parametricAlwaysShowItem(tagNameFactory.buildTagName("ParametricField2"))
                .build());

        final List<TagName> fields = getParametricFields();
        assertThat(fields, hasSize(2));
        assertThat(fields, hasItem(is(tagNameFactory.buildTagName("ParametricField1"))));
        assertThat(fields, hasItem(is(tagNameFactory.buildTagName("ParametricField2"))));
    }

    @Test
    public void getParametricFieldsWithNeverShowList() throws E {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricNeverShowItem(tagNameFactory.buildTagName("ParametricField1"))
                .parametricNeverShowItem(tagNameFactory.buildTagName("ParametricField2"))
                .build());

        final List<TagName> fields = getParametricFields();
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(tagNameFactory.buildTagName("ParametricField3"))));
    }

    @Test
    public void getParametricFieldsWithAlwaysShowListAndNeverShowList() throws E {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricAlwaysShowItem(tagNameFactory.buildTagName("ParametricField1"))
                .parametricAlwaysShowItem(tagNameFactory.buildTagName("ParametricField2"))
                .parametricNeverShowItem(tagNameFactory.buildTagName("ParametricField1"))
                .build());

        final List<TagName> fields = getParametricFields();
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(tagNameFactory.buildTagName("ParametricField2"))));
    }

    private void mockSimpleParametricResponse() throws E {
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, Collections.emptyList());
        response.put(FieldTypeParam.NumericDate, Collections.emptyList());
        response.put(FieldTypeParam.Parametric, ImmutableList.of(tagNameFactory.buildTagName("ParametricField1"), tagNameFactory.buildTagName("ParametricField2"), tagNameFactory.buildTagName("ParametricField3")));
        when(service.getFields(any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.Numeric), eq(FieldTypeParam.NumericDate))).thenReturn(response);
    }
}
