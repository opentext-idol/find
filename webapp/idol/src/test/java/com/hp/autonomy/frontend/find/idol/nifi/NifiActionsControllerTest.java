/*
 * Copyright 2021 Open Text.
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

package com.hp.autonomy.frontend.find.idol.nifi;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.idol.beanconfiguration.UserConfiguration;
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
import org.mockito.junit.MockitoJUnitRunner;

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
    @Mock private UserConfiguration userConfiguration;
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
        Mockito.doReturn(Arrays.asList("role1", "role2"))
            .when(userConfiguration).getCommunityRoles(authenticationInformationRetriever);

        controller = new NifiActionsController(
            nifiService, savedSnapshotService, aciParameterHandler,
            authenticationInformationRetriever, userConfiguration);
    }

    @Test
    public void testGetActions() throws ControlPointApiException {
        final List<NifiAction> actions = Arrays.asList(
            new NifiAction("id1", "Name 1"),
            new NifiAction("id2", "Name 2"),
            new NifiAction("id3", "Name 3")
        );

        Mockito.when(nifiService.getActions("the user", Arrays.asList("role1", "role2")))
            .thenReturn(actions);
        Assert.assertEquals(actions, controller.getActions());
    }

    @Test
    public void testExecuteAction() throws ControlPointApiException {
        controller.executeAction("the action", 123L, null, "the search", "the label");
        Mockito.verify(savedSnapshotService).getDashboardSearch(123L);
        Mockito.verify(nifiService).executeAction(
            "the action", "token", "sec info", "the user", Arrays.asList("role1", "role2"),
            "the search", "the label");
    }

}
