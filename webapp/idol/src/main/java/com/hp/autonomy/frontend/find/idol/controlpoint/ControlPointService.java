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
    private final HttpClient httpClient;
    // one of configService or config is null
    private final ConfigService<IdolFindConfig> configService;
    private final ControlPointConfig config;

    @Autowired
    ControlPointService(
        final HttpClient httpClient,
        final ConfigService<IdolFindConfig> configService
    ) {
        this.httpClient = httpClient;
        this.configService = configService;
        this.config = null;
    }

    /**
     * Interact with ControlPoint using a fixed configuration.
     *
     * @param httpClient Used to make API requests
     * @param config How to connect to the server
     */
    public ControlPointService(final HttpClient httpClient, final ControlPointConfig config) {
        this.httpClient = httpClient;
        this.configService = null;
        this.config = config;
    }

    private ControlPointConfig getConfig() {
        if (config != null) {
            return config;
        } else {
            return configService.getConfig().getControlPoint();
        }
    }

    private ControlPointApiClient getApiClient() {
        final ControlPointConfig config = getConfig();
        if (config == null || !BooleanUtils.isTrue(config.getEnabled())) {
            throw new IllegalArgumentException("ControlPoint is disabled");
        } else {
            return new ControlPointApiClient(httpClient, config.getServer().toServerDetails());
        }
    }

    /**
     * @return Whether ControlPoint is enabled; if false, methods will throw
     */
    public boolean isEnabled() {
        final ControlPointConfig config = getConfig();
        return config != null && BooleanUtils.isTrue(config.getEnabled());
    }

    /**
     * Check the API is accessible.
     *
     * @throws ControlPointApiException
     */
    public void checkStatus() throws ControlPointApiException {
        getApiClient().get("status", Collections.singletonList(
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
        return getApiClient().get("policies/foridolfind", Arrays.asList(
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
        getApiClient().postUrlencoded("policyfiles/bystoredstate",
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
