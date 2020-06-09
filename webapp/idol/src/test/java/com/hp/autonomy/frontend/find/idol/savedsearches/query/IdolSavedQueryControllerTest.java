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

package com.hp.autonomy.frontend.find.idol.savedsearches.query;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQueryControllerTest;
import com.hp.autonomy.frontend.find.idol.dashboards.Dashboard;
import com.hp.autonomy.frontend.find.idol.dashboards.DashboardConfig;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.ResultsListWidget;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearch;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchConfig;
import com.hp.autonomy.searchcomponents.core.search.DocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolDocumentsService;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequest;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRequestBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictions;
import com.hp.autonomy.searchcomponents.idol.search.IdolQueryRestrictionsBuilder;
import com.hp.autonomy.searchcomponents.idol.search.IdolSearchResult;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.ObjectFactory;

import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IdolSavedQueryControllerTest extends SavedQueryControllerTest<IdolQueryRequest, String, IdolQueryRestrictions, IdolSearchResult, AciErrorException, IdolSavedQueryController> {
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
    private ConfigService<DashboardConfig> idolDashboardConfigService;

    @Mock
    private DashboardConfig dashboardConfig;
    private SavedQuery savedQuery;

    private List<Dashboard> mockQueryDashboard;

    @Override
    protected IdolSavedQueryController constructController() {
        when(queryRestrictionsBuilderFactory.getObject()).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.queryText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.fieldText(anyString())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.databases(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.maxDate(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.minScore(anyInt())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateMatchIds(any())).thenReturn(queryRestrictionsBuilder);
        when(queryRestrictionsBuilder.stateDontMatchIds(any())).thenReturn(queryRestrictionsBuilder);

        when(queryRequestBuilderFactory.getObject()).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryRestrictions(any())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.maxResults(anyInt())).thenReturn(queryRequestBuilder);
        when(queryRequestBuilder.queryType(any())).thenReturn(queryRequestBuilder);
        when(idolDashboardConfigService.getConfig()).thenReturn(dashboardConfig);
        when(dashboardConfig.getDashboards()).thenReturn(Collections.emptyList());

        return new IdolSavedQueryController(savedQueryService, idolDocumentsService, fieldTextParser, queryRestrictionsBuilderFactory, queryRequestBuilderFactory, idolDashboardConfigService);
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();

        savedQuery = new SavedQuery.Builder()
                .setTitle("Any old saved search")
                .setId(123L)
                .build();

        mockQueryDashboard = Collections.singletonList(
                Dashboard.builder()
                        .widget(ResultsListWidget.builder()
                                .datasource(SavedSearch.builder()
                                        .source("SavedSearch")
                                        .config(SavedSearchConfig.builder()
                                                .id(savedQuery.getId())
                                                .type(SavedSearchType.QUERY)
                                                .build())
                                        .build())
                                .build())
                        .build()
        );
    }

    @Override
    protected DocumentsService<IdolQueryRequest, ?, ?, IdolQueryRestrictions, IdolSearchResult, AciErrorException> constructDocumentsService() {
        return idolDocumentsService;
    }

    @Test
    public void get() {
        when(dashboardConfig.getDashboards()).thenReturn(mockQueryDashboard);

        when(savedQueryService.getDashboardSearch(savedQuery.getId())).thenReturn(savedQuery);

        savedQueryController.get(savedQuery.getId());
        verify(savedQueryService).getDashboardSearch(savedQuery.getId());
    }

    @Test
    public void getWhenSearchDoesNotExist() throws Exception {
        when(dashboardConfig.getDashboards()).thenReturn(mockQueryDashboard);

        when(savedQueryService.getDashboardSearch(any(long.class))).thenReturn(null);

        try {
            savedQueryController.get(savedQuery.getId());
            fail("Call to get() was expected to throw exception");
        } catch (final IllegalArgumentException e) {
            verify(savedQueryService).getDashboardSearch(savedQuery.getId());
            assertThat("Exception has the correct message",
                    e.getMessage(),
                    is("Configured ID " + savedQuery.getId() + " does not match any known Saved Query"));
        }
    }
}
