/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.idol.savedsearches.snapshot.SavedSnapshotService;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
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
     * Retrieve active policies.
     */
    @RequestMapping(method = RequestMethod.GET)
    public List<ControlPointPolicy> getPolicies() throws ControlPointApiException {
        checkEnabled();
        return controlPointService.getPolicies(aciParameterHandler.getSecurityInfo()).stream()
            .filter(policy -> policy.isActive() && policy.isPublished())
            .collect(Collectors.toList());
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
        final String stateToken = SavedSnapshotService
            .toSnapshotToken(savedSnapshotService, snapshotId, query)
            .getStateToken();
        controlPointService.applyPolicy(
            policy, stateToken, aciParameterHandler.getSecurityInfo());
    }

}
