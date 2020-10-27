/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.savedsearches.ConceptClusterPhrase;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ControlPointPolicyControllerTest {
    @Mock private ControlPointService cpService;
    @Mock private SavedSearchService<SavedSnapshot, SavedSnapshot.Builder> savedSnapshotService;
    @Mock private HavenSearchAciParameterHandler aciParameterHandler;
    private ControlPointPolicyController controller;

    @Before
    public void setUp() throws ControlPointApiException {
        Mockito.when(cpService.isEnabled()).thenReturn(true);

        Mockito.when(savedSnapshotService.getDashboardSearch(Mockito.anyLong()))
            .thenReturn(new SavedSnapshot.Builder()
                .setStateTokens(new HashSet<>(Arrays.asList(
                    new TypedStateToken("fetched,qms", TypedStateToken.StateTokenType.PROMOTIONS),
                    new TypedStateToken("fetched,normal", TypedStateToken.StateTokenType.QUERY)
                )))
                .build());
        Mockito.when(savedSnapshotService.build(Mockito.any()))
            .thenReturn(new SavedSnapshot.Builder()
                .setStateTokens(new HashSet<>(Arrays.asList(
                    new TypedStateToken("built,qms", TypedStateToken.StateTokenType.PROMOTIONS),
                    new TypedStateToken("built,normal", TypedStateToken.StateTokenType.QUERY)
                )))
                .build());

        Mockito.doAnswer(invocation -> {
            invocation.getArgumentAt(0, AciParameters.class)
                .put(QueryParams.SecurityInfo.name(), "sec info");
            return null;
        }).when(aciParameterHandler).addSecurityInfo(Mockito.any());

        controller = new ControlPointPolicyController(
            cpService, savedSnapshotService, aciParameterHandler);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPolicies_controlPointDisabled() throws ControlPointApiException {
        Mockito.when(cpService.isEnabled()).thenReturn(false);
        controller.getPolicies();
    }

    @Test
    public void testGetPolicies() throws ControlPointApiException {
        final List<ControlPointPolicy> policies = Arrays.asList(
            new ControlPointPolicy("1", "archive", true, true),
            new ControlPointPolicy("2", "delete", true, true),
            new ControlPointPolicy("3", "preserve", false, true),
            new ControlPointPolicy("4", "process", true, false),
            new ControlPointPolicy("5", "redirect", false, false)
        );

        Mockito.when(cpService.getPolicies("sec info"))
            .thenReturn(new ControlPointPolicies(policies));
        final List<ControlPointPolicy> result = controller.getPolicies();
        Mockito.verify(cpService).getPolicies("sec info");
        Assert.assertEquals("should return active, published policies", Arrays.asList(
            new ControlPointPolicy("1", "archive", true, true),
            new ControlPointPolicy("2", "delete", true, true)
        ), result);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApplyPolicy_controlPointDisabled() throws ControlPointApiException {
        Mockito.when(cpService.isEnabled()).thenReturn(false);
        controller.applyPolicy("the policy", 123L, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApplyPolicy_missingDocs() throws ControlPointApiException {
        controller.applyPolicy("the policy", null, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApplyPolicy_tooManyArguments() throws ControlPointApiException {
        controller.applyPolicy("the policy", 123L, new SavedQuery());
    }

    @Test
    public void testApplyPolicy_snapshot() throws ControlPointApiException {
        controller.applyPolicy("the policy", 123L, null);
        Mockito.verify(savedSnapshotService).getDashboardSearch(123L);
        Mockito.verify(cpService).applyPolicy("the policy", "fetched,normal", "sec info");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testApplyPolicy_missingSnapshot() throws ControlPointApiException {
        Mockito.when(savedSnapshotService.getDashboardSearch(123L)).thenReturn(null);
        controller.applyPolicy("the policy", 123L, null);
    }

    @Test
    public void testApplyPolicy_query() throws ControlPointApiException {
        final SavedQuery query = new SavedQuery.Builder()
            .setConceptClusterPhrases(Collections.singleton(
                new ConceptClusterPhrase("query", true, 1)
            ))
            .build();
        controller.applyPolicy("the policy", null, query);
        Mockito.verify(savedSnapshotService).build(query);
        Mockito.verify(cpService).applyPolicy("the policy", "built,normal", "sec info");
    }

}
