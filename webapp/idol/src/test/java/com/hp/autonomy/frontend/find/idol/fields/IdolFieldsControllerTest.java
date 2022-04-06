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

package com.hp.autonomy.frontend.find.idol.fields;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.find.core.fields.AbstractFieldsControllerTest;
import com.hp.autonomy.frontend.find.core.fields.FieldAndValueDetails;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.idol.beanconfiguration.HavenSearchIdolConfiguration;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequest;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.fields.IdolFieldsService;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequest;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.parametricvalues.IdolParametricValuesService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(classes = HavenSearchIdolConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class IdolFieldsControllerTest extends AbstractFieldsControllerTest<IdolFieldsController, IdolFieldsRequest, AciErrorException, String, IdolQueryRestrictions, IdolParametricRequest, IdolFindConfig> {
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
    @Mock
    private IdolFindConfig idolFindConfig;

    @Override
    protected IdolFieldsController constructController() {
        when(parametricRequestBuilderFactory.getObject()).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.fieldNames(any())).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.queryRestrictions(any())).thenReturn(parametricRequestBuilder);

        when(fieldsRequestBuilderFactory.getObject()).thenReturn(fieldsRequestBuilder);
        when(fieldsRequestBuilder.fieldTypes(any())).thenReturn(fieldsRequestBuilder);
        when(fieldsRequestBuilder.build()).thenReturn(fieldsRequest);

        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.queryText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);

        return new IdolFieldsController(idolFieldsService, idolParametricValuesService, parametricRequestBuilderFactory, fieldComparatorFactory, tagNameFactory, configService, fieldsRequestBuilderFactory, queryRestrictionsBuilderFactory);
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
    protected IdolFindConfig mockConfig() {
        return idolFindConfig;
    }

    @Override
    protected List<FieldAndValueDetails<?>> getParametricFields(final FieldTypeParam... fieldTypes) {
        when(fieldsRequest.getFieldTypes()).thenReturn(Arrays.asList(fieldTypes));
        return controller.getParametricFields(Arrays.asList(fieldTypes));
    }
}
