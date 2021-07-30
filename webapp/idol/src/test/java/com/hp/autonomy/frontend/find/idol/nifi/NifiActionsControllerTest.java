/*
 * Copyright 2021 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.nifi;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.idol.controlpoint.ControlPointApiException;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.apache.http.auth.BasicUserPrincipal;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class NifiActionsControllerTest {
    @Mock private NifiService nifiService;
    @Mock private SavedSearchService<SavedSnapshot, SavedSnapshot.Builder> savedSnapshotService;
    @Mock private HavenSearchAciParameterHandler aciParameterHandler;
    @Mock private AuthenticationInformationRetriever<?, ? extends Principal>
        authenticationInformationRetriever;
    private NifiActionsController controller;

    @Before
    public void setUp() throws ControlPointApiException {
        Mockito.when(savedSnapshotService.getDashboardSearch(Mockito.anyLong()))
            .thenReturn(new SavedSnapshot.Builder()
                .setStateTokens(new HashSet<>(Collections.singletonList(
                    new TypedStateToken("token", TypedStateToken.StateTokenType.QUERY)
                )))
                .build());
        Mockito.doReturn("sec info").when(aciParameterHandler).getSecurityInfo();
        Mockito.doReturn(new BasicUserPrincipal("the user"))
            .when(authenticationInformationRetriever).getPrincipal();

        controller = new NifiActionsController(
            nifiService, savedSnapshotService, aciParameterHandler,
            authenticationInformationRetriever);
    }

    @Test
    public void testGetActions() throws ControlPointApiException {
        final List<NifiAction> actions = Arrays.asList(
            new NifiAction("id1", "Name 1"),
            new NifiAction("id2", "Name 2"),
            new NifiAction("id3", "Name 3")
        );

        Mockito.when(nifiService.getActions()).thenReturn(actions);
        Assert.assertEquals(actions, controller.getActions());
    }

    @Test
    public void testExecuteAction() throws ControlPointApiException {
        controller.executeAction("the action", 123L, null, "the search", "the label");
        Mockito.verify(savedSnapshotService).getDashboardSearch(123L);
        Mockito.verify(nifiService).executeAction(
            "the action", "token", "sec info", "the user", "the search", "the label");
    }

}
