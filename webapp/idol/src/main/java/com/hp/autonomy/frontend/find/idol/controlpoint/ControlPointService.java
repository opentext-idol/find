/*
 * Copyright 2020 Micro Focus International plc.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find.idol.controlpoint;

import com.autonomy.aci.client.services.Processor;
import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.idol.configuration.ControlPointConfig;
import com.hp.autonomy.frontend.find.idol.configuration.IdolFindConfig;
import com.hp.autonomy.types.idol.marshalling.ProcessorFactory;
import com.hp.autonomy.types.idol.responses.answer.ReportResponsedata;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
     * Retrieve available policies.
     */
    public ControlPointPolicies getPolicies() throws ControlPointApiException {
        checkEnabled();
        return apiClient.get("policies", Collections.singletonList(
            new BasicNameValuePair("api-version", "1.0")
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

        final ControlPointApplyPolicyResponse response =
            apiClient.postUrlencoded("policyfiles/bystoredstate",
                Arrays.asList(
                    new BasicNameValuePair("api-version", "1.0"),
                    new BasicNameValuePair("policyId", policy),
                    new BasicNameValuePair("stateMatchId", documentsStateToken),
                    new BasicNameValuePair("securityInfo", documentsSecurityInfo)
                ),
                Collections.emptyList(),
                ControlPointApplyPolicyResponse.class);

        if (!response.isSuccess()) {
            throw new ControlPointApiException("Failed to apply policy");
        }
        if (response.isPartialApplication()) {
            throw new ControlPointApiException("Policy was partially applied");
        }
    }

}
