/*
 * Copyright 2015-2017 Open Text.
 *
 * Licensed under the MIT License (the "License"); you may not use this file
 * except in compliance with the License.
 *
 * The only warranties for products and services of Open Text and its affiliates
 * and licensors ("Open Text") are as may be set forth in the express warranty
 * statements accompanying such products and services. Nothing herein should be
 * construed as constituting an additional warranty. Open Text shall not be
 * liable for technical or editorial errors or omissions contained herein. The
 * information contained herein is subject to change without notice.
 */

package com.hp.autonomy.frontend.find.idol.search;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigFileService;
import com.hp.autonomy.frontend.configuration.authentication.CommunityPrincipal;
import com.hp.autonomy.frontend.find.core.search.AbstractDocumentsControllerTest;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.searchcomponents.idol.search.*;
import com.hp.autonomy.user.UserService;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectFactory;

import static junit.framework.TestCase.fail;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

public class IdolDocumentsControllerTest extends AbstractDocumentsControllerTest<IdolQueryRequest, IdolSuggestRequest, IdolGetContentRequest, String, IdolQueryRestrictions, IdolGetContentRequestIndex, IdolSearchResult, AciErrorException> {
    @Mock
    private IdolDocumentsService idolDocumentsService;

    @Mock
    private ObjectFactory<IdolQueryRestrictionsBuilder> queryRestrictionsBuilderFactory;

    @Mock
    private IdolQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private ObjectFactory<IdolQueryRequestBuilder> queryRequestBuilderFactory;

    @Mock
    private IdolQueryRequestBuilder queryRequestBuilder;

    @Mock
    private ObjectFactory<IdolSuggestRequestBuilder> suggestRequestBuilderFactory;

    @Mock
    private IdolSuggestRequestBuilder suggestRequestBuilder;

    @Mock
    private ObjectFactory<IdolGetContentRequestBuilder> getContentRequestBuilderFactory;

    @Mock
    private IdolGetContentRequestBuilder getContentRequestBuilder;

    @Mock
    private ObjectFactory<IdolGetContentRequestIndexBuilder> getContentRequestIndexBuilderFactory;

    @Mock
    private IdolGetContentRequestIndexBuilder getContentRequestIndexBuilder;

    @Mock
    private ConfigFileService<IdolFindConfig> configFileService;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationInformationRetriever<?, CommunityPrincipal> authenticationInformationRetriever;

    @Mock
    private IdolFindConfig config;

    @Before
    public void setUp() {
        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.queryText(anyString())).thenReturn(queryRestrictionsBuilder);
        Mockito.lenient().when(queryRestrictionsBuilder.fieldText(any())).thenReturn(queryRestrictionsBuilder);
        Mockito.lenient().when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);
        Mockito.lenient().when(queryRestrictionsBuilder.minDate(any())).thenReturn(queryRestrictionsBuilder);
        Mockito.lenient().when(queryRestrictionsBuilder.maxDate(any())).thenReturn(queryRestrictionsBuilder);
        Mockito.lenient().when(queryRestrictionsBuilder.minScore(any())).thenReturn(queryRestrictionsBuilder);
        Mockito.lenient().when(queryRestrictionsBuilder.stateMatchIds(any())).thenReturn(queryRestrictionsBuilder);
        Mockito.lenient().when(queryRestrictionsBuilder.stateDontMatchIds(any())).thenReturn(queryRestrictionsBuilder);

        when(queryRequestBuilderFactory.getObject()).thenReturn(queryRequestBuilder);
        mockSearchRequestBuilder(queryRequestBuilder);
        when(queryRequestBuilder.autoCorrect(anyBoolean())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryType(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.summary(anyString())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.sort(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.intentBasedRanking(anyBoolean())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.referenceField(any())).thenReturn(queryRequestBuilder);

        when(suggestRequestBuilderFactory.getObject()).thenReturn(suggestRequestBuilder);
        when(suggestRequestBuilder.reference(any())).thenReturn(suggestRequestBuilder);
        mockSearchRequestBuilder(suggestRequestBuilder);
        when(suggestRequestBuilder.summary(anyString())).thenReturn(suggestRequestBuilder);
        when(suggestRequestBuilder.sort(anyString())).thenReturn(suggestRequestBuilder);
        when(suggestRequestBuilder.referenceField(any())).thenReturn(suggestRequestBuilder);

        when(getContentRequestBuilderFactory.getObject()).thenReturn(getContentRequestBuilder);
        when(getContentRequestIndexBuilderFactory.getObject()).thenReturn(getContentRequestIndexBuilder);
        when(getContentRequestIndexBuilder.index(any())).thenReturn(getContentRequestIndexBuilder);
        when(getContentRequestIndexBuilder.reference(any())).thenReturn(getContentRequestIndexBuilder);
        when(getContentRequestBuilder.indexAndReferences(any())).thenReturn(getContentRequestBuilder);
        when(getContentRequestBuilder.print(any())).thenReturn(getContentRequestBuilder);
        when(getContentRequestBuilder.referenceField(any())).thenReturn(getContentRequestBuilder);

        when(configFileService.getConfig()).thenReturn(config);

        when(config.getReferenceField()).thenReturn("refField");

        documentsController = new IdolDocumentsController(idolDocumentsService, queryRestrictionsBuilderFactory, queryRequestBuilderFactory, suggestRequestBuilderFactory, getContentRequestBuilderFactory, getContentRequestIndexBuilderFactory, configFileService, authenticationInformationRetriever, userService);
        documentsService = idolDocumentsService;
        databaseType = String.class;
    }

    @Override
    protected IdolSearchResult sampleResult() {
        return IdolSearchResult.builder().build();
    }

    @Override
    protected String getSort() {
        return "DocumentCount";
    }

    @Test
    public void getDocumentContentNotFound() throws AciErrorException {
        final String reference = "Some Reference";
        try {
            documentsController.getDocumentContent(reference, null);
            fail("Exception should have been thrown");
        } catch(final AciErrorException e) {
            assertThat("Exception has the correct message",
                       e.getMessage(),
                       containsString("No content found for document with reference " + reference));
        }
    }
}
