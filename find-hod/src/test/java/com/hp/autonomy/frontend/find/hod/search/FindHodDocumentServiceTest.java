/*
 * Copyright 2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.hod.search;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.hod.configuration.HodFindConfig;
import com.hp.autonomy.frontend.find.hod.configuration.IodConfig;
import com.hp.autonomy.hod.client.api.resource.ResourceIdentifier;
import com.hp.autonomy.hod.client.api.textindex.query.search.QueryRequestBuilder;
import com.hp.autonomy.hod.client.error.HodError;
import com.hp.autonomy.hod.client.error.HodErrorCode;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.searchcomponents.core.caching.CacheNames;
import com.hp.autonomy.searchcomponents.core.databases.DatabasesService;
import com.hp.autonomy.searchcomponents.core.search.QueryRestrictions;
import com.hp.autonomy.searchcomponents.core.search.SearchRequest;
import com.hp.autonomy.searchcomponents.hod.configuration.QueryManipulationConfig;
import com.hp.autonomy.searchcomponents.hod.databases.Database;
import com.hp.autonomy.searchcomponents.hod.databases.HodDatabasesRequest;
import com.hp.autonomy.searchcomponents.hod.search.HodDocumentServiceTest;
import com.hp.autonomy.searchcomponents.hod.search.HodQueryRestrictions;
import com.hp.autonomy.searchcomponents.hod.search.HodSearchResult;
import com.hp.autonomy.types.requests.Documents;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.HttpStatus;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FindHodDocumentServiceTest extends HodDocumentServiceTest {
    @Mock
    private DatabasesService<Database, HodDatabasesRequest, HodErrorException> databasesService;

    @Mock
    private ConfigService<HodFindConfig> findConfigService;

    @Mock
    private HodFindConfig findConfig;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache cache;

    @Override
    @Before
    public void setUp() {
        super.setUp();
        documentsService = new FindHodDocumentService(findSimilarService, findConfigService, queryTextIndexService, getContentService, authenticationInformationRetriever, databasesService, documentFieldsService, cacheManager);

        when(findConfig.getQueryManipulation()).thenReturn(new QueryManipulationConfig("SomeProfile", "SomeIndex"));
        when(findConfig.getIod()).thenReturn(new IodConfig.Builder().setPublicIndexesEnabled(true).build());
        when(findConfigService.getConfig()).thenReturn(findConfig);
    }

    @Test
    public void invalidIndexName() throws HodErrorException {
        final ResourceIdentifier goodIndex = testUtils.getDatabases().get(0);
        final ResourceIdentifier badIndex = new ResourceIdentifier("bad", "bad");

        when(cacheManager.getCache(CacheNames.DATABASES)).thenReturn(cache);

        final HodError invalidIndexError = new HodError.Builder().setErrorCode(HodErrorCode.INDEX_NAME_INVALID).build();
        final HodSearchResult result = new HodSearchResult.Builder().setIndex(goodIndex.getName()).build();
        final Documents<HodSearchResult> mockedResults = new Documents<>(Collections.singletonList(result), 1, null, null, null, null);
        when(queryTextIndexService.queryTextIndexWithText(anyString(), any(QueryRequestBuilder.class))).thenThrow(new HodErrorException(invalidIndexError, HttpStatus.INTERNAL_SERVER_ERROR.value())).thenReturn(mockedResults);

        final Database goodDatabase = new Database.Builder().setName(goodIndex.getName()).setDomain(goodIndex.getDomain()).build();
        when(databasesService.getDatabases(any(HodDatabasesRequest.class))).thenReturn(Collections.singleton(goodDatabase));

        final QueryRestrictions<ResourceIdentifier> queryRestrictions = new HodQueryRestrictions.Builder()
                .setQueryText("*")
                .setDatabases(Arrays.asList(goodIndex, badIndex))
                .setAnyLanguage(true)
                .build();
        final SearchRequest<ResourceIdentifier> searchRequest = new SearchRequest<>(queryRestrictions, 1, 30, "concept", 250, null, true, false, SearchRequest.QueryType.MODIFIED);
        final Documents<HodSearchResult> results = documentsService.queryTextIndex(searchRequest);
        assertThat(results.getDocuments(), hasSize(1));
        assertNotNull(results.getWarnings());
        assertThat(results.getWarnings().getInvalidDatabases(), hasSize(1));
        assertEquals(badIndex, results.getWarnings().getInvalidDatabases().iterator().next());
        verify(cache).clear();
    }

    @Test(expected = HodErrorException.class)
    public void miscellaneousError() throws HodErrorException {
        final HodError miscellaneousError = new HodError.Builder().setErrorCode(HodErrorCode.UNKNOWN).build();
        when(queryTextIndexService.queryTextIndexWithText(anyString(), any(QueryRequestBuilder.class))).thenThrow(new HodErrorException(miscellaneousError, HttpStatus.INTERNAL_SERVER_ERROR.value()));

        final QueryRestrictions<ResourceIdentifier> queryRestrictions = testUtils.buildQueryRestrictions();
        final SearchRequest<ResourceIdentifier> searchRequest = new SearchRequest<>(queryRestrictions, 1, 30, "concept", 250, null, true, false, SearchRequest.QueryType.MODIFIED);
        documentsService.queryTextIndex(searchRequest);
    }
}
