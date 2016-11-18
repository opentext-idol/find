/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.hod.configuration.HodConfig;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.error.HodError;
import com.hp.autonomy.hod.client.error.HodErrorCode;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.core.search.GetContentRequest;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.QueryRequest;
import com.hp.autonomy.searchcomponents.core.search.SuggestRequest;
import com.hp.autonomy.searchcomponents.core.test.TestUtils;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.searchcomponents.hod.test.HodTestUtils;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FindHodDocumentServiceTest {
    @Mock
    private DatabasesService<Database, HodDatabasesRequest, HodErrorException> databasesService;

    @Mock
    private DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> documentsService;

    @Mock
    private ConfigService<HodFindConfig> findConfigService;

    @Mock
    private HodFindConfig findConfig;

    private DocumentsService<ResourceIdentifier, HodSearchResult, HodErrorException> findDocumentsService;
    private final TestUtils<ResourceIdentifier> testUtils = new HodTestUtils();

    @Before
    public void setUp() {
        findDocumentsService = new FindHodDocumentService(databasesService, documentsService, findConfigService);

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
        final ResourceIdentifier goodIndex = testUtils.getDatabases().get(0);
        final ResourceIdentifier badIndex = new ResourceIdentifier("bad", "bad");

        final HodError invalidIndexError = new HodError.Builder().setErrorCode(HodErrorCode.INDEX_NAME_INVALID).build();
        final HodSearchResult result = HodSearchResult.builder()
                .index(goodIndex.getName())
                .build();
        final Documents<HodSearchResult> mockedResults = new Documents<>(Collections.singletonList(result), 1, null, null, null, null);
        when(documentsService.queryTextIndex(any())).thenThrow(new HodErrorException(invalidIndexError, HttpStatus.INTERNAL_SERVER_ERROR.value())).thenReturn(mockedResults);

        final Database goodDatabase = Database.builder().name(goodIndex.getName()).domain(goodIndex.getDomain()).build();
        when(databasesService.getDatabases(any(HodDatabasesRequest.class))).thenReturn(Collections.singleton(goodDatabase));

        final QueryRestrictions<ResourceIdentifier> queryRestrictions = HodQueryRestrictions.builder()
                .queryText("*")
                .databases(Arrays.asList(goodIndex, badIndex))
                .anyLanguage(true)
                .build();
        final QueryRequest<ResourceIdentifier> queryRequest = QueryRequest.<ResourceIdentifier>builder()
                .queryRestrictions(queryRestrictions)
                .start(1)
                .maxResults(30)
                .summary("concept")
                .summaryCharacters(250)
                .sort(null)
                .highlight(true)
                .autoCorrect(false)
                .queryType(QueryRequest.QueryType.MODIFIED)
                .build();
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

        final QueryRestrictions<ResourceIdentifier> queryRestrictions = testUtils.buildQueryRestrictions();
        final QueryRequest<ResourceIdentifier> queryRequest = QueryRequest.<ResourceIdentifier>builder()
                .queryRestrictions(queryRestrictions)
                .start(1)
                .maxResults(30)
                .summary("concept")
                .summaryCharacters(250)
                .sort(null)
                .highlight(true)
                .autoCorrect(false)
                .queryType(QueryRequest.QueryType.MODIFIED)
                .build();
        findDocumentsService.queryTextIndex(queryRequest);
    }

    @Test
    public void findSimilar() throws HodErrorException {
        final SuggestRequest<ResourceIdentifier> request = SuggestRequest.<ResourceIdentifier>builder().build();
        findDocumentsService.findSimilar(request);
        verify(documentsService).findSimilar(request);
    }

    @Test
    public void getDocumentContent() throws HodErrorException {
        final GetContentRequest<ResourceIdentifier> request = GetContentRequest.<ResourceIdentifier>builder().build();
        findDocumentsService.getDocumentContent(request);
        verify(documentsService).getDocumentContent(request);
    }

    @Test
    public void getStateToken() throws HodErrorException {
        final HodQueryRestrictions queryRestrictions = HodQueryRestrictions.<ResourceIdentifier>builder().build();
        final int maxResults = 5;
        final boolean promotions = false;
        findDocumentsService.getStateToken(queryRestrictions, maxResults, promotions);
        verify(documentsService).getStateToken(queryRestrictions, maxResults, promotions);
    }

    @Test
    public void getStateTokenAndResultCount() throws HodErrorException {
        final HodQueryRestrictions queryRestrictions = HodQueryRestrictions.<ResourceIdentifier>builder().build();
        final int maxResults = 5;
        final boolean promotions = false;
        findDocumentsService.getStateTokenAndResultCount(queryRestrictions, maxResults, promotions);
        verify(documentsService).getStateTokenAndResultCount(queryRestrictions, maxResults, promotions);
    }
}
