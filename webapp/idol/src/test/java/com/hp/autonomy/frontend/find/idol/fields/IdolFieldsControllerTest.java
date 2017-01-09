/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.fields;

import com.autonomy.aci.client.services.AciErrorException;
import com.google.common.collect.ImmutableList;
import com.hp.autonomy.frontend.find.core.configuration.UiCustomization;
import com.hp.autonomy.frontend.find.core.fields.AbstractFieldsControllerTest;
import com.hp.autonomy.frontend.find.core.fields.FieldAndValueDetails;
import com.hp.autonomy.searchcomponents.core.fields.FieldPathNormaliser;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequest;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsService;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.TagName;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(classes = HavenSearchIdolConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdolFieldsControllerTest extends AbstractFieldsControllerTest<IdolFieldsController, IdolFieldsRequest, AciErrorException, String, IdolQueryRestrictions, IdolParametricRequest> {
    @Mock
    private IdolFieldsService idolFieldsService;
    @Mock
    private IdolParametricValuesService idolParametricValuesService;
    @Mock
    private ObjectFactory<IdolParametricRequestBuilder> parametricRequestBuilderFactory;
    @Mock
    private IdolParametricRequestBuilder parametricRequestBuilder;
    @Mock
    private ObjectFactory<IdolFieldsRequestBuilder> fieldsRequestBuilderFactory;
    @Mock
    private IdolFieldsRequestBuilder fieldsRequestBuilder;
    @Mock
    private IdolFieldsRequest fieldsRequest;
    @Mock
    private ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;
    @Mock
    private IdolQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Override
    protected IdolFieldsController constructController() {
        when(parametricRequestBuilderFactory.getObject()).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.fieldNames(any())).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.queryRestrictions(any())).thenReturn(parametricRequestBuilder);

        when(fieldsRequestBuilderFactory.getObject()).thenReturn(fieldsRequestBuilder);
        when(fieldsRequestBuilder.build()).thenReturn(fieldsRequest);

        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.queryText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);

        return new IdolFieldsController(idolFieldsService, idolParametricValuesService, parametricRequestBuilderFactory, fieldPathNormaliser, tagNameFactory, configService, fieldsRequestBuilderFactory, queryRestrictionsBuilderFactory);
    }

    @Override
    protected IdolFieldsService constructService() {
        return idolFieldsService;
    }

    @Override
    protected IdolParametricValuesService constructParametricValuesService() {
        return idolParametricValuesService;
    }

    @Override
    protected List<TagName> getParametricFields() {
        return controller.getParametricFields();
    }

    @Override
    protected List<FieldAndValueDetails> getParametricDateFields() {
        return controller.getParametricDateFields();
    }

    @Override
    protected List<FieldAndValueDetails> getParametricNumericFields() {
        return controller.getParametricNumericFields();
    }

    @Test
    public void getParametricFieldsWithAlwaysShowList() {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricAlwaysShow(Arrays.asList("ParametricField1", "DOCUMENT/ParametricField2"))
                .build());

        final List<TagName> fields = getParametricFields();
        assertThat(fields, hasSize(2));
        assertThat(fields, hasItem(is(tagNameFactory.buildTagName("/DOCUMENT/ParametricField1"))));
        assertThat(fields, hasItem(is(tagNameFactory.buildTagName("/DOCUMENT/ParametricField2"))));
    }

    @Test
    public void getParametricFieldsWithNeverShowList() {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricNeverShow(Arrays.asList("ParametricField1", "DOCUMENT/ParametricField2"))
                .build());

        final List<TagName> fields = getParametricFields();
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(tagNameFactory.buildTagName("/DOCUMENT/ParametricField3"))));
    }

    @Test
    public void getParametricFieldsWithAlwaysShowListAndNeverShowList() {
        mockSimpleParametricResponse();

        when(config.getUiCustomization()).thenReturn(UiCustomization.builder()
                .parametricAlwaysShow(Arrays.asList("ParametricField1", "DOCUMENT/ParametricField2"))
                .parametricNeverShow(Collections.singletonList("ParametricField1"))
                .build());

        final List<TagName> fields = getParametricFields();
        assertThat(fields, hasSize(1));
        assertThat(fields, hasItem(is(tagNameFactory.buildTagName("/DOCUMENT/ParametricField2"))));
    }

    private void mockSimpleParametricResponse() {
        final Map<FieldTypeParam, List<TagName>> response = new EnumMap<>(FieldTypeParam.class);
        response.put(FieldTypeParam.Numeric, Collections.emptyList());
        response.put(FieldTypeParam.NumericDate, Collections.emptyList());
        response.put(FieldTypeParam.Parametric, ImmutableList.of(tagNameFactory.buildTagName("DOCUMENT/ParametricField1"), tagNameFactory.buildTagName("DOCUMENT/ParametricField2"), tagNameFactory.buildTagName("DOCUMENT/ParametricField3")));
        when(service.getFields(any(), eq(FieldTypeParam.Parametric), eq(FieldTypeParam.Numeric), eq(FieldTypeParam.NumericDate))).thenReturn(response);
    }
}
