/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.ControlPointConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;

/**
 * Interact with ControlPoint.  Methods also throw {@link ControlPointServiceException}.
 */
@Component
public class ControlPointService {
    // null if ControlPoint is disabled
    private final ControlPointApiClient apiClient;

    /**
     * Interact with ControlPoint using alternative configuration.
     *
     * @param httpClient Used to make API requests
     * @param config How to connect to the server
     */
    public ControlPointService(final HttpClient httpClient, final ControlPointConfig config) {
        if (config == null || !BooleanUtils.isTrue(config.getEnabled())) {
            apiClient = null;
        } else {
            apiClient = new ControlPointApiClient(httpClient, config.getServer().toServerDetails());
        }
    }

    @Autowired
    ControlPointService(
        final HttpClient httpClient,
        final ConfigService<IdolFindConfig> configService
    ) {
        this(httpClient, configService.getConfig().getControlPoint());
    }

    private void checkEnabled() {
        if (apiClient == null) {
            throw new IllegalArgumentException("ControlPoint is disabled");
        }
    }

    /**
     * @return Whether ControlPoint is enabled; if false, methods will throw
     */
    public boolean isEnabled() {
        return apiClient != null;
    }

    /**
     * Check the API is accessible.
     *
     * @throws ControlPointApiException
     */
    public void checkStatus() throws ControlPointApiException {
        checkEnabled();
        apiClient.get("status", Collections.singletonList(
            new BasicNameValuePair("api-version", "1.0")
        ), null);
    }

    /**
     * Retrieve available policies.
     *
     * @param userSecurityInfo Used to determine which policies are visible
     */
    public ControlPointPolicies getPolicies(final String userSecurityInfo)
        throws ControlPointApiException
    {
        checkEnabled();
        return apiClient.get("policies/foridolfind", Arrays.asList(
            new BasicNameValuePair("api-version", "1.0"),
            new BasicNameValuePair("securityInfo", userSecurityInfo)
        ), ControlPointPolicies.class);
    }

    /**
     * Apply a policy to a set of documents.
     *
     * @param policy {@link ControlPointPolicy#id}
     * @param documentsStateToken Defines the document set
     * @param documentsSecurityInfo Used to access the documents, to ensure they're all visible
     */
    public void applyPolicy(
        final String policy,
        final String documentsStateToken,
        final String documentsSecurityInfo
    ) throws ControlPointApiException {
        checkEnabled();

        apiClient.postUrlencoded("policyfiles/bystoredstate",
            Arrays.asList(
                new BasicNameValuePair("api-version", "1.0"),
                new BasicNameValuePair("policyId", policy),
                new BasicNameValuePair("stateMatchId", documentsStateToken),
                new BasicNameValuePair("securityInfo", documentsSecurityInfo)
            ),
            Collections.emptyList(),
            ControlPointApplyPolicyResponse.class);
    }

}
