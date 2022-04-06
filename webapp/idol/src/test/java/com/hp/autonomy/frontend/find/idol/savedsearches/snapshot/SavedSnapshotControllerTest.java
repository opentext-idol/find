/*
 * (c) Copyright 2016-2017 Micro Focus or one of its affiliates.
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

package com.hp.autonomy.frontend.find.idol.savedsearches.snapshot;

import com.autonomy.aci.client.services.AciErrorException;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.core.savedsearches.ConceptClusterPhrase;
import com.hp.autonomy.frontend.find.core.savedsearches.EmbeddableIndex;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchType;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.idol.dashboards.Dashboard;
import com.hp.autonomy.frontend.find.idol.dashboards.DashboardConfig;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.ResultsListWidget;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearch;
import com.hp.autonomy.frontend.find.idol.dashboards.widgets.datasources.SavedSearchConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SavedSnapshotControllerTest {
    @Mock
    private SavedSnapshotService savedSnapshotService;
    @Mock
    private ConfigService<DashboardConfig> idolDashboardConfigService;
    @Mock
    private DashboardConfig dashboardConfig;
    private SavedSnapshotController savedSnapshotController;
    private SavedQuery savedQuery;
    private SavedSnapshot savedSnapshot;

    private List<Dashboard> mockSnapshotDashboard;

    @Before
    public void setUp() {
        when(idolDashboardConfigService.getConfig()).thenReturn(dashboardConfig);
        when(dashboardConfig.getDashboards()).thenReturn(Collections.emptyList());

        savedQuery = new SavedQuery.Builder()
            .setTitle("Any old saved search")
            .setId(123L)
            .setIndexes(Collections.singleton(new EmbeddableIndex("index", "domain")))
            .setConceptClusterPhrases(Collections.singleton(new ConceptClusterPhrase("*", true, -1)))
            .build();
        savedSnapshot = new SavedSnapshot.Builder()
                .setTitle("Any old saved search")
                .setId(123L)
                .setIndexes(Collections.singleton(new EmbeddableIndex("index", "domain")))
                .setConceptClusterPhrases(Collections.singleton(new ConceptClusterPhrase("*", true, -1)))
                .build();

        mockSnapshotDashboard = Collections.singletonList(
                Dashboard.builder()
                        .widget(ResultsListWidget.builder()
                                .datasource(SavedSearch.builder()
                                        .source("SavedSearch")
                                        .config(SavedSearchConfig.builder()
                                                .id(savedSnapshot.getId())
                                                .type(SavedSearchType.SNAPSHOT)
                                                .build())
                                        .build())
                                .build())
                        .build()
        );

        savedSnapshotController = new SavedSnapshotController(savedSnapshotService, idolDashboardConfigService);
    }

    @Test
    public void create() throws Exception {
        when(savedSnapshotService.build(any())).thenReturn(new SavedSnapshot());

        savedSnapshotController.create(savedQuery);
        verify(savedSnapshotService).build(savedQuery);
        verify(savedSnapshotService).create(any(SavedSnapshot.class));
    }

    @Test
    public void update() throws AciErrorException {
        when(savedSnapshotService.update(any(SavedSnapshot.class))).then(returnsFirstArg());

        final SavedSnapshot updatedQuery = savedSnapshotController.update(42, savedSnapshot);
        verify(savedSnapshotService).update(isA(SavedSnapshot.class));
        assertEquals(new Long(42L), updatedQuery.getId());
    }

    @Test
    public void getAll() {
        savedSnapshotController.getAll();
        verify(savedSnapshotService).getOwned();
    }

    @Test
    public void delete() {
        savedSnapshotController.delete(42L);
        verify(savedSnapshotService).deleteById(eq(42L));
    }

    @Test
    public void get() {
        when(dashboardConfig.getDashboards()).thenReturn(mockSnapshotDashboard);

        when(savedSnapshotService.getDashboardSearch(any(long.class))).thenReturn(savedSnapshot);

        savedSnapshotController.get(savedSnapshot.getId());
        verify(savedSnapshotService).getDashboardSearch(savedSnapshot.getId());
    }

    @Test
    public void getWhenSearchDoesNotExist() throws Exception {
        when(dashboardConfig.getDashboards()).thenReturn(mockSnapshotDashboard);

        when(savedSnapshotService.getDashboardSearch(any(long.class))).thenReturn(null);

        try {
            savedSnapshotController.get(savedSnapshot.getId());
            fail("Call to get() was expected to throw exception");
        } catch (final IllegalArgumentException e) {
            verify(savedSnapshotService).getDashboardSearch(savedSnapshot.getId());
            assertThat("Exception has the correct message",
                    e.getMessage(),
                    is("Configured ID " + savedSnapshot.getId() + " does not match any known Saved Snapshot"));
        }
    }
}
