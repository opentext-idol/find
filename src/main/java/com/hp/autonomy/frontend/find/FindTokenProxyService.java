/*
 * Copyright 2014-2015 Hewlett-Packard Development Company, L.P.
 * Licensed under the MIT License (the "License"); you may not use this file except in compliance with the License.
 */

package com.hp.autonomy.frontend.find;

import com.hp.autonomy.frontend.configuration.ConfigService;
import com.hp.autonomy.frontend.find.configuration.FindConfig;
import com.hp.autonomy.frontend.find.configuration.IodConfig;
import com.hp.autonomy.hod.client.api.authentication.ApiKey;
import com.hp.autonomy.hod.client.api.authentication.AuthenticationService;
import com.hp.autonomy.hod.client.api.authentication.TokenType;
import com.hp.autonomy.hod.client.error.HodErrorException;
import com.hp.autonomy.hod.client.token.TokenProxy;
import com.hp.autonomy.hod.client.token.TokenProxyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Short term token proxy service until SSO is ready. Will gradually fill up the in memory token repository over time
 */
public class FindTokenProxyService implements TokenProxyService {

    @Autowired
    private ConfigService<FindConfig> configService;

    private final AuthenticationService authenticationService;

    public FindTokenProxyService(final AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @Override
    public TokenProxy getTokenProxy() {
        try {
            final IodConfig iodConfig = configService.getConfig().getIod();
            final String apiKey = iodConfig.getApiKey();

            if (apiKey == null) {
                // app probably hasn't been configured yet
                return null;
            } else {
                return authenticationService.authenticateApplication(new ApiKey(apiKey), iodConfig.getApplication(), iodConfig.getDomain(), TokenType.simple);
            }
        } catch (final HodErrorException e) {
            throw new RuntimeException("Could not obtain token proxy", e);
        }
    }

}
