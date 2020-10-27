/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.autonomy.aci.client.util.AciParameters;
import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.searchcomponents.core.search.TypedStateToken;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import com.hp.autonomy.types.requests.idol.actions.query.params.QueryParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * These APIs should only be called when ControlPoint is enabled
 * ({@link com.hp.autonomy.frontend.find.core.web.MvcConstants.CONTROL_POINT_ENABLED}).
 */
@RestController
@RequestMapping(ControlPointPolicyController.BASE_PATH)
class ControlPointPolicyController {
    static final String BASE_PATH = "/api/public/controlpoint/policy";
    static final String APPLY_PATH = "apply";
    private static final String POLICY_PARAM = "policy";
    private static final String SAVED_SNAPSHOT_ID_PARAM = "savedSnapshotId";
    private static final int DEFAULT_MAX_POLICIES = 30;

    private final ControlPointService controlPointService;
    private final SavedSearchService<SavedSnapshot, SavedSnapshot.Builder> savedSnapshotService;
    private final HavenSearchAciParameterHandler aciParameterHandler;

    @Autowired
    ControlPointPolicyController(
        final ControlPointService controlPointService,
        final SavedSearchService<SavedSnapshot, SavedSnapshot.Builder> savedSnapshotService,
        final HavenSearchAciParameterHandler aciParameterHandler
    ) {
        this.controlPointService = controlPointService;
        this.savedSnapshotService = savedSnapshotService;
        this.aciParameterHandler = aciParameterHandler;
    }

    private void checkEnabled() {
        if (!controlPointService.isEnabled()) {
            throw new IllegalArgumentException("ControlPoint is disabled");
        }
    }

    /**
     * @return Security info string for the current user session.
     */
    private String getSecurityInfo() {
        // get the ACI parameter handler to determine security info for us
        final AciParameters aciParams = new AciParameters();
        aciParameterHandler.addSecurityInfo(aciParams);
        return aciParams.get(QueryParams.SecurityInfo.name());
    }

    /**
     * Retrieve active policies.
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<ControlPointPolicy> getPolicies() throws ControlPointApiException {
        checkEnabled();
        return controlPointService.getPolicies(getSecurityInfo()).stream()
            .filter(policy -> policy.isActive() && policy.isPublished())
            .collect(Collectors.toList());
    }

    /**
     * Apply a policy to documents in a snapshot.
     */
    private void applyPolicy(final String policy, final SavedSnapshot snapshot)
        throws ControlPointApiException
    {
        final TypedStateToken stateToken = snapshot.getStateTokens().stream()
            .filter(x -> x.getType().equals(TypedStateToken.StateTokenType.QUERY))
            .findFirst()
            .orElseThrow(() ->
                new RuntimeException("Saved Snapshot has no associated state token"));
        controlPointService.applyPolicy(policy, stateToken.getStateToken(), getSecurityInfo());
    }

    /**
     * Apply a policy to the specified documents.  Exactly one of `snapshotId` and `query` is
     * required.
     *
     * @param policy ID of the policy to apply
     * @param snapshotId Apply the policy to the documents in the saved snapshot with this ID
     * @param query Apply the policy to the documents returned by this query
     */
    @RequestMapping(value = APPLY_PATH, method = RequestMethod.POST)
    public void applyPolicy(
        @RequestParam(value = POLICY_PARAM) final String policy,
        @RequestParam(value = SAVED_SNAPSHOT_ID_PARAM, required = false) final Long snapshotId,
        @RequestBody(required = false) final SavedQuery query
    ) throws ControlPointApiException {
        checkEnabled();
        if (snapshotId == null && query == null) {
            throw new IllegalArgumentException("Snapshot ID or saved query required");
        }
        if (snapshotId != null && query != null) {
            throw new IllegalArgumentException("Only one of snapshot ID and saved query allowed");
        }

        final SavedSnapshot snapshot;
        if (snapshotId != null) {
            snapshot = savedSnapshotService.getDashboardSearch(snapshotId);
            if (snapshot == null) {
                throw new IllegalArgumentException("No Saved Snapshot found with ID " + snapshotId);
            }
        } else {
            snapshot = savedSnapshotService.build(query);
        }

        applyPolicy(policy, snapshot);
    }

}
