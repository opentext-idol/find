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

package com.hp.autonomy.frontend.find.hod.fields;

import com.hp.autonomy.frontend.find.core.fields.AbstractFieldsControllerTest;
import com.hp.autonomy.frontend.find.core.fields.FieldAndValueDetails;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.fields.FieldsService;
import com.hp.autonomy.searchcomponents.core.parametricvalues.ParametricValuesService;
import com.hp.autonomy.searchcomponents.hod.beanconfiguration.HavenSearchHodConfiguration;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequest;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.fields.HodFieldsService;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequest;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.parametricvalues.HodParametricValuesService;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.types.requests.idol.actions.tags.params.FieldTypeParam;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@SpringBootTest(classes = HavenSearchHodConfiguration.class, properties = {"mock.authentication=false", "mock.authenticationRetriever=false"}, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class HodFieldsControllerTest extends AbstractFieldsControllerTest<HodFieldsController, HodFieldsRequest, HodErrorException, ResourceName, HodQueryRestrictions, HodParametricRequest, HodFindConfig> {
    @Mock
    private HodFieldsService hodFieldsService;
    @Mock
    private HodParametricValuesService hodParametricValuesService;
    @Mock
    private ObjectFactory<HodParametricRequestBuilder> parametricRequestBuilderFactory;

    @Mock
    private HodParametricRequestBuilder parametricRequestBuilder;

    @Mock
    private ObjectFactory<HodFieldsRequestBuilder> fieldsRequestBuilderFactory;

    @Mock
    private HodFieldsRequestBuilder fieldsRequestBuilder;

    @Mock
    private HodFieldsRequest fieldsRequest;

    @Mock
    private ObjectFactory<HodQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Mock
    private HodQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private HodFindConfig hodFindConfig;

    @Override
    protected HodFieldsController constructController() {
        when(parametricRequestBuilderFactory.getObject()).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.fieldNames(any())).thenReturn(parametricRequestBuilder);
        when(parametricRequestBuilder.queryRestrictions(any())).thenReturn(parametricRequestBuilder);

        when(fieldsRequestBuilderFactory.getObject()).thenReturn(fieldsRequestBuilder);
        when(fieldsRequestBuilder.databases(any())).thenReturn(fieldsRequestBuilder);
        when(fieldsRequestBuilder.fieldTypes(any())).thenReturn(fieldsRequestBuilder);
        when(fieldsRequestBuilder.build()).thenReturn(fieldsRequest);
        when(fieldsRequest.getDatabases()).thenReturn(Collections.singletonList(ResourceName.WIKI_ENG));

        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.queryText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);

        return new HodFieldsController(hodFieldsService, hodParametricValuesService, parametricRequestBuilderFactory, fieldComparatorFactory, tagNameFactory, configService, fieldsRequestBuilderFactory, queryRestrictionsBuilderFactory);
    }

    @Override
    protected FieldsService<HodFieldsRequest, HodErrorException> constructService() {
        return hodFieldsService;
    }

    @Override
    protected ParametricValuesService<HodParametricRequest, HodQueryRestrictions, HodErrorException> constructParametricValuesService() {
        return hodParametricValuesService;
    }

    @Override
    protected HodFindConfig mockConfig() {
        return hodFindConfig;
    }

    @Override
    protected List<FieldAndValueDetails<?>> getParametricFields(final FieldTypeParam... fieldTypes) throws HodErrorException {
        when(fieldsRequest.getFieldTypes()).thenReturn(Arrays.asList(fieldTypes));
        return controller.getParametricFields(Arrays.asList(fieldTypes), Collections.singleton(ResourceName.WIKI_ENG));
    }
}
