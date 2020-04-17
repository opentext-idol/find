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
    private final ControlPointApiClient apiClient;

    /**
     * Interact with ControlPoint using alternative configuration.
     *
     * @param httpClient Used to make API requests
     * @param config How to connect to the server
     */
    public ControlPointService(final HttpClient httpClient, final ControlPointConfig config) {
        apiClient = new ControlPointApiClient(httpClient, config.getServer().toServerDetails());
    }

    @Autowired
    ControlPointService(
        final HttpClient httpClient,
        final ConfigService<IdolFindConfig> configService
    ) {
        this(httpClient, configService.getConfig().getControlPoint());
    }

    /**
     * Retrieve available policies.
     */
    public ControlPointPolicies getPolicies() throws ControlPointApiException {
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
        apiClient.postUrlencoded("policyfiles/bystoredstate",
            Arrays.asList(
                new BasicNameValuePair("api-version", "1.0"),
                new BasicNameValuePair("policyId", policy),
                new BasicNameValuePair("stateMatchId", documentsStateToken),
                new BasicNameValuePair("securityInfo", documentsSecurityInfo)
            ),
            Collections.emptyList(),
            Void.class);
    }

}
