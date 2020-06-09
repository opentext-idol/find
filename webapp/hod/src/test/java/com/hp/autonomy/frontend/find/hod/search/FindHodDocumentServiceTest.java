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

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.hod.configuration.HodConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceName;
import com.hp.autonomy.hod.client.error.HodError;
import com.hp.autonomy.hod.client.error.HodErrorCode;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesService;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentsService;
import com.hp.autonomy.searchcomponents.hod.search.HodGetContentRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.searchcomponents.hod.search.HodSuggestRequest;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FindHodDocumentServiceTest {
    @Mock
    private HodDatabasesService databasesService;

    @Mock
    private HodDocumentsService documentsService;

    @Mock
    private ObjectFactory<HodDatabasesRequestBuilder> databasesRequestBuilderFactory;

    @Mock
    private HodDatabasesRequestBuilder databasesRequestBuilder;

    @Mock
    private HodQueryRequest queryRequest;

    @Mock
    private HodQueryRequestBuilder queryRequestBuilder;

    @Mock
    private HodSuggestRequest suggestRequest;

    @Mock
    private HodGetContentRequest getContentRequest;

    @Mock
    private HodQueryRestrictions queryRestrictions;

    @Mock
    private HodQueryRestrictionsBuilder queryRestrictionsBuilder;

    @Mock
    private ConfigService<HodFindConfig> findConfigService;

    @Mock
    private HodFindConfig findConfig;

    private HodDocumentsService findDocumentsService;

    @Before
    public void setUp() {
        when(databasesRequestBuilderFactory.getObject()).thenReturn(databasesRequestBuilder);
        when(databasesRequestBuilder.publicIndexesEnabled(anyBoolean())).thenReturn(databasesRequestBuilder);
        when(queryRequest.getQueryRestrictions()).thenReturn(queryRestrictions);
        when(queryRequest.toBuilder()).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryRestrictions(any())).thenReturn(queryRequestBuilder);
        when(queryRestrictions.toBuilder()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);

        findDocumentsService = new FindHodDocumentService(databasesService, documentsService, databasesRequestBuilderFactory, findConfigService);

        final QueryManipulationConfig config = QueryManipulationConfig.builder()
            .profile("SomeProfile")
            .index("SomeIndex")
            .build();

        when(findConfig.getQueryManipulation()).thenReturn(config);
        when(findConfig.getHod()).thenReturn(HodConfig.builder().publicIndexesEnabled(true).build());
        when(findConfigService.getConfig()).thenReturn(findConfig);
    }

    @Test
    public void invalidIndexName() throws HodErrorException {
        final ResourceName goodIndex = new ResourceName("Good", "Good");
        final ResourceName badIndex = new ResourceName("bad", "bad");

        final HodError invalidIndexError = new HodError.Builder().setErrorCode(HodErrorCode.INDEX_NAME_INVALID).build();
        final HodSearchResult result = HodSearchResult.builder()
            .index(goodIndex.getName())
            .build();
        final Documents<HodSearchResult> mockedResults = new Documents<>(Collections.singletonList(result), 1, null, null, null, null);
        when(documentsService.queryTextIndex(any())).thenThrow(new HodErrorException(invalidIndexError, HttpStatus.INTERNAL_SERVER_ERROR.value())).thenReturn(mockedResults);

        final Database goodDatabase = Database.builder().name(goodIndex.getName()).domain(goodIndex.getDomain()).build();
        when(databasesService.getDatabases(any(HodDatabasesRequest.class))).thenReturn(Collections.singleton(goodDatabase));

        when(queryRestrictions.getDatabases()).thenReturn(Arrays.asList(goodIndex, badIndex));

        final Documents<HodSearchResult> results = findDocumentsService.queryTextIndex(queryRequest);
        assertThat(results.getDocuments(), hasSize(1));
        assertNotNull(results.getWarnings());
        assertThat(results.getWarnings().getInvalidDatabases(), hasSize(1));
        assertEquals(badIndex, results.getWarnings().getInvalidDatabases().iterator().next());
    }

    @Test(expected = HodErrorException.class)
    public void miscellaneousError() throws HodErrorException {
        final HodError miscellaneousError = new HodError.Builder().setErrorCode(HodErrorCode.UNKNOWN).build();
        when(documentsService.queryTextIndex(any())).thenThrow(new HodErrorException(miscellaneousError, HttpStatus.INTERNAL_SERVER_ERROR.value()));

        findDocumentsService.queryTextIndex(queryRequest);
    }

    @Test
    public void findSimilar() throws HodErrorException {
        findDocumentsService.findSimilar(suggestRequest);
        verify(documentsService).findSimilar(suggestRequest);
    }

    @Test
    public void getDocumentContent() throws HodErrorException {
        findDocumentsService.getDocumentContent(getContentRequest);
        verify(documentsService).getDocumentContent(getContentRequest);
    }

    @Test
    public void getStateToken() throws HodErrorException {
        final int maxResults = 5;
        final boolean promotions = false;
        findDocumentsService.getStateToken(queryRestrictions, maxResults, promotions);
        verify(documentsService).getStateToken(queryRestrictions, maxResults, promotions);
    }

    @Test
    public void getStateTokenAndResultCount() throws HodErrorException {
        final int maxResults = 5;
        final boolean promotions = false;
        findDocumentsService.getStateTokenAndResultCount(queryRestrictions, maxResults, promotions);
        verify(documentsService).getStateTokenAndResultCount(queryRestrictions, maxResults, promotions);
    }
}
