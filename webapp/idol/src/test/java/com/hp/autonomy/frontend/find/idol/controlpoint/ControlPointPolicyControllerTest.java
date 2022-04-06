/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
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
                .setStateTokens(new HashSet<>(Collections.singletonList(
                    new TypedStateToken("token", TypedStateToken.StateTokenType.QUERY)
                )))
                .build());

        Mockito.doReturn("sec info").when(aciParameterHandler).getSecurityInfo();

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

    @Test
    public void testApplyPolicy() throws ControlPointApiException {
        controller.applyPolicy("the policy", 123L, null);
        Mockito.verify(savedSnapshotService).getDashboardSearch(123L);
        Mockito.verify(cpService).applyPolicy("the policy", "token", "sec info");
    }

}
