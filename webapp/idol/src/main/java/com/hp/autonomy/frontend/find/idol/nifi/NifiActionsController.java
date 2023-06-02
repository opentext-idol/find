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
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.idol.beanconfiguration.UserConfiguration;
import com.hp.autonomy.frontend.find.idol.savedsearches.snapshot.SavedSnapshotService;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hpe.bigdata.frontend.spring.authentication.AuthenticationInformationRetriever;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * These APIs should only be called when NiFi is enabled
 * ({@link com.hp.autonomy.frontend.find.core.web.MvcConstants.NIFI_ENABLED}).
 */
@RestController
@RequestMapping(NifiActionsController.BASE_PATH)
class NifiActionsController {
    static final String BASE_PATH = "/api/public/nifi/actions";
    static final String EXECUTE_PATH = "execute";
    private static final String ACTION_PARAM = "action";
    private static final String SAVED_SNAPSHOT_ID_PARAM = "savedSnapshotId";
    private static final String SEARCH_NAME_PARAM = "searchName";
    private static final String LABEL_PARAM = "label";

    private final NifiService nifiService;
    private final SavedSearchService<SavedSnapshot, SavedSnapshot.Builder> savedSnapshotService;
    private final HavenSearchAciParameterHandler aciParameterHandler;
    private final AuthenticationInformationRetriever<?, ? extends Principal>
        authenticationInformationRetriever;
    private final UserConfiguration userConfiguration;

    @Autowired
    NifiActionsController(
        final NifiService nifiService,
        final SavedSearchService<SavedSnapshot, SavedSnapshot.Builder> savedSnapshotService,
        final HavenSearchAciParameterHandler aciParameterHandler,
        final AuthenticationInformationRetriever<?, ? extends Principal>
            authenticationInformationRetriever,
        final UserConfiguration userConfiguration
    ) {
        this.nifiService = nifiService;
        this.savedSnapshotService = savedSnapshotService;
        this.aciParameterHandler = aciParameterHandler;
        this.authenticationInformationRetriever = authenticationInformationRetriever;
        this.userConfiguration = userConfiguration;
    }

    /**
     * Retrieve actions.
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<NifiAction> getActions() {
        final String username = authenticationInformationRetriever.getPrincipal().getName();
        final List<String> roles =
            userConfiguration.getCommunityRoles(authenticationInformationRetriever);
        return nifiService.getActions(username, roles);
    }

    /**
     * Execute an action on the specified documents.  Exactly one of `snapshotId` and `query` is
     * required.
     *
     * @param action ID of the action to execute
     * @param snapshotId Execute the action on the documents in the saved snapshot with this ID
     * @param query Execute the action on the documents returned by this query
     * @param searchName Name of the search containing the documents - saved with the execution as
     *                   metadata
     * @param label Optional label to save with the execution as metadata
     */
    @RequestMapping(value = EXECUTE_PATH, method = RequestMethod.POST)
    public void executeAction(
        @RequestParam(value = ACTION_PARAM) final String action,
        @RequestParam(value = SAVED_SNAPSHOT_ID_PARAM, required = false) final Long snapshotId,
        @RequestBody(required = false) final SavedQuery query,
        @RequestParam(value = SEARCH_NAME_PARAM, required = false) final String searchName,
        @RequestParam(value = LABEL_PARAM, required = false) final String label
    ) {
        final String stateToken = SavedSnapshotService
            .toSnapshotToken(savedSnapshotService, snapshotId, query)
            .getStateToken();
        final String username = authenticationInformationRetriever.getPrincipal().getName();
        final List<String> roles =
            userConfiguration.getCommunityRoles(authenticationInformationRetriever);
        nifiService.executeAction(
            action, stateToken, aciParameterHandler.getSecurityInfo(), username, roles,
            searchName, label);
    }

}
