/*
 * Copyright 2020 Open Text.
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

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.hp.autonomy.frontend.find.core.savedsearches.SavedSearchService;
import com.hp.autonomy.frontend.find.core.savedsearches.query.SavedQuery;
import com.hp.autonomy.frontend.find.core.savedsearches.snapshot.SavedSnapshot;
import com.hp.autonomy.frontend.find.idol.api.ControlPointApi;
import com.hp.autonomy.frontend.find.idol.api.model.ControlPointPoliciesResponse;
import com.hp.autonomy.frontend.find.idol.api.model.ControlPointPolicy;
import com.hp.autonomy.frontend.find.idol.savedsearches.snapshot.SavedSnapshotService;
import com.hp.autonomy.searchcomponents.idol.search.HavenSearchAciParameterHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Collectors;

/**
 * These APIs should only be called when ControlPoint is enabled
 * ({@link com.hp.autonomy.frontend.find.core.web.MvcConstants.CONTROL_POINT_ENABLED}).
 */
@RestController
class ControlPointPolicyController implements ControlPointApi {
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
    @Override
    public ResponseEntity<ControlPointPoliciesResponse> getControlPointPolicies() {
        checkEnabled();
        try {
            return ResponseEntity.ok(new ControlPointPoliciesResponse(
                controlPointService.getPolicies(aciParameterHandler.getSecurityInfo()).stream()
                    .filter(policy -> policy.isActive() && policy.isPublished())
                    .map(policy -> new ControlPointPolicy(policy.getId(), policy.getName()))
                    .collect(Collectors.toList())));
        } catch (final ControlPointApiException e) {
            throw new ControlPointApiRuntimeException(e);
        }
    }

    /**
     * Apply a policy to the specified documents.  Exactly one of `snapshotId` and `query` is
     * required.
     *
     * @param policy ID of the policy to apply
     * @param snapshotId Apply the policy to the documents in the saved snapshot with this ID
     * @param query Apply the policy to the documents returned by this query
     */
    @Override
    public ResponseEntity<Void> applyControlPointPolicy(
        final String policy,
        final Long snapshotId,
        final SavedQuery query
    ) {
        checkEnabled();
        final String stateToken = SavedSnapshotService
            .toSnapshotToken(savedSnapshotService, snapshotId, query)
            .getStateToken();
        try {
            controlPointService.applyPolicy(
                policy, stateToken, aciParameterHandler.getSecurityInfo());
        } catch (final ControlPointApiException e) {
            throw new ControlPointApiRuntimeException(e);
        }
        return ResponseEntity.ok().build();
    }

}
